package com.gudrhs8304.ticketory.feature.movie.controller;

import com.gudrhs8304.ticketory.feature.movie.domain.Movie;
import com.gudrhs8304.ticketory.feature.movie.dto.MovieDetailResponseDTO;
import com.gudrhs8304.ticketory.feature.movie.dto.MovieListItemDTO;
import com.gudrhs8304.ticketory.feature.movie.dto.MovieScrollResponseDTO;
import com.gudrhs8304.ticketory.feature.movie.dto.MovieSearchResponseDTO;
import com.gudrhs8304.ticketory.feature.movie.repository.MovieRepository;
import com.gudrhs8304.ticketory.feature.movie.dto.MovieQueryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class MovieController {

    private final MovieQueryService movieQueryService;
    private final MovieRepository movieRepository;

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) Integer page,    // page가 오면 페이지 방식
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Long cursor,     // 없으면 스크롤 방식
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String rating,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        int limit = (size == null || size < 1 || size > 100) ? 24 : size;

        // ✅ 1) 프론트가 page로 호출하면: Page API로 응답(배열만 반환)
        if (page != null) {
            var pg = movieRepository.pageList(
                    emptyToNull(q),
                    emptyToNull(genre),
                    emptyToNull(rating),
                    from, to,
                    PageRequest.of(page, limit)
            );
            var items = pg.getContent().stream().map(MovieListItemDTO::from).toList();
            return ResponseEntity.ok(items);
        }

        // ✅ 2) page 없으면: cursor 기반 스크롤 응답 유지
        MovieScrollResponseDTO dto =
                movieQueryService.getMovies(cursor, limit, q, genre, rating, from, to);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<MovieDetailResponseDTO> detail(@PathVariable Long movieId) {
        var dto = movieQueryService.getMovieDetail(movieId);
        return ResponseEntity.ok(MovieDetailResponseDTO.from(dto));
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    @Operation(summary = "검색어로 영화 검색 (제목 부분일치)", description = "예: /controller/movies/search?q=전독시&page=0&size=24")
    @GetMapping("/search")
    public ResponseEntity<List<MovieSearchResponseDTO>> search(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size
    ) {
        int capped = Math.min(Math.max(size, 1), 50); // 1~50
        Pageable pageable = PageRequest.of(page, capped);

        List<Movie> items = (q == null || q.isBlank())
                ? movieRepository.findAllByOrderByReleaseDateDesc(pageable).getContent()
                : movieRepository.findByTitleContainingIgnoreCase(q.trim(), pageable).getContent();

        List<MovieSearchResponseDTO> dto = items.stream()
                .map(m -> new MovieSearchResponseDTO(
                        m.getMovieId(),
                        m.getTitle(),
                        m.getPosterUrl(),
                        m.getReleaseDate()
                ))
                .toList();

        return ResponseEntity.ok(dto);
    }
}