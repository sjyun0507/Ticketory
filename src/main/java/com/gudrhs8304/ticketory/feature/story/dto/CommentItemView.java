package com.gudrhs8304.ticketory.feature.story.dto;

public record CommentItemView(
        Long commentId,
        String content,
        java.time.LocalDateTime createdAt,
        Long authorId,
        String authorName,
        String authorAvatarUrl,
        Boolean mine
) {}
