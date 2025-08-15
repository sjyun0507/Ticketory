package com.gudrhs8304.ticketory.repository;

import com.gudrhs8304.ticketory.domain.Booking;
import com.gudrhs8304.ticketory.dto.booking.BookingSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // (1) 예매 요약 페이지 (JPQL DTO 프로젝션)
    @Query(
            value = """
        select new com.gudrhs8304.ticketory.dto.booking.BookingSummaryDTO(
            b.bookingId,
            m.title,
            sc.startAt,
            sc.endAt,
            s.name,
            s.location,
            b.totalPrice,
            b.paymentStatus
        )
        from Booking b
          join b.screening sc
          join sc.movie m
          join sc.screen s
        where b.member.memberId = :memberId
        order by b.createdAt desc
        """,
            countQuery = """
        select count(b)
        from Booking b
        where b.member.memberId = :memberId
        """
    )
    Page<BookingSummaryDTO> findSummaryPageByMemberId(@Param("memberId") Long memberId,
                                                      Pageable pageable);


    // (2) 좌석 라벨 일괄 조회
    //  - 파라미터 이름을 bookingIds 로 통일
    //  - 숫자 컬럼 결합 시 JPQL concat 타입 캐스팅 문제 방지: function('concat', ...)
    @Query("""
        select bs.booking.bookingId,
               function('concat', s.rowLabel, s.colNumber)
        from BookingSeat bs
          join bs.seat s
        where bs.booking.bookingId in :bookingIds
        """)
    List<Object[]> findSeatLabelsByBookingIds(@Param("bookingIds") List<Long> bookingIds);

    @Modifying
    @Query("update Booking b set b.member = null where b.member.memberId = :memberId")
    int clearMemberByMemberId(@Param("memberId") Long memberId);

    Optional<Booking> findByBookingIdAndMember_MemberId(Long bookingId, Long memberId);
}