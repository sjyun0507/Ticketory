package com.gudrhs8304.ticketory.repository;

import com.gudrhs8304.ticketory.domain.SeatHold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {
    boolean existsByScreening_ScreeningIdAndExpiresAtAfter(Long screeningId, LocalDateTime now);
}

