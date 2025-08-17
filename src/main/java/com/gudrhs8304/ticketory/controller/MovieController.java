package com.gudrhs8304.ticketory.controller;

import com.gudrhs8304.ticketory.dto.movie.MovieDetailResponseDTO;
import com.gudrhs8304.ticketory.dto.movie.MovieScrollResponseDTO;
import com.gudrhs8304.ticketory.service.MovieQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class MovieController {

    private final MovieQueryService movieQueryService;

    @GetMapping
    public ResponseEntity<MovieScrollResponseDTO> list(
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String rating,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        int limit = (size == null || size < 1 || size > 100) ? 20 : size;

        // 서비스 시그니처에 맞게: cursor 먼저, 그다음 limit
        MovieScrollResponseDTO dto =
                movieQueryService.getMovies(cursor, limit, q, genre, rating, from, to);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<MovieDetailResponseDTO> detail(@PathVariable Long movieId) {
        var dto = movieQueryService.getMovieDetail(movieId); // Service는 MovieDetailDTO 반환
        return ResponseEntity.ok(MovieDetailResponseDTO.from(dto)); // <-- 여기서 ResponseDTO로 변환
    }
}
