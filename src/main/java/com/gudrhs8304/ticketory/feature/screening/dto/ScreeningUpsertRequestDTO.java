package com.gudrhs8304.ticketory.feature.screening.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScreeningUpsertRequestDTO {
    @Schema(example = "1")
    private Long movieId;

    @Schema(example = "1")
    private Long screenId;

    @Schema(example = "2025-08-18T10:30:00")
    private LocalDateTime startAt;

    @Schema(example = "2025-08-18T12:40:00")
    private LocalDateTime endAt;
}
