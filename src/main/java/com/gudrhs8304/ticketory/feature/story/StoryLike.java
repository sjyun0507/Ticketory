package com.gudrhs8304.ticketory.feature.story;

import com.gudrhs8304.ticketory.feature.member.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "story_like",
        uniqueConstraints = @UniqueConstraint(name="uk_story_like", columnNames = {"story_id","member_id"}),
        indexes = {
                @Index(name="idx_story_like_story", columnList = "story_id"),
                @Index(name="idx_story_like_member", columnList = "member_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StoryLike {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="story_id", nullable=false)
    private Long storyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="member_id", nullable=false)
    private Member member;
}
