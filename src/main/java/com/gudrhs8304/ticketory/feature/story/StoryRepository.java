package com.gudrhs8304.ticketory.feature.story;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface StoryRepository extends JpaRepository<Story, Long> {
    Page<Story> findByMember_MemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    Page<Story> findByMember_MemberIdAndStatusOrderByCreatedAtDesc(
            Long memberId, StoryStatus status, Pageable pageable);

    boolean existsByMember_MemberIdAndBooking_BookingIdAndStatus(
            Long memberId, Long bookingId, StoryStatus status);

    Optional<Story> findByStoryIdAndStatus(Long storyId, StoryStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Story s set s.status = :status where s.storyId = :storyId")
    int updateStatus(Long storyId, StoryStatus status);

    @Query("""
        select
          s.storyId,
          mv.movieId,
          mv.title,
          mv.posterUrl,
          s.rating,
          s.content,
          s.createdAt
        from Story s
          join s.movie mv
        where s.member.memberId = :memberId
          and s.status <> 'DELETED'
        order by s.createdAt desc
    """)
    Page<Object[]> findMyStoryRows(@Param("memberId") Long memberId, Pageable pageable);


}