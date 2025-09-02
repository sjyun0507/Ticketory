package com.gudrhs8304.ticketory.feature.story.dto.response;

public record CommentAuthorView(
        Long memberId,
        String name,
        String avatarUrl
) {}
