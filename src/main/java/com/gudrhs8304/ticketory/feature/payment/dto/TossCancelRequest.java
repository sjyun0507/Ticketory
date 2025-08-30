package com.gudrhs8304.ticketory.feature.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record TossCancelRequest(
        @NotBlank String paymentKey,
        @NotBlank String cancelReason
) {
}
