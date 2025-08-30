package com.gudrhs8304.ticketory.feature.stats.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "영화별 Top-N 매출")
public record TopMovieRes(
        @Schema(description = "영화 ID") Long movieId,
        @Schema(description = "영화 제목") String title,
        @Schema(description = "승인 매출 합계") BigDecimal revenue
) {}
