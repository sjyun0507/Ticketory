package com.gudrhs8304.ticketory.feature.story.dto.response;

import com.gudrhs8304.ticketory.feature.story.domain.Story;

import java.time.LocalDateTime;

public record StoryRes(
        Long storyId,
        Long bookingId,
        String content,
        LocalDateTime createdAt
) {
    public static StoryRes from(Story story) {
        if (story == null) return null;
        return new StoryRes(
                story.getStoryId(),
                story.getBooking() != null ? story.getBooking().getBookingId() : null,
                story.getContent(),
                story.getCreatedAt()
        );
    }
}
