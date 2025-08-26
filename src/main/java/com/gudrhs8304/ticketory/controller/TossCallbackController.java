package com.gudrhs8304.ticketory.controller;

import com.gudrhs8304.ticketory.dto.pay.ConfirmPaymentRequestDTO;
import com.gudrhs8304.ticketory.service.PaymentService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/payments/toss")
@RequiredArgsConstructor
public class TossCallbackController {

    private final PaymentService paymentService; // 내부에서 toss confirm 호출 + DB 반영 + seat_hold 정리
    private static final Logger log = LoggerFactory.getLogger(TossCallbackController.class);

    @GetMapping("/success")
    public ResponseEntity<?> success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount,
            HttpServletResponse resp
    ) throws IOException {
        log.info("[TOSS] success callback: paymentKey={}, orderId={}, amount={}", paymentKey, orderId, amount);
        paymentService.confirmAndFinalize(new ConfirmPaymentRequestDTO(paymentKey, orderId, BigDecimal.valueOf(amount)));
        // 완료 후 프론트 성공 페이지로 리다이렉트(필요 시 주문번호 전달)
        resp.sendRedirect("http://localhost:5173/success?orderId=" + URLEncoder.encode(orderId, StandardCharsets.UTF_8));
        return null;
    }

    @GetMapping("/fail")
    public void fail(@RequestParam String code,
                     @RequestParam String message,
                     @RequestParam String orderId,
                     HttpServletResponse resp) throws IOException {
        // 실패 처리 로깅/정리
        resp.sendRedirect("http://localhost:5173/fail?code=" + code +
                "&message=" + URLEncoder.encode(message, StandardCharsets.UTF_8) +
                "&orderId=" + URLEncoder.encode(orderId, StandardCharsets.UTF_8));
    }
}
