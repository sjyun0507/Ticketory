package com.gudrhs8304.ticketory.feature.story.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class StorySimpleRes {
    private Long storyId;
    private Long movieId;
    private String movieTitle;
    private String posterUrl;
    private BigDecimal rating;       // null 가능
    private String content;
    private LocalDateTime createdAt;
}
