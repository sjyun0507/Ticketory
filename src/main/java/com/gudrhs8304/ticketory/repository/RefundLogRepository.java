package com.gudrhs8304.ticketory.repository;

import com.gudrhs8304.ticketory.domain.RefundLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundLogRepository extends JpaRepository<RefundLog, Long> {
    Page<RefundLog> findByPayment_PaymentId(Long paymentId, Pageable pageable);
}
