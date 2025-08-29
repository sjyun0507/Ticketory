package com.gudrhs8304.ticketory.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "일자별 승인 매출")
public record DailyRevenueRes(
        @Schema(description = "일자") LocalDate date,
        @Schema(description = "승인 매출 합계") BigDecimal revenue
) {}
