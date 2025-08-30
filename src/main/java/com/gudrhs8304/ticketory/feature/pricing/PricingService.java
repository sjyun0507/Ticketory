package com.gudrhs8304.ticketory.feature.pricing;

import com.gudrhs8304.ticketory.feature.pricing.domain.PricingRule;
import com.gudrhs8304.ticketory.feature.screen.Screen;
import com.gudrhs8304.ticketory.feature.point.PricingKind;
import com.gudrhs8304.ticketory.feature.screen.ScreenRepository;
import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final PricingRuleRepository pricingRuleRepository;
    private final ScreenRepository screenRepository;

    /**
     * 관객군별 단가 계산 (basePrice 원단위, BigDecimal)
     * audienceType: "ADULT" / "TEEN" (대소문자 구분 없음)
     */
    public BigDecimal calcUnitPrice(Long screenId, BigDecimal basePrice, String audienceType) {
        List<PricingRule> rules = pricingRuleRepository.findActiveRules(screenId, LocalDateTime.now());

        BigDecimal price = basePrice;
        String t = audienceType == null ? "ALL" : audienceType.toUpperCase();

        for (PricingRule r : rules) {
            if (!appliesTo(r.getKind(), t)) continue;
            price = apply(price, r.getOp(), r.getAmount());
        }
        // 음수가 되지 않도록 방어
        if (price.signum() < 0) price = BigDecimal.ZERO;
        // 소수점 0자리(원) 정규화 (DB scale=2면 필요시 setScale(2))
        return price;
    }

    private boolean appliesTo(PricingKind kind, String t) {
        if (kind == null) return true;

        return switch (kind) {
            case ADULT -> "ADULT".equals(t);
            case TEEN ->  "TEEN".equals(t);
        };
    }

    private BigDecimal apply(BigDecimal price, PricingOp op, BigDecimal amount) {
        if (op == null || amount == null) return price;

        switch (op) {
            case SET:
                return amount;
            case PLUS:
                return price.add(amount);
            case MINUS:
                return price.subtract(amount);
            case PCT_PLUS:
                // amount=10 → +10%  (price * (1 + 10/100))
                return price.add(price.multiply(amount).movePointLeft(2));
            case PCT_MINUS:
                // amount=10 → -10%
                return price.subtract(price.multiply(amount).movePointLeft(2));
            default:
                return price;
        }
    }

    /** 단가 계산: screen.base_price 를 시작점으로, 해당 kind 의 rule 들을 priority 순서대로 적용 */
    public BigDecimal resolveUnit(Long screenId, PricingKind kind, LocalDateTime when) {
        // 1) base price (NULL 이면 0)
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new IllegalArgumentException("screen not found: " + screenId));
        BigDecimal price = screen.getBasePrice() == null
                ? BigDecimal.ZERO
                : new BigDecimal(screen.getBasePrice());

        // 2) kind 에 해당하는 rule 적용
        List<PricingRule> rules = pricingRuleRepository.findEnabledByScreenAt(screenId, when)
                .stream()
                .filter(r -> r.getKind() == null || r.getKind() == kind)
                .toList();

        for (PricingRule r : rules) {
            BigDecimal amt = r.getAmount();
            PricingOp op = r.getOp();
            switch (op) {
                case SET -> price = amt;
                case PLUS -> price = price.add(amt);
                case MINUS -> price = price.subtract(amt);
                case PCT_PLUS -> price = price.multiply(BigDecimal.ONE.add(amt.movePointLeft(2)));
                case PCT_MINUS -> price = price.multiply(BigDecimal.ONE.subtract(amt.movePointLeft(2)));
            }
        }
        // 마이너스 방지 + 반올림
        if (price.signum() < 0) price = BigDecimal.ZERO;
        return price.setScale(0, RoundingMode.HALF_UP); // 원 단위
    }

    /** 여러 인원 종류 합산 금액 계산 */
    public BigDecimal computeTotal(Long screenId,
                                   Map<PricingKind, Integer> counts,
                                   LocalDateTime when) {
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<PricingKind, Integer> e : counts.entrySet()) {
            int n = e.getValue() == null ? 0 : e.getValue();
            if (n <= 0) continue;
            BigDecimal unit = resolveUnit(screenId, e.getKey(), when);
            total = total.add(unit.multiply(BigDecimal.valueOf(n)));
        }
        return total;
    }

    /** 편의: 성인/청소년 숫자로 바로 합산 */
    public BigDecimal computeTotal(Long screenId, int adult, int teen, LocalDateTime when) {
        Map<PricingKind, Integer> m = new EnumMap<>(PricingKind.class);
        m.put(PricingKind.ADULT, adult);
        m.put(PricingKind.TEEN,  teen);
        return computeTotal(screenId, m, when);
    }

    public BigDecimal calcFinalPrice(Long screenId, PricingKind kind, LocalDateTime useAt, BigDecimal base) {
        BigDecimal price = base;
        List<PricingRule> rules = pricingRuleRepository.findActiveFor(screenId, kind, useAt);
        for (var r : rules) {
            switch (r.getOp()) {
                case SET      -> price = r.getAmount();
                case PLUS     -> price = price.add(r.getAmount());
                case MINUS    -> price = price.subtract(r.getAmount());
                case PCT_PLUS -> price = price.multiply(BigDecimal.ONE.add(r.getAmount().movePointLeft(2)));
                case PCT_MINUS-> price = price.multiply(BigDecimal.ONE.subtract(r.getAmount().movePointLeft(2)));
            }
        }
        return price.max(BigDecimal.ZERO);
    }

    /**
     * 예: 6개월치 수요일마다 모든 Kind에 대해 전역 20% 할인(PCT_MINUS)
     * @param from  시작일(포함)
     * @param to    종료일(포함)
     * @param percent 할인 퍼센트(예: 20.00)
     * @param kinds  적용할 Kind (null 또는 빈 배열이면 Enum 전체 사용)
     * @return 생성된 규칙 개수
     */
    @Transactional
    public int upsertGlobalWednesdayDiscount(LocalDate from, LocalDate to, BigDecimal percent,
                                             @Nullable List<PricingKind> kinds) {
        int created = 0;

        // 적용 대상 Kind 목록 구성
        List<PricingKind> targets = (kinds == null || kinds.isEmpty())
                ? Arrays.asList(PricingKind.values())
                : kinds;

        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            if (d.getDayOfWeek() != DayOfWeek.WEDNESDAY) continue;

            LocalDateTime vf = d.atStartOfDay();
            LocalDateTime vt = d.atTime(23, 59, 59);

            for (PricingKind kind : targets) {
                // 필요시 제외할 Kind가 있으면 필터링 (예: UNKNOWN 제외)
                if (kind.name().equalsIgnoreCase("UNKNOWN")) continue;

                boolean exists = pricingRuleRepository.existsOverlappingEnabled(
                        0L, kind, PricingOp.PCT_MINUS, vf, vt
                );
                if (exists) continue;

                PricingRule r = new PricingRule();
                r.setScreenId(0L);                 // 전역
                r.setKind(kind);                   // ALL 대신 각 Kind 별로 한 줄
                r.setOp(PricingOp.PCT_MINUS);
                r.setAmount(percent);              // 예: 20.00
                r.setPriority(50);                 // 관별 규칙 우선순위와 충돌 없게 조정
                r.setValidFrom(vf);
                r.setValidTo(vt);
                r.setEnabled(true);
                r.setCurrency("KRW");              // 스키마상 not null이면 기본값 지정
                pricingRuleRepository.save(r);
                created++;
            }
        }
        return created;
    }
}