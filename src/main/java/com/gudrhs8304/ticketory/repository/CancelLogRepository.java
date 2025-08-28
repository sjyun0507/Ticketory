package com.gudrhs8304.ticketory.repository;

import com.gudrhs8304.ticketory.domain.CancelLog;
import com.gudrhs8304.ticketory.dto.admin.CancelLogRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CancelLogRepository extends JpaRepository<CancelLog, Long> {


    @Query("""
    select new com.gudrhs8304.ticketory.dto.admin.CancelLogRes(
        c.cancelId,
        b.bookingId,
        p.paymentId,
        p.amount,
        p.status,
        c.reason,
        null,
        cmember.memberId,
        cadmin.memberId,
        coalesce(cadmin.email, cadmin.name),
        c.createdAt
    )
    from CancelLog c
    left join c.booking b
    left join Payment p on p.booking = b
    left join c.canceledByMember cmember
    left join c.canceledByAdmin  cadmin
    where (:bookingId is null or b.bookingId = :bookingId)
      and (:memberId  is null or cmember.memberId = :memberId)
    order by c.createdAt desc
    """)
    Page<CancelLogRes> search(
            @Param("bookingId") Long bookingId,
            @Param("memberId") Long memberId,
            Pageable pageable
    );
}
