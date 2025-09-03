package com.gudrhs8304.ticketory.feature.admin.dto;

import com.gudrhs8304.ticketory.feature.payment.enums.PaymentStatus; // <- 실제 패키지 맞춰주세요
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CancelLogRes(
        Long cancelId,
        Long bookingId,
        Long paymentId,
        Integer refundAmount,
        PaymentStatus status,
        String reason,

        // RefundLog
        String pgRefundTid,
        LocalDateTime refundedAt,     // RefundLog.createdAt (최근 1건)

        // 시간 정보
        LocalDateTime bookingTime,    // Booking.bookingTime
        LocalDateTime canceledAt,     // CancelLog.createdAt

        // 처리자/요청자
        Long canceledByMemberId,
        Long canceledByAdminId,
        String canceledByAdminName
) {}
