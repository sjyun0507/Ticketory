package com.gudrhs8304.ticketory.dto.payment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record TossConfirmRequestDTO(
        @NotBlank String paymentKey,
        @NotBlank String orderId,
        @Min(100) BigDecimal amount
) {}
