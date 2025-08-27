package com.gudrhs8304.ticketory.repository;

import com.gudrhs8304.ticketory.domain.PointLog;
import com.gudrhs8304.ticketory.domain.enums.PointChangeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;

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
}
