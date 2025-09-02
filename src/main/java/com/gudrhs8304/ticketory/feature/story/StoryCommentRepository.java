package com.gudrhs8304.ticketory.feature.story;

import com.gudrhs8304.ticketory.feature.story.dto.CommentItemView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoryCommentRepository extends JpaRepository<StoryComment, Long> {
    Page<StoryComment> findByStoryIdOrderByCreatedAtAsc(Long storyId, Pageable pageable);
    long countByStoryId(Long storyId);

    @Query("""
    select new com.gudrhs8304.ticketory.feature.story.dto.CommentItemView(
      c.commentId,
      c.content,
      c.createdAt,
      m.memberId,
      m.name,
      m.avatarUrl,
      case when :viewerId is not null and m.memberId = :viewerId then true else false end
    )
    from StoryComment c
    join c.member m
    where c.storyId = :storyId
    order by c.createdAt desc
  """)
    Page<CommentItemView> findComments(@Param("storyId") Long storyId,
                                       @Param("viewerId") Long viewerId,
                                       Pageable pageable);

    @Query(
            value = """
      select c
      from StoryComment c
      join fetch c.member m
      where c.storyId = :storyId
      order by c.createdAt asc
    """,
            countQuery = """
      select count(c)
      from StoryComment c
      where c.storyId = :storyId
    """
    )
    Page<StoryComment> findWithMemberByStoryIdOrderByCreatedAtAsc(
            @Param("storyId") Long storyId, Pageable pageable);
}
