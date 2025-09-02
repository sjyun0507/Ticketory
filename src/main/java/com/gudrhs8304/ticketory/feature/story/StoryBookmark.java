package com.gudrhs8304.ticketory.feature.story;

import com.gudrhs8304.ticketory.feature.member.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "story_bookmark",
        uniqueConstraints = @UniqueConstraint(name="uk_story_bookmark", columnNames = {"story_id","member_id"}),
        indexes = {
                @Index(name="idx_story_bookmark_story", columnList="story_id"),
                @Index(name="idx_story_bookmark_member", columnList="member_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryBookmark {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="story_id", nullable=false)
    private Story story;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="member_id", nullable=false)
    private Member member;

    @Column(name="created_at", nullable=false, updatable=false)
    private LocalDateTime createdAt;

    @PrePersist void onCreate(){ this.createdAt = LocalDateTime.now(); }
}
