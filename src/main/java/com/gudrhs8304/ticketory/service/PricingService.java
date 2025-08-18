package com.gudrhs8304.ticketory.service;

import com.gudrhs8304.ticketory.domain.PricingRule;
import com.gudrhs8304.ticketory.domain.enums.PricingKind;
import com.gudrhs8304.ticketory.domain.enums.PricingOp;
import com.gudrhs8304.ticketory.repository.PricingRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final PricingRuleRepository repo;

    /** 규칙이 없을 때 기본 단가 */
    private static final BigDecimal DEFAULT_UNIT_PRICE = new BigDecimal("12000");

    /**
     * 상영관 + 요금종류 기준으로 단가 계산
     * @param screenId 상영관 ID
     * @param kind     요금 종류(성인/청소년 등). null이면 모든 kind 규칙을 적용.
     * @param at       기준 시각(유효기간 판정용). null이면 기간 무시.
     */
    public BigDecimal resolvePrice(Long screenId, PricingKind kind, LocalDateTime at) {
        BigDecimal price = DEFAULT_UNIT_PRICE;

        // 1) 저장소에서 우선순위 정렬된 규칙 조회
        List<PricingRule> rules = (kind == null)
                ? repo.findByScreenIdAndEnabledTrueOrderByPriorityAscIdAsc(screenId)
                : repo.findByScreenIdAndKindAndEnabledTrueOrderByPriorityAscIdAsc(screenId, kind);

        // 2) 유효기간 필터
        rules = rules.stream()
                .filter(r -> isInRange(r, at))
                .toList();

        // 3) 규칙 적용
        for (PricingRule r : rules) {
            PricingOp op = r.getOp();
            BigDecimal amt = r.getAmount() == null ? BigDecimal.ZERO : r.getAmount();

            switch (op) {
                case SET -> {
                    // 금액을 고정
                    price = amt;
                }
                case PLUS -> {
                    // 정액 가산
                    price = price.add(amt);
                }
                case MINUS -> {
                    // 정액 감액
                    price = price.subtract(amt);
                }
                case PCT_PLUS -> {
                    // % 인상 (예: amt=10 → 10% 인상)
                    BigDecimal rate = BigDecimal.ONE.add(amt.movePointLeft(2)); // 1 + pct/100
                    price = price.multiply(rate);
                }
                case PCT_MINUS -> {
                    // % 인하 (예: amt=10 → 10% 인하)
                    BigDecimal rate = BigDecimal.ONE.subtract(amt.movePointLeft(2)); // 1 - pct/100
                    price = price.multiply(rate);
                }
            }
        }

        // 4) 음수 방지 + 원단위 반올림
        if (price.signum() < 0) price = BigDecimal.ZERO;
        return price.setScale(0, RoundingMode.HALF_UP);
    }

    private boolean isInRange(PricingRule r, LocalDateTime at) {
        if (at == null) return true;
        if (r.getValidFrom() != null && at.isBefore(r.getValidFrom())) return false;
        if (r.getValidTo() != null && at.isAfter(r.getValidTo())) return false;
        return true;
    }


}