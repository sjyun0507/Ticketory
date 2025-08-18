package com.gudrhs8304.ticketory.controller;

import com.gudrhs8304.ticketory.dto.screening.admin.ScreeningAdminListItemDTO;
import com.gudrhs8304.ticketory.dto.screening.admin.ScreeningAdminResponseDTO;
import com.gudrhs8304.ticketory.dto.screening.admin.ScreeningUpsertRequestDTO;
import com.gudrhs8304.ticketory.service.AdminScreeningService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/screenings")
@RequiredArgsConstructor
public class AdminScreeningController {

    private final AdminScreeningService service;

    @Operation(summary = "상영 목록(Admin)", description = "관리자용 상영 목록을 페이지네이션으로 조회합니다.")
    @GetMapping
    public ResponseEntity<Page<ScreeningAdminListItemDTO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(service.list(page, size));
    }

    @Operation(summary = "상영 생성", description = "영화/상영관/시작/종료 시간을 받아 상영을 생성합니다. 시간 겹침을 검증합니다.")
    @PostMapping
    public ResponseEntity<ScreeningAdminResponseDTO> create(@RequestBody ScreeningUpsertRequestDTO req) {
        return ResponseEntity.ok(service.create(req));
    }

    @Operation(summary = "상영 수정", description = "상영 ID 기준으로 영화/상영관/시간을 수정합니다. 시간 겹침을 검증합니다.")
    @PutMapping("/{screeningId}")
    public ResponseEntity<ScreeningAdminResponseDTO> update(
            @PathVariable Long screeningId,
            @RequestBody ScreeningUpsertRequestDTO req
    ) {
        return ResponseEntity.ok(service.update(screeningId, req));
    }

    @Operation(summary = "상영 삭제", description = "예약/좌석홀드 존재 시 삭제 불가.")
    @DeleteMapping("/{screeningId}")
    public ResponseEntity<Void> delete(@PathVariable Long screeningId) {
        service.delete(screeningId);
        return ResponseEntity.noContent().build();
    }
}
