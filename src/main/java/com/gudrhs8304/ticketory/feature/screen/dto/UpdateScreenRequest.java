package com.gudrhs8304.ticketory.feature.screen.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

public record UpdateScreenRequest(
        @Schema(example = "리뉴얼 1관") String name,

        @Schema(example = "서울 1층") String location,

        @Schema(example = "VIP 좌석 포함 리뉴얼 상영관") String description,

        @Schema(example = "true") Boolean isActive,

        @Schema(example = "12") @Min(1) Integer rows,

        @Schema(example = "14") @Min(1) Integer cols
) {}
