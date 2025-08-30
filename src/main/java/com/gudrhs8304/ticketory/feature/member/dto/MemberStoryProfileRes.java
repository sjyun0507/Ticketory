package com.gudrhs8304.ticketory.feature.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MemberStoryProfileRes {
    private Long memberId;
    private String name;
    private String avatarUrl;
    private LocalDateTime lastWatchedAt;
}
