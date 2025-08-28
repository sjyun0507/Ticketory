package com.gudrhs8304.ticketory.repository;

import com.gudrhs8304.ticketory.domain.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    Page<Movie> findByStatus(Boolean status, Pageable pageable);

    Optional<Movie> findByMovieIdAndDeletedAtIsNull(Long movieId);

    @Query("""
            select m from Movie m
            where (:afterId is null or m.movieId < :afterId)
              and (:genre   is null or m.genre   = :genre)
              and (:rating  is null or m.rating  = :rating)
              and (:dateFrom is null or m.releaseDate >= :dateFrom)
              and (:dateTo   is null or m.releaseDate <= :dateTo)
              and (
                   :q is null
                or m.title    like concat('%', :q, '%')
                or m.director like concat('%', :q, '%')
                or coalesce(m.actors,  '') like concat('%', :q, '%')
                or coalesce(m.summary, '') like concat('%', :q, '%')
              )
            order by m.movieId desc
            """)
    List<Movie> scroll(
            @Param("afterId") Long afterId,
            @Param("q") String q,
            @Param("genre") String genre,
            @Param("rating") String rating,
            @Param("datefrom") LocalDate from,
            @Param("dateTo") LocalDate to,
            Pageable pageable
    );

    // MovieRepository
    @Query("""
            select m
            from Movie m
            where m.deletedAt is null
              and (:q is null or m.title like concat('%', :q, '%'))
              and (:genre is null or m.genre = :genre)
              and (:rating is null or m.rating = :rating)
              and (:dateFrom is null or m.releaseDate >= :dateFrom)
              and (:dateTo is null or m.releaseDate <= :dateTo)
            order by m.movieId desc
            """)
    org.springframework.data.domain.Page<Movie> pageList(
            @org.springframework.data.repository.query.Param("q") String q,
            @org.springframework.data.repository.query.Param("genre") String genre,
            @org.springframework.data.repository.query.Param("rating") String rating,
            @org.springframework.data.repository.query.Param("dateFrom") java.time.LocalDate dateFrom,
            @org.springframework.data.repository.query.Param("dateTo") java.time.LocalDate dateTo,
            org.springframework.data.domain.Pageable pageable
    );

    // 제목 부분일치(대소문자 무시)
    Page<Movie> findByTitleContainingIgnoreCase(String q, Pageable pageable);

    // 전체 조회용(검색어 비었을 때 최신순)
    Page<Movie> findAllByOrderByReleaseDateDesc(Pageable pageable);

    List<Movie> findByStatusTrue();

}
