package com.gudrhs8304.ticketory.dto.admin;

import java.time.LocalDateTime;

public record AdminActionLogRes(
        Long adminActionId,
        Long adminMemberId,
        String actionType,
        String targetTable,
        Long targetId,
        String payloadJson,
        String ipAddress,
        String userAgent,
        LocalDateTime createdAt
) {}
