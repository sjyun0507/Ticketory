package com.gudrhs8304.ticketory.repository;

import com.gudrhs8304.ticketory.domain.Booking;
import com.gudrhs8304.ticketory.dto.BookingSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // 예매 요약 페이지
    @Query("""
            select new (
            b.bookingId,
            m.title,
            sc.startAt,
            sc.endAt,
            b.totalPrice,
            b.paymentStatus
        )
        from Booking b
          join b.screening sc
          join sc.movie m
        where b.member.memberId = :memberId
        """)
    Page<BookingSummaryDTO> findSummaryPageByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    // (2) 좌석 라벨 일괄 조회 (페이지에 포함된 bookingId들만)
    @Query("""
        select bs.booking.bookingId,
               concat(s.rowLabel, s.colNumber)
        from BookingSeat bs
          join bs.seat s
        where bs.booking.bookingId in :bookingId
        """)
    List<Object[]> findSeatLabelsByBookingIds(@Param("bookingId") List<Long> bookingIds);
}
