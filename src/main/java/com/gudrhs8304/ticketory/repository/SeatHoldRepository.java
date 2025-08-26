package com.gudrhs8304.ticketory.repository;

import com.gudrhs8304.ticketory.domain.SeatHold;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {
    boolean existsByScreening_ScreeningIdAndExpiresAtAfter(Long screeningId, LocalDateTime now);

    @Query("select (count(h) > 0) " +
            "from SeatHold h " +
            "where h.screening.screeningId = ?1 " +
            "and   h.seat.seatId in ?2 " +
            "and   h.expiresAt > ?3")
    boolean existsAnyActiveHold(Long screeningId, List<Long> seatIds, LocalDateTime now);

    @Query("select h from SeatHold h " +
            "where h.screening.screeningId = :screeningId " +
            "and   h.expiresAt > :now")
    List<SeatHold> findActiveByScreening(@Param("screeningId") Long screeningId,
                                         @Param("now") LocalDateTime now);

    @Modifying
    @Query("delete from SeatHold h where h.expiresAt <= :now")
    int deleteExpired(@Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM SeatHold h WHERE h.holdKey = :holdKey")
    int deleteByHoldKey(@Param("holdKey") String holdKey);



    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        DELETE h
          FROM seat_hold h
          JOIN booking_seat bs ON bs.seat_id = h.seat_id
         WHERE bs.booking_id = :bookingId
        """, nativeQuery = true)
    int deleteByBookingId(@Param("bookingId") Long bookingId);




}

