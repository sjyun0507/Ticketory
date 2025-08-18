// com.gudrhs8304.ticketory.dto.pricing.PricingRuleDTO
package com.gudrhs8304.ticketory.dto.pricing;

import com.gudrhs8304.ticketory.domain.enums.PricingKind;
import com.gudrhs8304.ticketory.domain.enums.PricingOp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PricingRuleDTO {
    Long id;

    @Schema(description = "상영관 ID", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    Long screenId;

    @Schema(description = "요금 종류", example = "ADULT")
    PricingKind kind;

    @Schema(description = "연산 방식", example = "SET")
    PricingOp op;            // SET / ADD / MUL

    @Schema(description = "금액(SET/ADD) 또는 계수(MUL)", example = "13000")
    BigDecimal amount;

    @Schema(description = "우선순위(작을수록 우선)", example = "100")
    Integer priority;

    @Schema(description = "적용 시작 시각", example = "2025-08-01T00:00:00")
    LocalDateTime validFrom;

    @Schema(description = "적용 종료 시각(미설정 시 무기한)", example = "2025-09-30T23:59:59")
    LocalDateTime validTo;

    @Schema(description = "활성화 여부", example = "true")
    Boolean enabled;

    String currency;
}