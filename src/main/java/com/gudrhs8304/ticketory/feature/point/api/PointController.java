package com.gudrhs8304.ticketory.feature.point.api;

import com.gudrhs8304.ticketory.feature.member.enums.PointChangeType;
import com.gudrhs8304.ticketory.core.auth.CustomUserPrincipal;
import com.gudrhs8304.ticketory.feature.point.PointQueryService;
import com.gudrhs8304.ticketory.feature.point.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class PointController {

    private final PointQueryService pointQueryService;
    private final PointService pointService;

    @GetMapping("/{memberId}/points")
    public ResponseEntity<?> getPointLogs(
            @PathVariable Long memberId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) List<PointChangeType> type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth
    ) {
        // Authentication에서 memberId 꺼내기
        Long me = null;
        if (auth != null && auth.getPrincipal() instanceof CustomUserPrincipal principal) {
            me = principal.getMemberId(); // ✅ CustomUserPrincipal에 memberId getter 필요
        }

        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "로그인이 필요합니다."));
        }
        if (!Objects.equals(me, memberId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "본인 내역만 조회할 수 있습니다."));
        }

        return ResponseEntity.ok(pointService.getLogs(memberId, from, to, type, page, size));
    }
}
