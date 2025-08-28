package com.gudrhs8304.ticketory.controller;

import com.gudrhs8304.ticketory.domain.Screen;
import com.gudrhs8304.ticketory.dto.screen.CreateScreenRequest;
import com.gudrhs8304.ticketory.dto.screen.UpdateScreenRequest;
import com.gudrhs8304.ticketory.repository.ScreenRepository;
import com.gudrhs8304.ticketory.service.AdminScreenService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/screen")
@RequiredArgsConstructor
public class AdminScreenController {

    private final AdminScreenService adminScreenService;
    private final ScreenRepository screenRepository;

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

    @GetMapping
    public ResponseEntity<List<ScreenSimpleDTO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "200") int size
    ) {
        // 단순 전체 조회(정렬 포함) or 페이지네이션
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "screenId"));
        var pageRes = screenRepository.findAll(pageable)
                .map(s -> new ScreenSimpleDTO(
                        s.getScreenId(),
                        s.getName(),
                        s.getLocation(),
                        s.getRowCount(),
                        s.getColCount()
                ));
        return ResponseEntity.ok(pageRes.getContent());
    }

    // 화면에서 쓰는 간단 DTO
    public record ScreenSimpleDTO(
            Long screenId,
            String name,
            String location,
            Integer rowCount,
            Integer colCount
    ) {}
}