package com.gudrhs8304.ticketory.feature.story.domain;

import com.gudrhs8304.ticketory.feature.booking.domain.Booking;
import com.gudrhs8304.ticketory.feature.member.domain.Member;
import com.gudrhs8304.ticketory.feature.movie.domain.Movie;
import com.gudrhs8304.ticketory.feature.story.enums.StoryStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "story",
        indexes = {
                @Index(name = "idx_story_member", columnList = "member_id"),
                @Index(name = "idx_story_movie", columnList = "movie_id"),
                @Index(name = "idx_story_booking", columnList = "booking_id"),
                @Index(name = "idx_story_status", columnList = "status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "story_id")
    private Long storyId;

    /** 작성자 (Member) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** 영화 (Movie) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    /** 예매 (Booking) - 선택 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    /** 스토리 본문 */
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 평점 (예: 4.5) -> DECIMAL(2,1) */
    @Column(name = "rating", precision = 2, scale = 1)
    private BigDecimal rating;

    /** 생성/수정일 — DB DDL과 호환됨 */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** 상태 (ACTIVE / DELETED) — VARCHAR(20)로 저장 */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private StoryStatus status = StoryStatus.ACTIVE;

    /** 카운트 */
    @Builder.Default
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Builder.Default
    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    /** 빌더/세터 경로에서 NULL 들어오는 것에 대한 최종 안전망 */
    @PrePersist
    protected void onPrePersist() {
        if (status == null) status = StoryStatus.ACTIVE;
        if (likeCount == null) likeCount = 0;
        if (commentCount == null) commentCount = 0;
    }
}