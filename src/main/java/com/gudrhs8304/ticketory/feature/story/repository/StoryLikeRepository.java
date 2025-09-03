package com.gudrhs8304.ticketory.feature.story.repository;

import com.gudrhs8304.ticketory.feature.member.domain.Member;
import com.gudrhs8304.ticketory.feature.story.domain.StoryLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoryLikeRepository extends JpaRepository<StoryLike, Long> {
    boolean existsByStory_StoryIdAndMember(Long storyId, Member member);
    long countByStory_StoryId(Long storyId);
    Optional<StoryLike> findByStory_StoryIdAndMember(Long storyId, Member member);
}
