package com.gudrhs8304.ticketory.feature.story;

import com.gudrhs8304.ticketory.feature.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoryBookmarkRepository extends JpaRepository<StoryBookmark, Long> {
    boolean existsByStoryIdAndMember(Long storyId, Member member);
    Optional<StoryBookmark> findByStoryIdAndMember(Long storyId, Member member);
}
