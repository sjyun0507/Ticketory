package com.gudrhs8304.ticketory.feature.movie.controller;

import com.gudrhs8304.ticketory.feature.movie.service.AdminMovieService;
import com.gudrhs8304.ticketory.feature.movie.dto.MovieCreateRequestDTO;
import com.gudrhs8304.ticketory.feature.movie.dto.MoviePatchRequestDTO;
import com.gudrhs8304.ticketory.feature.movie.dto.MovieResponseDTO;
import com.gudrhs8304.ticketory.feature.movie.dto.MovieUpdateRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/movies")
@RequiredArgsConstructor
@Tag(name = "Admin - Movies")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMovieController {

    private final AdminMovieService service;

    @Operation(summary = "영화 목록(Admin) - status 필터 가능")
    @GetMapping
    public Page<MovieResponseDTO> getMovies(
            @RequestParam(required = false) Boolean status,

            // Swagger에서 page/size/sort를 자동으로 분리해서 노출
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return service.getMovies(status, pageable);
    }

    @Operation(summary = "영화 단건 조회")
    @GetMapping("/{movieId}")
    public ResponseEntity<MovieResponseDTO> get(@PathVariable Long movieId) {
        return ResponseEntity.ok(service.get(movieId));
    }

    @Operation(summary = "영화 생성")
    @PostMapping
    public ResponseEntity<MovieResponseDTO> create(@Valid @RequestBody MovieCreateRequestDTO req) {
        return ResponseEntity.ok(service.create(req));
    }

    @Operation(summary = "영화 수정(전체 PUT)")
    @PutMapping("/{movieId}")
    public ResponseEntity<MovieResponseDTO> put(@PathVariable Long movieId,
                                             @Valid @RequestBody MovieUpdateRequestDTO req) {
        return ResponseEntity.ok(service.put(movieId, req));
    }

    @Operation(summary = "영화 부분 수정(PATCH)")
    @PatchMapping("/{movieId}")
    public ResponseEntity<MovieResponseDTO> patch(@PathVariable Long movieId,
                                               @RequestBody MoviePatchRequestDTO req) {
        return ResponseEntity.ok(service.patch(movieId, req));
    }

    @Operation(summary = "영화 삭제(소프트 삭제)")
    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> delete(@PathVariable Long movieId) {
        service.delete(movieId);
        return ResponseEntity.noContent().build();
    }
}
