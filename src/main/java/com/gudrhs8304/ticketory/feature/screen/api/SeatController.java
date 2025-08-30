package com.gudrhs8304.ticketory.feature.screen.api;

import com.gudrhs8304.ticketory.feature.seat.SeatHoldRequestDTO;
import com.gudrhs8304.ticketory.feature.seat.SeatHoldResponseDTO;
import com.gudrhs8304.ticketory.feature.seat.SeatMapResponseDTO;
import com.gudrhs8304.ticketory.feature.seat.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seats")
@CrossOrigin(origins = "http://localhost:5173")
public class SeatController {

    private final SeatService seatService;

    @Operation(summary = "좌석 현황 조회", description = "screeningId 기준으로 AVAILABLE/ 상태를 반환합니다.")
    @GetMapping("/map")
    public ResponseEntity<SeatMapResponseDTO> getSeatMap(@RequestParam Long screeningId) {
        return ResponseEntity.ok(seatService.getSeatMap(screeningId));
    }

    @Operation(summary = "좌석 HOLD 생성", description = "좌석을 임시 점유(PENDING) 상태로 만듭니다.")
    @PostMapping("/hold")
    public ResponseEntity<SeatHoldResponseDTO> createHold(@RequestBody SeatHoldRequestDTO req) {
        return ResponseEntity.ok(seatService.createHold(req));
    }

    @Operation(summary = "좌석 HOLD 연장", description = "holdId의 만료 시각을 연장합니다.")
    @PatchMapping("/hold/{holdId}")
    public ResponseEntity<SeatHoldResponseDTO> extendHold(@PathVariable Long holdId,
                                                       @RequestParam(required = false) Integer holdSeconds) {
        return ResponseEntity.ok(seatService.extendHold(holdId, holdSeconds));
    }

    @Operation(summary = "좌석 HOLD 해제", description = "임시 점유를 해제합니다.")
    @DeleteMapping("/hold/{holdId}")
    public ResponseEntity<Void> releaseHold(@PathVariable Long holdId) {
        seatService.releaseHold(holdId);
        return ResponseEntity.noContent().build();
    }
}
