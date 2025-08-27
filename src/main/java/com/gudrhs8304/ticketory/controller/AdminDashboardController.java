package com.gudrhs8304.ticketory.controller;

import com.gudrhs8304.ticketory.domain.CancelLog;
import com.gudrhs8304.ticketory.domain.RefundLog;
import com.gudrhs8304.ticketory.dto.admin.CancelLogRes;
import com.gudrhs8304.ticketory.dto.admin.RefundLogRes;
import com.gudrhs8304.ticketory.repository.CancelLogRepository;
import com.gudrhs8304.ticketory.repository.RefundLogRepository;
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
    public ResponseEntity<Page<CancelLogRes>> getCancelLogs(
            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CancelLog> slice = cancelLogRepo.search(bookingId, memberId, pageable);
        Page<CancelLogRes> body = slice.map(c -> new CancelLogRes(
                c.getCancelId(),
                c.getBooking().getBookingId(),
                c.getCanceledByMember() == null ? null : c.getCanceledByMember().getMemberId(),
                c.getCanceledByAdmin()  == null ? null : c.getCanceledByAdmin().getMemberId(),
                c.getReason(),
                c.getCreatedAt()
        ));
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "환불 로그 조회 (관리자)")
    @GetMapping("/refund-logs")
    public ResponseEntity<Page<RefundLogRes>> getRefundLogs(
            @RequestParam(required = false) Long paymentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<RefundLog> slice = (paymentId == null)
                ? refundLogRepo.findAll(pageable)
                : refundLogRepo.findByPayment_PaymentId(paymentId, pageable);

        Page<RefundLogRes> body = slice.map(r -> new RefundLogRes(
                r.getRefundId(),
                r.getPayment().getPaymentId(),
                r.getRefundAmount(),
                r.getReason(),
                r.getPgRefundTid(),
                r.getStatus(),
                r.getProcessedByAdmin() == null ? null : r.getProcessedByAdmin().getMemberId(),
                r.getCreatedAt()
        ));

        return ResponseEntity.ok(body);
    }

}
