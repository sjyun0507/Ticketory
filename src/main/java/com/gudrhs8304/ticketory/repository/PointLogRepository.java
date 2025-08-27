package com.gudrhs8304.ticketory.repository;

import com.gudrhs8304.ticketory.domain.PointLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointLogRepository extends JpaRepository<PointLog, Long> {
    // 필요시: Page<PointLog> findByMember_MemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
}
