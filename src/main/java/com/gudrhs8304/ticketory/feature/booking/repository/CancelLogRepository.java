package com.gudrhs8304.ticketory.feature.booking.repository;

import com.gudrhs8304.ticketory.feature.booking.domain.CancelLog;
import com.gudrhs8304.ticketory.feature.admin.dto.CancelLogRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CancelLogRepository extends JpaRepository<CancelLog, Long> {


    @Query("""
select new com.gudrhs8304.ticketory.feature.admin.dto.CancelLogRes(
    c.cancelId,
    b.bookingId,
    p.paymentId,
    coalesce(r.refundAmount, p.amount),
    p.status,
    c.reason,
    r.pgRefundTid,
    r.createdAt,
    b.bookingTime,
    c.createdAt,
    cmember.memberId,
    cadmin.memberId,
    coalesce(cadmin.email, cadmin.name)
)
from CancelLog c
left join c.booking b
left join Payment p on p.booking = b
left join com.gudrhs8304.ticketory.feature.refund.domain.RefundLog r
       on r.paymentId = p.paymentId
      and r.createdAt = (
           select max(r2.createdAt)
           from com.gudrhs8304.ticketory.feature.refund.domain.RefundLog r2
           where r2.paymentId = p.paymentId
      )
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
