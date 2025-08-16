package com.gudrhs8304.ticketory.controller;

import com.gudrhs8304.ticketory.domain.MovieMedia;
import com.gudrhs8304.ticketory.domain.enums.MovieMediaType;
import com.gudrhs8304.ticketory.dto.movie.MovieMediaResponseDTO;
import com.gudrhs8304.ticketory.dto.movie.TrailerCreateRequestDTO;
import com.gudrhs8304.ticketory.service.AdminMovieMediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Movie Media")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMovieMediaController {

    private final AdminMovieMediaService service;

    // 이미지 업로드 (POSTER/STILL/OTHER)
    @Operation(summary = "영화 이미지 업로드 (포스터/스틸/기타)")
    @PostMapping(path = "/movies/{movieId}/media/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MovieMediaResponseDTO> uploadImage(
            @PathVariable Long movieId,
            @RequestParam MovieMediaType type,                 // POSTER/STILL/OTHER
            @RequestParam(required = false) String description,
            @RequestPart("file") MultipartFile file
    ) {
        if (type == MovieMediaType.TRAILER) {
            return ResponseEntity.badRequest().build();
        }
        MovieMedia m = service.uploadImage(movieId, type, file, description);
        return ResponseEntity.ok(toRes(m));
    }

    // 트레일러 URL 등록
    @Operation(summary = "영화 트레일러(URL) 등록")
    @PostMapping(path = "/movies/{movieId}/media/trailer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MovieMediaResponseDTO> addTrailer(
            @PathVariable Long movieId,
            @Valid @RequestBody TrailerCreateRequestDTO req
    ) {
        MovieMedia m = service.addTrailerUrl(movieId, req.url(), req.description());
        return ResponseEntity.ok(toRes(m));
    }

    // 목록 조회 (type 필터 optional)
    @Operation(summary = "영화 미디어 목록 조회 (type 선택)")
    @GetMapping("/movies/{movieId}/media")
    public ResponseEntity<List<MovieMediaResponseDTO>> list(
            @PathVariable Long movieId,
            @RequestParam(required = false) MovieMediaType type
    ) {
        return ResponseEntity.ok(service.list(movieId, type).stream().map(this::toRes).toList());
    }

    // 삭제
    @Operation(summary = "영화 미디어 삭제")
    @DeleteMapping("/media/{mediaId}")
    public ResponseEntity<Void> delete(@PathVariable Long mediaId) {
        service.delete(mediaId);
        return ResponseEntity.noContent().build();
    }

    private MovieMediaResponseDTO toRes(MovieMedia m) {
        return new MovieMediaResponseDTO(
                m.getMediaId(),
                m.getMovie().getMovieId(),
                m.getMovieMediaType(),
                m.getUrl(),
                m.getDescription()
        );
    }
}
