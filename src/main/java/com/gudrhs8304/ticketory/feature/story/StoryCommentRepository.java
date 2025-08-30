package com.gudrhs8304.ticketory.feature.story;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryCommentRepository extends JpaRepository<StoryComment, Long> {
    Page<StoryComment> findByStoryIdOrderByCreatedAtAsc(Long storyId, Pageable pageable);
    long countByStoryId(Long storyId);
}
