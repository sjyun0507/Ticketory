package com.gudrhs8304.ticketory.repository;

import com.gudrhs8304.ticketory.domain.CancelLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CancelLogRepository extends JpaRepository<CancelLog, Long> {
}
