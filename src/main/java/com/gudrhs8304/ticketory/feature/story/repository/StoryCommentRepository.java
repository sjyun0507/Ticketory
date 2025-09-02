package com.gudrhs8304.ticketory.feature.story.repository;

import com.gudrhs8304.ticketory.feature.story.domain.StoryComment;
import com.gudrhs8304.ticketory.feature.story.dto.response.CommentItemView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoryCommentRepository extends JpaRepository<StoryComment, Long> {

    @Query("""
  select new com.gudrhs8304.ticketory.feature.story.dto.response.CommentItemView(
    c.commentId,
    c.content,
    c.createdAt,
    m.memberId,
    coalesce(m.name, '익명'),
    m.avatarUrl,
    case when :viewerId is not null and m.memberId = :viewerId then true else false end
  )
  from StoryComment c
  join c.member m
  where c.storyId = :storyId
  order by c.createdAt asc
""")
    Page<CommentItemView> findComments(@Param("storyId") Long storyId,
                                       @Param("viewerId") Long viewerId,
                                       Pageable pageable);


    Page<StoryComment> findByStoryIdOrderByCommentIdAsc(Long storyId, Pageable pageable);

    long countByStoryId(Long storyId);

}
