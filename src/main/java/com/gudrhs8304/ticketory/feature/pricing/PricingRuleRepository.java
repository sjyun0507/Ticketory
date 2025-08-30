package com.gudrhs8304.ticketory.feature.pricing;

import com.gudrhs8304.ticketory.feature.pricing.domain.PricingRule;
import com.gudrhs8304.ticketory.feature.member.enums.PricingKind;
import com.gudrhs8304.ticketory.feature.member.enums.PricingOp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {

    /**
     * 상영관 + (옵션)요금종류 + 유효기간까지 한 번에 필터
     */
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

    /**
     * 단순 조회(유효기간은 서비스단에서 필터)
     */
    List<PricingRule> findByScreenIdAndEnabledTrueOrderByPriorityAscIdAsc(Long screenId);

    List<PricingRule> findByScreenIdAndKindAndEnabledTrueOrderByPriorityAscIdAsc(Long screenId, PricingKind kind);

    @Query("""
                select r
                  from PricingRule r
                 where r.screenId = :screenId
                   and r.enabled = true
                   and (:now between coalesce(r.validFrom, :now) and coalesce(r.validTo, :now))
                 order by r.priority asc, r.id asc
            """)
    List<PricingRule> findActiveRules(@Param("screenId") Long screenId,
                                      @Param("now") LocalDateTime now);

    @Query("""
                select p
                  from PricingRule p
                 where p.screenId = :screenId
                   and p.enabled = true
                   and (p.validFrom is null or p.validFrom <= :now)
                   and (p.validTo   is null or p.validTo   >= :now)
                 order by p.priority asc, p.id asc
            """)
    List<PricingRule> findActiveByScreenId(@Param("screenId") Long screenId,
                                           @Param("now") LocalDateTime now);

    @Query("""
              select r
                from PricingRule r
               where r.screenId = :screenId
                 and r.enabled = true
                 and (r.validFrom is null or r.validFrom <= :when)
                 and (r.validTo   is null or r.validTo   >= :when)
               order by r.priority asc, r.id asc
            """)
    List<PricingRule> findEnabledByScreenAt(
            @Param("screenId") Long screenId,
            @Param("when") LocalDateTime when);

    @Query("""
              select r
                from PricingRule r
               where r.screenId = :screenId
                 and r.enabled = true
                 and r.kind = :kind
                 and (r.validFrom is null or r.validFrom <= :when)
                 and (r.validTo   is null or r.validTo   >= :when)
               order by r.priority asc, r.id asc
            """)
    List<PricingRule> findEnabledByScreenAtAndKind(
            @Param("screenId") Long screenId,
            @Param("kind") PricingKind kind,
            @Param("when") LocalDateTime when);

    @Query("""
              select r
              from PricingRule r
              where r.screenId = :screenId
                and r.enabled = true
                and (r.validFrom is null or r.validFrom <= :when)
                and (r.validTo   is null or r.validTo   >= :when)
                and r.kind = :kind
              order by r.priority asc, r.id asc
            """)
    List<PricingRule> findActiveRulesByKind(
            @Param("screenId") Long screenId,
            @Param("kind") PricingKind kind,
            @Param("when") LocalDateTime when
    );

    @Query("""
            select p from PricingRule p
            where p.enabled = true
              and (:screenId is null or p.screenId = :screenId)
              and (:kind is null or p.kind = :kind)
            order by p.priority asc, p.id asc
            """)
    List<PricingRule> findByFilter(@Param("screenId") Long screenId,
                                   @Param("kind") PricingKind kind);

    // 전역(0) + 해당 상영관 둘 다 불러오고, kind 일치 또는 ALL 허용, 기간·활성 필터
    @Query("""
            select r
            from PricingRule r
            where r.enabled = true
              and (r.screenId = :screenId or r.screenId = 0)
              and (:kind is null or r.kind = :kind or r.kind = 'ALL')
              and (r.validFrom is null or r.validFrom <= :at)
              and (r.validTo   is null or r.validTo   >= :at)
            order by r.priority asc, r.id asc
            """)
    List<PricingRule> findActiveFor(@Param("screenId") Long screenId,
                                    @Param("kind") PricingKind kind,
                                    @Param("at") LocalDateTime at);

    // 동일 screen/kind/op 에 대해 기간이 겹치는 enabled 규칙이 있는지
    @Query("""
       select (count(r) > 0) from PricingRule r
       where r.screenId = :sid
         and r.kind = :kind
         and r.op = :op
         and r.enabled = true
         and r.validFrom <= :to
         and (r.validTo is null or r.validTo >= :from)
    """)
    boolean existsOverlappingEnabled(@Param("sid") Long screenId,
                                     @Param("kind") PricingKind kind,
                                     @Param("op") PricingOp op,
                                     @Param("from") LocalDateTime from,
                                     @Param("to") LocalDateTime to);
}
