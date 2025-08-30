package com.gudrhs8304.ticketory.feature.point;

import com.gudrhs8304.ticketory.feature.point.domain.PointLog;
import com.gudrhs8304.ticketory.feature.member.enums.PointChangeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface PointLogRepository extends JpaRepository<PointLog, Long> {
    // 필요시: Page<PointLog> findByMember_MemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    // 기본: 특정 회원 전체 이력(최신순)
    Page<PointLog> findByMember_MemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    // 기간 & 타입 필터
    Page<PointLog> findByMember_MemberIdAndCreatedAtBetweenAndChangeTypeInOrderByCreatedAtDesc(
            Long memberId, LocalDateTime from, LocalDateTime to,
            Collection<PointChangeType> types,
            Pageable pageable
    );

    // 기간만
    Page<PointLog> findByMember_MemberIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long memberId, LocalDateTime from, LocalDateTime to, Pageable pageable
    );

    // 타입만
    Page<PointLog> findByMember_MemberIdAndChangeTypeInOrderByCreatedAtDesc(
            Long memberId, Collection<PointChangeType> types, Pageable pageable
    );

    @Query("""
        select p from PointLog p
        where p.member.memberId = :memberId
          and (:from is null or p.createdAt >= :from)
          and (:to   is null or p.createdAt <  :to)
          and (:types is null or p.changeType in :types)
        """)
    Page<PointLog> search(
            @Param("memberId") Long memberId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("types") List<PointChangeType> types,
            Pageable pageable
    );
}
