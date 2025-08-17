package com.gudrhs8304.ticketory.controller;

import com.gudrhs8304.ticketory.dto.movie.MovieDetailResponseDTO;
import com.gudrhs8304.ticketory.dto.movie.MovieListItemDTO;
import com.gudrhs8304.ticketory.dto.movie.MovieScrollResponseDTO;
import com.gudrhs8304.ticketory.repository.MovieRepository;
import com.gudrhs8304.ticketory.service.MovieQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class MovieController {

    private final MovieQueryService movieQueryService;
    private final MovieRepository movieRepository; // ✅ 페이지네이션용 직접 호출

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) Integer page,    // ✅ page가 오면 페이지 방식
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Long cursor,     // ✅ 없으면 스크롤 방식
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
}