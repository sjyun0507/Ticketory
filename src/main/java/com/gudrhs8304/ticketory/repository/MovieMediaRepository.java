package com.gudrhs8304.ticketory.repository;

import com.gudrhs8304.ticketory.domain.MovieMedia;
import com.gudrhs8304.ticketory.domain.enums.MovieMediaType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MovieMediaRepository extends JpaRepository<MovieMedia, Long> {
    List<MovieMedia> findByMovie_MovieId(Long movieId);
    List<MovieMedia> findByMovie_MovieIdAndMovieMediaType(Long movieId, MovieMediaType type);
    List<MovieMedia> findByMovie_MovieIdAndMovieMediaTypeIn(Long movieId, Collection<MovieMediaType> types);

    @Query("""
        select m
        from MovieMedia m
        where m.movie.movieId = :movieId
          and m.movieMediaType = :type
        order by m.mediaId asc
    """)
    List<MovieMedia> findOldestOneByMovieAndType(
            @Param("movieId") Long movieId,
            @Param("type") MovieMediaType type,
            Pageable pageable
    );}
