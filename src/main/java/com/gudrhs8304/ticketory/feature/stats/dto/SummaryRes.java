package com.gudrhs8304.ticketory.feature.stats.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "요약 응답")
public record SummaryRes(
        @Schema(description = "총 승인 매출 합계") BigDecimal grossRevenue,
        @Schema(description = "환불 금액 합계") BigDecimal refundedAmount,
        @Schema(description = "순매출(총매출-환불)") BigDecimal netRevenue,
        @Schema(description = "승인 결제 건수") long paymentCount
) {}
