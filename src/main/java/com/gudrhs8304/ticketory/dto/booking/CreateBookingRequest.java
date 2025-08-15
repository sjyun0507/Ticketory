package com.gudrhs8304.ticketory.dto.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateBookingRequest(
    @Schema(example = "1") @NotNull Long screeningId,
    @Schema(example = "[1,2]") @NotNull List<Long> seatIds
) {}

