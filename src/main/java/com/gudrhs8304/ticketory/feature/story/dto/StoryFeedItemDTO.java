package com.gudrhs8304.ticketory.feature.story.dto;

import com.gudrhs8304.ticketory.feature.story.Story;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoryFeedItemDTO {
    private Long storyId;
    private Long memberId;
    private Long movieId;
    private String content;
    private BigDecimal rating;
    private int likeCount;
    private int commentCount;
    private LocalDateTime createdAt;

    public static StoryFeedItemDTO from(Story s) {
        return StoryFeedItemDTO.builder()
                .storyId(s.getStoryId())
                .memberId(s.getMember().getMemberId())
                .movieId(s.getMovie().getMovieId())
                .content(s.getContent())
                .rating(s.getRating())
                .likeCount(s.getLikeCount())
                .commentCount(s.getCommentCount())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
