package com.gudrhs8304.ticketory.repository;

import com.gudrhs8304.ticketory.domain.PricingRule;
import com.gudrhs8304.ticketory.domain.enums.PricingKind;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {

    /** 상영관 + (옵션)요금종류 + 유효기간까지 한 번에 필터 */
    @Query("""
           select r
             from PricingRule r
            where r.enabled = true
              and r.screenId = :screenId
              and (:kind is null or r.kind = :kind)
              and (r.validFrom is null or r.validFrom <= :now)
              and (r.validTo   is null or :now < r.validTo)
            order by coalesce(r.priority, 100) asc, r.id asc
           """)
    List<PricingRule> findEffectiveRulesByScreen(
            @Param("screenId") Long screenId,
            @Param("kind") PricingKind kind,
            @Param("now") LocalDateTime now
    );

    /** 단순 조회(유효기간은 서비스단에서 필터) */
    List<PricingRule> findByScreenIdAndEnabledTrueOrderByPriorityAscIdAsc(Long screenId);

    List<PricingRule> findByScreenIdAndKindAndEnabledTrueOrderByPriorityAscIdAsc(Long screenId, PricingKind kind);
}