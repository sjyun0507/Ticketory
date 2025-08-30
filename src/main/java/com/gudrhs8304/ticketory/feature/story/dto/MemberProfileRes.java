package com.gudrhs8304.ticketory.feature.story.dto;

import java.time.LocalDateTime;

public record MemberProfileRes(
        Long memberId,
        String name,
        String avatarUrl,
        LocalDateTime lastWatchedAt
) {}
