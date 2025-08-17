package com.gudrhs8304.ticketory.service;

import com.gudrhs8304.ticketory.domain.enums.MovieMediaType;
import com.gudrhs8304.ticketory.dto.movie.MovieDetailDTO;
import com.gudrhs8304.ticketory.dto.movie.MovieListItemDTO;
import com.gudrhs8304.ticketory.dto.movie.MovieScrollResponseDTO;
import com.gudrhs8304.ticketory.repository.MovieMediaRepository;
import com.gudrhs8304.ticketory.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieQueryService {

    private final MovieRepository movieRepository;
    private final MovieMediaRepository mediaRepository;

    public MovieScrollResponseDTO getMovies(Long cursor, int limit,
                                            String q, String genre, String rating,
                                            LocalDate from, LocalDate to) {

        var rows = movieRepository.scroll(
                cursor,
                emptyToNull(q),
                emptyToNull(genre),
                emptyToNull(rating),
                from,
                to,
                PageRequest.of(0, limit)
        );

        Long next = rows.isEmpty() ? null : rows.get(rows.size() - 1).getMovieId();

        return MovieScrollResponseDTO.of(
                rows.stream().map(MovieListItemDTO::from).toList(),
                next
        );
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    public MovieDetailDTO getMovieDetail(Long movieId) {
        var movie = movieRepository.findByMovieIdAndDeletedAtIsNull(movieId)
                .orElseThrow(() -> new IllegalArgumentException("영화를 찾을 수 없습니다: " + movieId));

        var stills = mediaRepository
                .findByMovie_MovieIdAndMovieMediaType(movieId, MovieMediaType.STILL)
                .stream()
                .map(mm -> mm.getUrl())
                .toList();

        var trailerUrl = mediaRepository
                .findOldestOneByMovieAndType(movieId, MovieMediaType.TRAILER, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(mm -> mm.getUrl())
                .orElse(null);

        return MovieDetailDTO.builder()
                .movieId(movie.getMovieId())
                .title(movie.getTitle())
                .subtitle(movie.getSubtitle())
                .summary(movie.getSummary())
                .genre(movie.getGenre())
                .rating(movie.getRating())
                .runningMinutes(movie.getRunningMinutes())
                .releaseDate(movie.getReleaseDate())
                .status(movie.getStatus())
                .actors(movie.getActors())
                .director(movie.getDirector())
                .posterUrl(movie.getPosterUrl())
                .stillcutUrls(stills)
                .trailerUrl(trailerUrl)
                .build();
    }
}