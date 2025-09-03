package com.gudrhs8304.ticketory.feature.refund.repository;

import com.gudrhs8304.ticketory.feature.refund.domain.RefundLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefundLogRepository extends JpaRepository<RefundLog, Long> {

}
