package com.gudrhs8304.ticketory.dto.admin;

import java.time.LocalDateTime;

public record CancelLogRes(
        Long cancelId,
        Long bookingId,
        Long canceledByMemberId,
        Long canceledByAdminId,
        String reason,
        LocalDateTime createdAt
) {
}
