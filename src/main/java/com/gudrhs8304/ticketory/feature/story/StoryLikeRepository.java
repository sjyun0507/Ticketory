package com.gudrhs8304.ticketory.feature.story;

import com.gudrhs8304.ticketory.feature.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoryLikeRepository extends JpaRepository<StoryLike, Long> {
    boolean existsByStoryIdAndMember(Long storyId, Member member);
    Optional<StoryLike> findByStoryIdAndMember(Long storyId, Member member);
    long countByStoryId(Long storyId);
}
