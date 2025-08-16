package com.gudrhs8304.ticketory.controller;

import com.gudrhs8304.ticketory.domain.Payment;
import com.gudrhs8304.ticketory.dto.payment.*;
import com.gudrhs8304.ticketory.security.SecurityUtil;
import com.gudrhs8304.ticketory.service.TossPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.RedirectView;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments/toss")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class PaymentController {

    private final TossPaymentService tossPaymentService;

    @Operation(summary = "Toss 결제 시작 파라미터 생성")
    @PostMapping("/initiate")
    public ResponseEntity<TossInitiateResponseDTO> initiate(@Valid @RequestBody TossInitiateRequestDTO req) {
        Long me = SecurityUtil.currentMemberId(); // 로그인 필요 없다면 null 허용 로직으로 변경
        return ResponseEntity.ok(tossPaymentService.initiate(req, me));
    }

    @Operation(summary = "Toss 결제 승인(프론트 successUrl에서 paymentKey/orderId/amount 받아 호출)")
    @PostMapping("/confirm")
    public ResponseEntity<Payment> confirm(@Valid @RequestBody TossConfirmRequestDTO req) {
        return ResponseEntity.ok(tossPaymentService.confirm(req));
    }

    @Operation(summary = "Toss 결제 취소")
    @PostMapping("/cancel")
    public ResponseEntity<Payment> cancel(@Valid @RequestBody TossCancelRequest req) {
        return ResponseEntity.ok(tossPaymentService.cancel(req));
    }

    @GetMapping("/api/payments/toss/success")
    public RedirectView success(@RequestParam String paymentKey,
                                @RequestParam String orderId,
                                @RequestParam BigDecimal amount) {
        tossPaymentService.confirm(new TossConfirmRequestDTO(paymentKey, orderId, amount));
        return new RedirectView("/success.html"); // 또는 JSON 응답
    }
}
