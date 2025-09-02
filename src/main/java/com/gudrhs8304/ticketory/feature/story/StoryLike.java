package com.gudrhs8304.ticketory.feature.story;

import com.gudrhs8304.ticketory.feature.member.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "story_like",
        uniqueConstraints = @UniqueConstraint(name="uk_story_like", columnNames = {"story_id","member_id"}),
        indexes = {
                @Index(name="idx_story_like_story", columnList = "story_id"),
                @Index(name="idx_story_like_member", columnList = "member_id")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(of = "id")
public class StoryLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 좋아요한 스토리 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="story_id", nullable=false)
    private Story story;

    /** 좋아요 누른 회원 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="member_id", nullable=false)
    private Member member;

    /** 좋아요 누른 시간 */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
