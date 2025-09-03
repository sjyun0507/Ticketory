package com.gudrhs8304.ticketory.feature.seat.repository;


import com.gudrhs8304.ticketory.feature.seat.domain.Seat;
import com.gudrhs8304.ticketory.feature.seat.enums.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findBySeatIdIn(List<Long> seatIds);
    boolean existsByScreen_ScreenId(Long screenId);
    void deleteByScreen_ScreenId(Long screenId);
    long countByScreen_ScreenId(Long screenId);

    // 벌크 삭제를 확실히 수행하고 1차 캐시도 비워주기
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Seat s where s.screen.screenId = :screenId")
    void deleteByScreenId(@Param("screenId") Long screenId);

    // 안전장치(선택): 중복 체크용
    boolean existsByScreen_ScreenIdAndRowLabelAndColNumber(Long screenId, String rowLabel, Integer colNumber);

    @Query("select s from Seat s where s.screen.screenId = :screenId order by s.rowLabel asc, s.colNumber asc")
    List<Seat> findAllByScreenId(@Param("screenId") Long screenId);



    @Query("""
      select (count(s) = :#{#ids.size()})
      from Seat s
      where s.seatId in :ids
        and s.screen.screenId = :screenId
    """)
    boolean allSeatsBelongToScreen(@Param("ids") List<Long> ids,
                                   @Param("screenId") Long screenId);

    @Query("select s from Seat s where s.seatId in ?1")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Seat> lockSeatsForUpdate(List<Long> seatIds);



    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Seat s
           set s.status = :available
         where s.seatId in (
               select bs.seat.seatId
                 from BookingSeat bs
                where bs.booking.bookingId = :bookingId
         )
    """)
    int releaseSeatsByBookingId(@Param("bookingId") Long bookingId,
                                @Param("available") SeatStatus available);

    default int releaseSeatsByBookingId(Long bookingId) {
        return releaseSeatsByBookingId(bookingId, SeatStatus.AVAILABLE);
    }

}
