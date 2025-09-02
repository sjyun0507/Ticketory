package com.gudrhs8304.ticketory.feature.story.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StoryFeedItemView(
        Long storyId,

        @JsonUnwrapped
        StoryMemberView member,
        StoryMovieView movie,
        String content,
        BigDecimal rating,
        int likeCount,
        int commentCount,
        LocalDateTime createdAt,
        Boolean liked,
        Boolean bookmarked
) {
    // 기존 JPQL(new …)과 호환되도록 bookmarked 없는 생성자도 유지
    public StoryFeedItemView(
            Long storyId,
            StoryMemberView member,
            StoryMovieView movie,
            String content,
            BigDecimal rating,
            int likeCount,
            int commentCount,
            LocalDateTime createdAt
    ) {
        this(storyId, member, movie, content, rating, likeCount, commentCount, createdAt, null, null);
    }

    public StoryFeedItemView(
            Long storyId,
            StoryMemberView member,
            StoryMovieView movie,
            String content,
            BigDecimal rating,
            int likeCount,
            int commentCount,
            LocalDateTime createdAt,
            Boolean liked
    ) {
        this(storyId, member, movie, content, rating, likeCount, commentCount, createdAt, liked, null);
    }
}