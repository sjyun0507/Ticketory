package com.gudrhs8304.ticketory.feature.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberProfileRes {
    Long memberId;
    String name;
    String avatarUrl;
    LocalDateTime lastWatchedAt;
}
