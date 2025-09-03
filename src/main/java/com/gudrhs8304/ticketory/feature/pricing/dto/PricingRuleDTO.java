// com.gudrhs8304.ticketory.feature.pricing.dto.PricingRuleDTO
package com.gudrhs8304.ticketory.feature.pricing.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.gudrhs8304.ticketory.feature.point.enums.PricingKind;
import com.gudrhs8304.ticketory.feature.pricing.enums.PricingOp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PricingRuleDTO {
    Long id;

    @Schema(description = "상영관 ID", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonAlias({"screen_id","screenId"})
    Long screenId;

    @Schema(description = "요금 종류", example = "ADULT")
    @JsonAlias({"kind"})
    PricingKind kind;

    @Schema(description = "연산 방식", example = "SET")
    @JsonAlias({"op"})
    PricingOp op;            // SET / ADD / MUL

    @Schema(description = "금액(SET/ADD) 또는 계수(MUL)", example = "13000")
    @JsonAlias({"amount"})
    BigDecimal amount;

    @Schema(description = "우선순위(작을수록 우선)", example = "100")
    @JsonAlias({"priority"})
    Integer priority;

    @Schema(description = "적용 시작 시각", example = "2025-08-01T00:00:00")
    @JsonAlias({"valid_from","validFrom"})
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime validFrom;

    @Schema(description = "적용 종료 시각(미설정 시 무기한)", example = "2025-09-30T23:59:59")
    @JsonAlias({"valid_to","validTo"})
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime validTo;

    @Schema(description = "활성화 여부", example = "true")
    @JsonAlias({"enabled"})
    Boolean enabled;

    @JsonAlias({"currency"})
    String currency;
}