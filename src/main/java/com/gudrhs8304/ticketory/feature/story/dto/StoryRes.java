package com.gudrhs8304.ticketory.feature.story.dto;

import java.time.LocalDateTime;

public record StoryRes(
        Long storyId,
        Long bookingId,
        String content,
        LocalDateTime createdAt
) {}
