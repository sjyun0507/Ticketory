package com.gudrhs8304.ticketory.feature.stats.api;

import com.gudrhs8304.ticketory.feature.admin.dto.CancelLogRes;
import com.gudrhs8304.ticketory.feature.booking.CancelLogRepository;
import com.gudrhs8304.ticketory.feature.refund.RefundLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final CancelLogRepository cancelLogRepo;
    private final RefundLogRepository refundLogRepo;

    @Operation(summary = "취소 로그 조회 (관리자)")
    @GetMapping("/cancel-logs")
    public ResponseEntity<Page<CancelLogRes>> cancelLogs(
            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CancelLogRes> body = cancelLogRepo.search(bookingId, memberId, pageable);
        return ResponseEntity.ok(body);
    }
}