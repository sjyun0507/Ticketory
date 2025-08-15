package com.gudrhs8304.ticketory.repository;

import com.gudrhs8304.ticketory.domain.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {
    boolean existsByScreening_ScreeningIdAndSeat_SeatId(Long screeningId, Long seatId);
    // 예약에 묶인 좌석들 조회
    List<BookingSeat> findByBooking_BookingId(Long bookingId);

    // 묶인 좌석 행들 일괄 삭제
    @Modifying
    @Query("delete from BookingSeat bs where bs.booking.bookingId = :bookingId")
    void deleteByBookingId(@Param("bookingId") Long bookingId);
}
