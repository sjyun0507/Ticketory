package com.gudrhs8304.ticketory.feature.story;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryRepository extends JpaRepository<Story, Long> {
    Page<Story> findByMember_MemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
}