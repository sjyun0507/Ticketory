package com.gudrhs8304.ticketory.dto.payment;

import jakarta.validation.constraints.NotBlank;

public record TossCancelRequest(
        @NotBlank String paymentKey,
        @NotBlank String cancelReason
) {
}
