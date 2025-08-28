package com.gudrhs8304.ticketory.dto.admin;

import com.gudrhs8304.ticketory.domain.enums.PaymentStatus; // <- 실제 패키지 맞춰주세요
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CancelLogRes(
        Long cancelId,
        Long bookingId,
        Long paymentId,              // 결제 ID
        BigDecimal refundAmount,     // 결제 금액(환불금액으로 사용)
        PaymentStatus status,        // PAID/PENDING/REFUNDED/CANCELLED/FAILED
        String reason,               // 취소 사유
        String pgRefundTid,          // 스키마에 없음 → 일단 null
        Long canceledByMemberId,
        Long canceledByAdminId,
        String canceledByAdminName,  // 관리자 email/이름 중 하나
        LocalDateTime createdAt
) {}
