package com.gudrhs8304.ticketory.service;

import com.gudrhs8304.ticketory.domain.Movie;
import com.gudrhs8304.ticketory.dto.movie.*;
import com.gudrhs8304.ticketory.repository.MovieRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminMovieService {

    private final MovieRepository movieRepository;

    @Transactional
    public MovieResponseDTO create(MovieCreateRequestDTO req) {
        Movie m = Movie.builder()
                .title(req.title())
                .summary(req.summary())
                .genre(req.genre())
                .rating(req.rating())
                .runningMinutes(req.runningMinutes())
                .releaseDate(req.releaseDate())
                .status(req.status() != null ? req.status() : Boolean.FALSE)
                .actors(req.actors())
                .director(req.director())
                .build();
        m = movieRepository.save(m);
        return toRes(m);
    }


    public MovieResponseDTO get(Long movieId) {
        return toRes(find(movieId));
    }

    @Transactional
    public MovieResponseDTO put(Long movieId, MovieUpdateRequestDTO req) {
        Movie m = find(movieId);
        m.setTitle(req.title());
        m.setSummary(req.summary());
        m.setGenre(req.genre());
        m.setRating(req.rating());
        m.setRunningMinutes(req.runningMinutes());
        m.setReleaseDate(req.releaseDate());
        m.setStatus(req.status() != null ? req.status() : Boolean.FALSE);
        m.setActors(req.actors());
        m.setDirector(req.director());
        return toRes(m);
    }

    @Transactional
    public MovieResponseDTO patch(Long movieId, MoviePatchRequestDTO req) {
        Movie m = find(movieId);
        if (req.title() != null && !req.title().isBlank()) m.setTitle(req.title());
        if (req.summary() != null) m.setSummary(req.summary());
        if (req.genre() != null) m.setGenre(req.genre());
        if (req.rating() != null) m.setRating(req.rating());
        if (req.runningMinutes() != null) m.setRunningMinutes(req.runningMinutes());
        if (req.releaseDate() != null) m.setReleaseDate(req.releaseDate());
        if (req.status() != null) m.setStatus(req.status());
        if (req.actors() != null) m.setActors(req.actors());
        if (req.director() != null) m.setDirector(req.director());
        return toRes(m);
    }

    @Transactional
    public void delete(Long movieId) {
        movieRepository.deleteById(movieId); // 하드 삭제
    }

    // ===== helper =====
    private Movie find(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("영화를 찾을 수 없습니다. id=" + id));
    }

    private MovieResponseDTO toRes(Movie m) {
        return new MovieResponseDTO(
                m.getMovieId(), m.getTitle(), m.getSummary(), m.getGenre(), m.getRating(),
                m.getRunningMinutes(), m.getReleaseDate(), m.getStatus(), m.getActors(), m.getDirector()
        );
    }

    @Transactional(readOnly = true)
    public Page<MovieResponseDTO> getMovies(Boolean status, Pageable pageable) {
        return (status == null
                ? movieRepository.findAll(pageable)
                : movieRepository.findByStatus(status, pageable))
                .map(this::toRes);
    }
}
