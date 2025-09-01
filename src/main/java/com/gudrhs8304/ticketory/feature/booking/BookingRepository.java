package com.gudrhs8304.ticketory.feature.booking;

import com.gudrhs8304.ticketory.feature.booking.domain.Booking;
import com.gudrhs8304.ticketory.feature.booking.dto.BookingSummaryDTO;
import com.gudrhs8304.ticketory.feature.story.dto.EligibleBookingRes;
import com.gudrhs8304.ticketory.mail.dto.BookingAlarmDTO;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // (1) 예매 요약 페이지 (JPQL DTO 프로젝션)
    @Query("""
select new com.gudrhs8304.ticketory.feature.booking.dto.BookingSummaryDTO(
    b.bookingId, m.title, sc.startAt, sc.endAt,
    s.name, s.location,
    b.totalPrice, b.paymentStatus,
    m.posterUrl
)
from Booking b
join b.screening sc
join sc.movie m
join sc.screen s
where b.member.memberId = :memberId
  and b.paymentStatus = com.gudrhs8304.ticketory.feature.booking.BookingPayStatus.PAID
order by b.createdAt desc
""")
    Page<BookingSummaryDTO> findSummaryPageByMemberId(@Param("memberId") Long memberId,
                                                      Pageable pageable);

    // (1-A) 예매 요약 페이지 (전체: 상태 제한 없음)
    @Query("""
select new com.gudrhs8304.ticketory.feature.booking.dto.BookingSummaryDTO(
    b.bookingId, m.title, sc.startAt, sc.endAt,
    s.name, s.location,
    b.totalPrice, b.paymentStatus,
    m.posterUrl
)
from Booking b
join b.screening sc
join sc.movie m
join sc.screen s
where b.member.memberId = :memberId
order by b.createdAt desc
""")
    Page<BookingSummaryDTO> findSummaryPageByMemberIdAll(@Param("memberId") Long memberId,
                                                         Pageable pageable);

    // (1-B) 예매 요약 페이지 (지정 상태만)
    @Query("""
select new com.gudrhs8304.ticketory.feature.booking.dto.BookingSummaryDTO(
    b.bookingId, m.title, sc.startAt, sc.endAt,
    s.name, s.location,
    b.totalPrice, b.paymentStatus,
    m.posterUrl
)
from Booking b
join b.screening sc
join sc.movie m
join sc.screen s
where b.member.memberId = :memberId
  and b.paymentStatus = :status
order by b.createdAt desc
""")
    Page<BookingSummaryDTO> findSummaryPageByMemberIdAndPaymentStatus(@Param("memberId") Long memberId,
                                                                      @Param("status") BookingPayStatus status,
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

    boolean existsByScreening_ScreeningId(Long screeningId);

    Optional<Booking> findTopByMember_MemberIdAndPaymentStatusOrderByCreatedAtDesc(
            Long memberId,
            BookingPayStatus paymentStatus
    );

    // 알람을 보낸적이 없고, 상영시간이 thisTime보다 작거나 같은 예약을 들고옴.
    @Query("""
select new com.gudrhs8304.ticketory.mail.dto.BookingAlarmDTO(
    b.bookingId,
    m2.loginId,
    m.title,
    s.startAt,
    b.qrCodeUrl,
    (
      select mm.url
      from MovieMedia mm
      where mm.movie = m
        and mm.movieMediaType = com.gudrhs8304.ticketory.feature.movie.MovieMediaType.POSTER
        and mm.mediaId = (
          select min(mm2.mediaId)
          from MovieMedia mm2
          where mm2.movie = m
            and mm2.movieMediaType = com.gudrhs8304.ticketory.feature.movie.MovieMediaType.POSTER
        )
    )
)
from Booking b
join b.screening s
join s.movie m
left join b.member m2
where (b.isSendAlarm = false or b.isSendAlarm is null)
  and s.startAt <= :thisTime
  and m2.loginId is not null
""")
    List<BookingAlarmDTO> findBookingAlarmDTO(@Param("thisTime") LocalDateTime thisTime);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
        update Booking b
           set b.isSendAlarm = :isSendAlarm
         where b.bookingId   = :bookingId
    """)
    int updateIsSendAlarm(@Param("bookingId") Long bookingId,
                          @Param("isSendAlarm") boolean isSendAlarm);

    @Query(
            value = """
        SELECT 
          b.booking_id           AS bookingId,
          s.screening_id         AS screeningId,
          m.movie_id             AS movieId,
          m.title                AS movieTitle,
          s.start_at             AS startAt,
          s.end_at               AS endAt,
          p.amount               AS paidAmount
        FROM booking b
        JOIN screening s ON s.screening_id = b.screening_id
        JOIN payment   p ON p.booking_id   = b.booking_id
        JOIN movie     m ON m.movie_id     = s.movie_id
        WHERE b.member_id = :memberId
          AND s.end_at < NOW(6)
          AND p.status = 'PAID'
          AND p.cancelled_at IS NULL
        ORDER BY s.end_at DESC
      """,
            countQuery = """
        SELECT COUNT(*)
        FROM booking b
        JOIN screening s ON s.screening_id = b.screening_id
        JOIN payment   p ON p.booking_id   = b.booking_id
        WHERE b.member_id = :memberId
          AND s.end_at < NOW(6)
          AND p.status = 'PAID'
          AND p.cancelled_at IS NULL
      """,
            nativeQuery = true
    )
    Page<Object[]> findEligibleBookingRows(@Param("memberId") Long memberId, Pageable pageable);

    /**
     * 스토리 작성 가능: 상영 종료 && 결제취소 아님 (PAID만 허용)
     */
    @Query("""
        select new com.gudrhs8304.ticketory.feature.booking.dto.BookingSummaryDTO(
            b.bookingId, m.title,
            sc.startAt, sc.endAt,
            s.name, s.location,
            b.totalPrice, b.paymentStatus, m.posterUrl
        )
        from Booking b
        join b.screening sc
        join sc.movie m
        join sc.screen s
        where b.member.memberId = :memberId
          and sc.endAt < :now
          and b.paymentStatus = com.gudrhs8304.ticketory.feature.booking.BookingPayStatus.PAID
        order by sc.endAt desc
    """)
    Page<BookingSummaryDTO> findEligibleForStory(@Param("memberId") Long memberId,
                                                 @Param("now") LocalDateTime now,
                                                 Pageable pageable);

    // 마지막 관람(가장 최근 endAt)
    @Query("""
        select max(sc.endAt)
          from Booking b
          join b.screening sc
         where b.member.memberId = :memberId
           and b.paymentStatus = com.gudrhs8304.ticketory.feature.booking.BookingPayStatus.PAID
    """)
    LocalDateTime findLastWatchedAt(@Param("memberId") Long memberId);

    @Query("""
        select new com.gudrhs8304.ticketory.feature.story.dto.EligibleBookingRes(
            b.bookingId,
            m.movieId,
            m.title,
            sc.startAt,
            sc.endAt,
            s.name,
            b.paymentStatus,
            case when exists (
                 select 1 from com.gudrhs8304.ticketory.feature.story.Story st
                  where st.booking.bookingId = b.bookingId
                    and st.status = 'ACTIVE'
            ) then true else false end
        )
        from com.gudrhs8304.ticketory.feature.booking.domain.Booking b
          join b.screening sc
          join sc.screen s
          join sc.movie m
        where b.member.memberId = :memberId
          and b.paymentStatus <> com.gudrhs8304.ticketory.feature.booking.BookingPayStatus.CANCELLED
          and sc.endAt < :now
        """)
    Page<EligibleBookingRes> findEligibleBookings(
            @Param("memberId") Long memberId,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Query("""
        select
            b.bookingId,
            m.movieId,
            m.title,
            s.startAt,
            s.endAt,
            sc.name,
            b.paymentStatus,
            exists(
                select 1 from Story st
                 where st.booking.bookingId = b.bookingId
                   and st.status <> 'DELETED'
            ) as hasStory
        from Booking b
          join b.screening s
          join s.screen sc
          join s.movie m
        where b.member.memberId = :memberId
          and s.endAt < :now
          and b.paymentStatus <> com.gudrhs8304.ticketory.feature.booking.BookingPayStatus.CANCELLED
        order by s.endAt desc
    """)
    Page<Object[]> findEligibleBookingRows(
            @Param("memberId") Long memberId,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "member")
    Optional<Booking> findWithMemberByBookingId(Long bookingId);

    @EntityGraph(attributePaths = {"screening", "member"})
    Optional<Booking> findWithScreeningAndMemberByBookingId(Long bookingId);
}