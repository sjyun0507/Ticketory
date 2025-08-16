package com.gudrhs8304.ticketory.dto.payment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TossInitiateRequestDTO (
    @NotNull Long bookingId,
    @Min(100) BigDecimal amount
){}
