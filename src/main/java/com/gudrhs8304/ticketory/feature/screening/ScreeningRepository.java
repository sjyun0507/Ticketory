package com.gudrhs8304.ticketory.feature.screening;

import com.gudrhs8304.ticketory.feature.screening.domain.Screening;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {
    long countByScreen_ScreenId(Long screenId);

    @Query("""
        select s
        from Screening s
          join fetch s.movie m
          join fetch s.screen sc
        where (:movieId  is null or m.movieId   = :movieId)
          and (:screenId is null or sc.screenId = :screenId)
          and (:region   is null or sc.location = :region)
          and ( (:dateStart is null and :dateEnd is null)
                or (s.startAt >= :dateStart and s.startAt < :dateEnd) )
        order by s.startAt asc, s.screeningId asc
        """)
    Page<Screening> search(
            @Param("movieId")   Long movieId,
            @Param("screenId")  Long screenId,
            @Param("region")    String region,            // Screen.location
            @Param("dateStart") LocalDateTime dateStart,
            @Param("dateEnd")   LocalDateTime dateEnd,
            Pageable pageable
    );

    @Query("""
      select s from Screening s
      join fetch s.movie
      join fetch s.screen
      """)
    Page<Screening> findAllWithJoins(Pageable pageable);

    @Query("""
      select count(s) > 0 from Screening s
      where s.screen.screenId = :screenId
        and s.screeningId <> COALESCE(:excludeId, -1)
        and s.startAt < :endAt
        and s.endAt   > :startAt
      """)
    boolean existsOverlap(Long screenId, LocalDateTime startAt, LocalDateTime endAt, Long excludeId);

    @Query("select s.screen.screenId from Screening s where s.screeningId = :screeningId")
    Long findScreenIdByScreeningId(@Param("screeningId") Long screeningId);

    /**
     * threshold(예: now+30m) 보다 시작 시간이 이른(= 곧 시작/이미 시작) 상영의 예약을 닫는다.
     * 이미 닫힌(isBooking=false) 건은 제외하고 true -> false 로만 변경.
     * 반환값은 업데이트된 행 수.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
        update Screening s
           set s.isBooking = false
         where s.isBooking = true
           and s.startAt   <= :threshold
    """)
    int updateIsBookingEnd(@Param("threshold") LocalDateTime threshold);

    // 같은 상영관에서 정확히 같은 시작시간 존재 여부
    boolean existsByScreen_ScreenIdAndStartAt(Long screenId, LocalDateTime startAt);

    // 시간 겹침(청소시간 포함) 검사 — 3파라미터 버전
    @Query("""
        select (count(s) > 0) from Screening s
        where s.screen.screenId = :screenId
          and s.startAt < :endAt
          and s.endAt   > :startAt
    """)
    boolean existsOverlap(@Param("screenId") Long screenId,
                          @Param("startAt") LocalDateTime startAt,
                          @Param("endAt")   LocalDateTime endAt);
}
