package com.gudrhs8304.ticketory.repository;

import com.gudrhs8304.ticketory.domain.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findBySeatIdIn(List<Long> seatIds);
}
