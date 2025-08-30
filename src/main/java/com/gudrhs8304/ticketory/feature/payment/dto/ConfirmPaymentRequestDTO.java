package com.gudrhs8304.ticketory.feature.payment.dto;

import java.math.BigDecimal;

public record ConfirmPaymentRequestDTO(
        String paymentKey,   // 토스에서 내려준 paymentKey
        String orderId,      // 우리가 생성한 주문번호 (ORD-...)
        BigDecimal amount    // 최종 결제금액
) {
}