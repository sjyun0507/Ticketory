package com.gudrhs8304.ticketory.controller;

import com.gudrhs8304.ticketory.domain.Payment;
import com.gudrhs8304.ticketory.dto.payment.PaymentOrderCreateReqDTO;
import com.gudrhs8304.ticketory.dto.payment.TossConfirmRequestDTO;
import com.gudrhs8304.ticketory.dto.payment.TossInitiateRequestDTO;
import com.gudrhs8304.ticketory.dto.payment.TossInitiateResponseDTO;
import com.gudrhs8304.ticketory.service.BookingService;
import com.gudrhs8304.ticketory.service.TossPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PaymentsController {
    private final BookingService bookingService;
    private final TossPaymentService tossPaymentService;

    /** 결제 시작 (주문/임시 결제 생성) */
    @PostMapping("/api/payments")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody PaymentOrderCreateReqDTO req) {
        // TODO: 필요하면 검증/저장
        String orderId = UUID.randomUUID().toString();
        return ResponseEntity.ok(Map.of("orderId", orderId));
    }

    /** 토스 결제 승인(프론트 successUrl에서 호출) */
    @PostMapping("/payments/confirm")
    public ResponseEntity<Payment> confirm(@RequestBody TossConfirmRequestDTO req) {
        return ResponseEntity.ok(tossPaymentService.confirm(req));
    }

    /** 결제 단건 조회 */
    @GetMapping("/api/payments/{paymentId}")
    public ResponseEntity<Payment> get(@PathVariable Long paymentId) {
        return ResponseEntity.of(tossPaymentService.findById(paymentId));
    }


}