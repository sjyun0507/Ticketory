package com.gudrhs8304.ticketory.feature.refund;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefundLogRepository extends JpaRepository<RefundLog, Long> {
    Page<RefundLog> findByPaymentId(Long paymentId, Pageable pageable);


    Optional<RefundLog> findFirstByPaymentIdOrderByCreatedAtDesc(Long paymentId);

    @Query("""
      select coalesce(sum(r.refundAmount), 0)
        from RefundLog r
       where r.status = com.gudrhs8304.ticketory.feature.refund.RefundStatus.DONE
         and r.createdAt >= :from and r.createdAt < :to
    """)
    Integer sumDoneRefunds(LocalDateTime from, LocalDateTime to);
}
