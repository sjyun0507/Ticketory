package com.gudrhs8304.ticketory.feature.story.dto.response;

import java.time.LocalDateTime;

public record BookmarkedStoryItemDTO(
        Long storyId,
        Long movieId,
        String movieTitle,
        String posterUrl,
        LocalDateTime createdAt,
        int likes,
        int comments,
        boolean liked,
        boolean bookmarked
) {}
