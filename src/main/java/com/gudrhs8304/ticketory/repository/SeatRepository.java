package com.gudrhs8304.ticketory.repository;

import com.gudrhs8304.ticketory.domain.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findBySeatIdIn(List<Long> seatIds);
    void deleteByScreen_ScreenId(Long screenId);
    long countByScreen_ScreenId(Long screenId);

    // 벌크 삭제를 확실히 수행하고 1차 캐시도 비워주기
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Seat s where s.screen.screenId = :screenId")
    void deleteByScreenId(@Param("screenId") Long screenId);

    // 안전장치(선택): 중복 체크용
    boolean existsByScreen_ScreenIdAndRowLabelAndColNumber(Long screenId, String rowLabel, Integer colNumber);
}
