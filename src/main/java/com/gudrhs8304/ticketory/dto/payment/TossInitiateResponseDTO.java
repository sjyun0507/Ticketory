package com.gudrhs8304.ticketory.dto.payment;

import java.math.BigDecimal;

public record TossInitiateResponseDTO(
        String orderId,
        BigDecimal amount,
        String orderName,
        String customerEmail,
        String successUrl,
        String failUrl,
        String clientKey
) { }
