package com.gudrhs8304.ticketory.feature.story.domain;

import com.gudrhs8304.ticketory.feature.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "story_comment",
        indexes = {
                @Index(name="idx_comment_story", columnList="story_id, created_at"),
                @Index(name="idx_comment_member", columnList="member_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StoryComment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="comment_id")
    private Long commentId;

    @Column(name="story_id", nullable=false)
    private Long storyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="member_id", nullable=false)
    private Member member;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() { this.createdAt = LocalDateTime.now(); }

    @PreUpdate
    void onUpdate() { this.updatedAt = LocalDateTime.now(); }
}
