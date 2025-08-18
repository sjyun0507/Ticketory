package com.gudrhs8304.ticketory.repository;

import com.gudrhs8304.ticketory.domain.Screening;
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
}
