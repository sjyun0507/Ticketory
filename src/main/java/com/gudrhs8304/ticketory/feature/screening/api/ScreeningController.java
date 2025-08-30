package com.gudrhs8304.ticketory.feature.screening.api;

import com.gudrhs8304.ticketory.feature.screening.dto.ScreeningDetailResponseDTO;
import com.gudrhs8304.ticketory.feature.screening.dto.ScreeningListResponseDTO;
import com.gudrhs8304.ticketory.feature.screening.ScreeningQueryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/screenings")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ScreeningController {

    private final ScreeningQueryService screeningQueryService;
    @Operation(
            summary = "날짜별 상영 목록 조회",
            description = "특정 영화, 날짜, 상영관, 지역 조건에 맞는 상영 목록을 조회합니다. " +
                    "필터는 선택적으로 적용할 수 있으며, 기본 페이지네이션(page, size) 지원합니다."
    )
    @GetMapping
    public ResponseEntity<ScreeningListResponseDTO> list(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false, name = "screenId") Long screenId,   // ✅ theaterId → screenId
            @RequestParam(required = false, name = "region") String region,     // ✅ Screen.location 사용
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "200") int size
    ) {
        var dto = screeningQueryService.getScreenings(movieId, date, screenId, region, page, size);
        return ResponseEntity.ok(dto);
    }

    @Operation(
            summary = "상영 상세 조회",
            description = "상영 ID로 특정 상영의 상세 정보를 조회합니다. " +
                    "영화 정보, 상영관 정보, 시간, 기본 요금 등 메타 데이터를 포함합니다."
    )
    @GetMapping("/{screeningId}")
    public ResponseEntity<ScreeningDetailResponseDTO> getDetail(@PathVariable Long screeningId) {
        return ResponseEntity.ok(screeningQueryService.getScreeningDetail(screeningId));
    }
}