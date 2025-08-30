package com.gudrhs8304.ticketory.feature.story.dto;

import lombok.Builder;

@Builder
public record StoryFeedFilter(
        Long movieId,
        String tag,
        Long memberId,
        Boolean hasProof,       // booking 존재 여부
        String sort             // RECENT | POPULAR (기본 RECENT)
) {}
