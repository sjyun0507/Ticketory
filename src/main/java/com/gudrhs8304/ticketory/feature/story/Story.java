package com.gudrhs8304.ticketory.feature.story;

import com.gudrhs8304.ticketory.feature.booking.domain.Booking;
import com.gudrhs8304.ticketory.feature.member.Member;
import com.gudrhs8304.ticketory.feature.movie.Movie;
import jakarta.persistence.*;
import lombok.*;

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

    /** 예매 (Booking) - 선택적 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    /** 스토리 본문 */
    @Lob
    @Column(nullable = false)
    private String content;

    /** 평점 (예: 4.5) */
    @Column(precision = 2, scale = 1)
    private BigDecimal rating;

    /** 생성일 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정일 */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** 상태 (ACTIVE, DELETED, HIDDEN 등) */
    @Column(length = 20, nullable = false)
    private String status = "ACTIVE";

    /** 좋아요 개수 */
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    /** 댓글 개수 */
    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = "ACTIVE";
        if (this.likeCount == null) this.likeCount = 0;
        if (this.commentCount == null) this.commentCount = 0;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
