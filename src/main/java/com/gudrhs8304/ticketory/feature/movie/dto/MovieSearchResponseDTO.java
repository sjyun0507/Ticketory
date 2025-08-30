package com.gudrhs8304.ticketory.feature.movie.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record MovieSearchResponseDTO(
        @Schema(example = "4") Long movieId,
        @Schema(example = "전지적 독자시점") String title,
        @Schema(example = "https://.../poster.jpg") String posterUrl,
        @Schema(example = "2025-09-10") LocalDate releaseDate
) {
}
