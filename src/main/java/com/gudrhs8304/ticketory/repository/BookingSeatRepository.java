package com.gudrhs8304.ticketory.repository;

import com.gudrhs8304.ticketory.domain.BookingSeat;
import com.gudrhs8304.ticketory.domain.enums.BookingPayStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {
    boolean existsByScreening_ScreeningIdAndSeat_SeatId(Long screeningId, Long seatId);
    // 예약에 묶인 좌석들 조회
    List<BookingSeat> findByBooking_BookingId(Long bookingId);

    // 묶인 좌석 행들 일괄 삭제
    @Modifying
    @Query("delete from BookingSeat bs where bs.booking.bookingId = :bookingId")
    void deleteByBookingId(@Param("bookingId") Long bookingId);

    // 단순: 해당 회차에 이미 예약좌석이 있으면 '확정'으로 간주
    @Query("select bs.seat.seatId from BookingSeat bs where bs.screening.screeningId = ?1")
    Set<Long> findSeatIdsByScreeningPaidOrExists(Long screeningId);


    // 상영 회차에서 이미 '결제완료(PAID)' 된 좌석 ID 집합
    @Query("""
        select bs.seat.seatId
        from BookingSeat bs
        join bs.booking b
        where bs.screening.screeningId = :screeningId
          and b.paymentStatus = com.gudrhs8304.ticketory.domain.enums.BookingPayStatus.PAID
    """)
    Set<Long> findSeatIdsByScreeningPaid(@Param("screeningId") Long screeningId);

    // 여러 상태를 한 번에 보고 싶다면 이렇게 '하나의 파라미터'로 IN 절을 쓰세요.
    @Query("""
        select bs.seat.seatId
        from BookingSeat bs
        join bs.booking b
        where bs.screening.screeningId = :screeningId
          and b.paymentStatus in :statuses
    """)
    Set<Long> findSeatIdsByScreeningAndStatuses(
            @Param("screeningId") Long screeningId,
            @Param("statuses") Collection<BookingPayStatus> statuses
    );

    // booking_id로 모든 BookingSeat 행 삭제
    void deleteByBooking_BookingId(Long bookingId);


}
