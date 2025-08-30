package com.gudrhs8304.ticketory.feature.payment.dto;

import java.math.BigDecimal;

public record ApprovePaymentRequest(
        Long bookingId,
        String method,           // CARD/KAKAO/NAVERPAY/TOSS
        BigDecimal amount
) {}
