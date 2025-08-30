package com.gudrhs8304.ticketory.feature.screen.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateScreenRequest(
        @Schema(example = "1관") @NotBlank String name,
        @Schema(example = "10") @NotNull @Min(1) Integer rows,
        @Schema(example = "12") @NotNull @Min(1) Integer cols,
        @Schema(example = "서울점 5F") String location,
        @Schema(example = "일반관") String description,
        @Schema(example = "true") Boolean isActive
) {}
