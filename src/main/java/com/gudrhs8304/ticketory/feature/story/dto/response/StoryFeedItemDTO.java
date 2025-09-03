package com.gudrhs8304.ticketory.feature.story.dto.response;

import com.gudrhs8304.ticketory.feature.story.domain.Story;
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

    /** 작성자 정보 */
    private Long memberId;
    private String memberName;
    private String avatarUrl;

    /** 영화 정보 */
    private Long movieId;
    private String movieTitle;
    private String posterUrl;

    /** 스토리 정보 */
    private String content;
    private BigDecimal rating;
    private int likeCount;
    private int commentCount;
    private LocalDateTime createdAt;

    @Builder.Default
    private boolean liked = false;
    private boolean bookmarked;

    public static StoryFeedItemDTO from(Story s) {
        return StoryFeedItemDTO.builder()
                .storyId(s.getStoryId())
                // 작성자
                .memberId(s.getMember().getMemberId())
                .memberName(s.getMember().getName())
                .avatarUrl(s.getMember().getAvatarUrl())
                // 영화
                .movieId(s.getMovie().getMovieId())
                .movieTitle(s.getMovie().getTitle())
                .posterUrl(s.getMovie().getPosterUrl())
                // 스토리
                .content(s.getContent())
                .rating(s.getRating())
                .likeCount(s.getLikeCount())
                .commentCount(s.getCommentCount())
                .createdAt(s.getCreatedAt())
                .liked(false)
                .build();


    }

    public static StoryFeedItemDTO from(StoryFeedItemView v) {
        return StoryFeedItemDTO.builder()
                .storyId(v.storyId())
                // 작성자
                .memberId(v.member().memberId())
                .memberName(v.member().name())
                .avatarUrl(v.member().avatarUrl())
                // 영화
                .movieId(v.movie().movieId())
                .movieTitle(v.movie().title())
                .posterUrl(v.movie().poster())
                // 스토리
                .content(v.content())
                .rating(v.rating())
                .likeCount(v.likeCount())
                .commentCount(v.commentCount())
                .createdAt(v.createdAt())
                .liked(Boolean.TRUE.equals(v.liked()))
                .bookmarked(v.bookmarked() != null ? v.bookmarked() : false)
                .build();
    }
}