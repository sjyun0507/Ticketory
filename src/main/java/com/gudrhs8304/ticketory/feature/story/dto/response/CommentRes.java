package com.gudrhs8304.ticketory.feature.story.dto.response;

import java.time.LocalDateTime;

public record CommentRes(
        Long commentId,
        MemberMini member,
        String content,
        LocalDateTime createdAt,
        boolean edited
) {
    public record MemberMini(Long memberId, String name, String avatarUrl) {}
}
