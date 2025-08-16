package com.gudrhs8304.ticketory.repository;

import com.gudrhs8304.ticketory.domain.MovieMedia;
import com.gudrhs8304.ticketory.domain.enums.MovieMediaType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieMediaRepository extends JpaRepository<MovieMedia, Long> {
    List<MovieMedia> findByMovie_MovieId(Long movieId);
    List<MovieMedia> findByMovie_MovieIdAndMovieMediaType(Long movieId, MovieMediaType type);
}
