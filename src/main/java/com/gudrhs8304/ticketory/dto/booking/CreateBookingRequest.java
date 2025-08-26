package com.gudrhs8304.ticketory.dto.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public record CreateBookingRequest(
    @Schema(example = "1") @NotNull Long screeningId,
    @Schema(example = "[1,2]") @NotNull List<Long> seatIds,
    @Schema(example = "{\"adult\":2, \"teen\":1}") Map<String, Integer> counts,
    @Schema(example = "1") Integer adult,   // 선택
    @Schema(example = "1") Integer teen
) {}

