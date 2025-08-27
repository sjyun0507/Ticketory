package com.gudrhs8304.ticketory.controller;

import com.gudrhs8304.ticketory.dto.point.PointLogDTO;
import com.gudrhs8304.ticketory.domain.enums.PointChangeType;
import com.gudrhs8304.ticketory.service.PointQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class PointController {

    private final PointQueryService pointQueryService;

    @GetMapping("/{memberId}/points")
    public ResponseEntity<Page<PointLogDTO>> getPointLogs(
            @PathVariable Long memberId,
            @RequestParam(required = false) LocalDate from,         // 예: ?from=2025-08-01
            @RequestParam(required = false) LocalDate to,           // 예: ?to=2025-08-31
            @RequestParam(required = false) List<PointChangeType> type, // 예: ?type=EARN&type=USE
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth
    ) {
        // 본인 또는 관리자만
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        Long me = extractMemberId(auth);
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (!isAdmin && !memberId.equals(me)) {
            return ResponseEntity.status(403).build();
        }

        Page<PointLogDTO> result = pointQueryService.getMemberPointLogs(memberId, from, to, type, page, size);
        return ResponseEntity.ok(result);
    }

    private Long extractMemberId(Authentication auth) {
        // 프로젝트에서 이미 쓰는 방식과 동일하게 구현 (예: JWT의 sub, 커스텀 Principal 등)
        Object principal = auth.getPrincipal();
        // TODO: 실제 구현에 맞춰 파싱
        // 예시:
        try {
            return Long.valueOf(auth.getName());
        } catch (Exception e) {
            return null;
        }
    }
}
