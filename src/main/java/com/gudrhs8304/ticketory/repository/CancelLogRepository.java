package com.gudrhs8304.ticketory.repository;

import com.gudrhs8304.ticketory.domain.CancelLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CancelLogRepository extends JpaRepository<CancelLog, Long> {
    @Query("""
      select c from CancelLog c
      join fetch c.booking b
      left join fetch c.canceledByMember m
      left join fetch c.canceledByAdmin a
      where (:bookingId is null or b.bookingId = :bookingId)
        and (:memberId  is null or (m.memberId = :memberId or a.memberId = :memberId))
    """)
    Page<CancelLog> search(
            @Param("bookingId") Long bookingId,
            @Param("memberId")  Long memberId,
            Pageable pageable
    );
}
