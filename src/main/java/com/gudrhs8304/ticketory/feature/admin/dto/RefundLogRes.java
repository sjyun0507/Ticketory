package com.gudrhs8304.ticketory.feature.admin.dto;

import java.time.LocalDateTime;

public record RefundLogRes(
        Long refundId,
        Long paymentId,
        Integer refundAmount,
        String reason,
        String pgRefundTid,
        String status,
        Long processedByAdminId,
        LocalDateTime createdAt
) {}
