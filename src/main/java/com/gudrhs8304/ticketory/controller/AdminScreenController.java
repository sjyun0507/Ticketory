package com.gudrhs8304.ticketory.controller;

import com.gudrhs8304.ticketory.domain.Screen;
import com.gudrhs8304.ticketory.dto.screen.CreateScreenRequest;
import com.gudrhs8304.ticketory.dto.screen.UpdateScreenRequest;
import com.gudrhs8304.ticketory.service.AdminScreenService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/screen")
@RequiredArgsConstructor
public class AdminScreenController {

    private final AdminScreenService adminScreenService;

    @Operation(summary = "상영관 생성 (좌석 레이아웃 포함)")
    @PostMapping
    public ResponseEntity<Screen> create(@RequestBody CreateScreenRequest req) {
        return ResponseEntity.ok(adminScreenService.createScreen(req));
    }

    @Operation(summary = "상영관 수정 (이름/레이아웃 재생성)")
    @PutMapping("/{screenId}")
    public ResponseEntity<Screen> update(@PathVariable Long screenId,
                                         @RequestBody UpdateScreenRequest req) {
        return ResponseEntity.ok(adminScreenService.updateScreen(screenId, req));
    }

    @Operation(summary = "상영관 삭제")
    @DeleteMapping("/{screenId}")
    public ResponseEntity<Void> delete(@PathVariable Long screenId) {
        adminScreenService.deleteScreen(screenId);
        return ResponseEntity.noContent().build();
    }
}