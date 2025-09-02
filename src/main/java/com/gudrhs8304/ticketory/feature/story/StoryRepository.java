package com.gudrhs8304.ticketory.feature.story;


import com.gudrhs8304.ticketory.feature.story.dto.StoryFeedItemDTO;
import com.gudrhs8304.ticketory.feature.story.dto.StoryFeedItemView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface StoryRepository extends JpaRepository<Story, Long>, JpaSpecificationExecutor<Story> {
    Page<Story> findByMember_MemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    boolean existsByMember_MemberIdAndBooking_BookingIdAndStatus(
            Long memberId, Long bookingId, StoryStatus status);


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


    Page<Story> findByStatusOrderByCreatedAtDesc(StoryStatus status, Pageable pageable);

    Page<Story> findByMember_MemberIdAndStatusOrderByCreatedAtDesc(Long memberId, StoryStatus status, Pageable pageable);


    Optional<Story> findByStoryIdAndStatus(Long storyId, StoryStatus status);


    boolean existsByBooking_BookingIdAndStatusNot(Long bookingId, StoryStatus status);

    @Query("""
select new com.gudrhs8304.ticketory.feature.story.dto.StoryFeedItemView(
  s.storyId,
  new com.gudrhs8304.ticketory.feature.story.dto.StoryMemberView(m.memberId, m.name, m.avatarUrl),
  new com.gudrhs8304.ticketory.feature.story.dto.StoryMovieView(v.movieId, v.title, v.posterUrl),
  s.content,
  s.rating,
  cast(count(distinct l.id) as int),
  s.commentCount,
  s.createdAt,
  case
    when :viewerId is null then false
    when exists (
      select 1 from StoryLike l2
      where l2.story = s and l2.member.memberId = :viewerId
    ) then true
    else false
  end,
  case
    when :viewerId is null then false
    when exists (
      select 1 from StoryBookmark b
      where b.story = s and b.member.memberId = :viewerId
    ) then true
    else false
  end
)
from Story s
join s.member m
join s.movie  v
left join StoryLike l on l.story = s
where s.status = com.gudrhs8304.ticketory.feature.story.StoryStatus.ACTIVE
group by
  s.storyId, m.memberId, m.name, m.avatarUrl,
  v.movieId, v.title, v.posterUrl,
  s.content, s.rating, s.commentCount, s.createdAt
order by s.createdAt desc
""")
    Page<StoryFeedItemView> findFeed(@Param("viewerId") Long viewerId, Pageable pageable);

    @Query("""
            select new com.gudrhs8304.ticketory.feature.story.dto.StoryFeedItemView(
              s.storyId,
              new com.gudrhs8304.ticketory.feature.story.dto.StoryMemberView(m.memberId, m.name, m.avatarUrl),
              new com.gudrhs8304.ticketory.feature.story.dto.StoryMovieView(v.movieId, v.title, v.posterUrl),
              s.content,
              s.rating,
              cast(count(distinct sl.id) as int),
              s.commentCount,
              s.createdAt,
              (count(me.id) > 0)
            )
            from Story s
            join s.member m
            join s.movie  v
            left join StoryLike sl on sl.story = s
            left join StoryLike me on me.story = s
                             and :viewerId is not null
                             and me.member.memberId = :viewerId
            where s.status = com.gudrhs8304.ticketory.feature.story.StoryStatus.ACTIVE
              and v.movieId = :movieId
            group by
              s.storyId, m.memberId, m.name, m.avatarUrl,
              v.movieId, v.title, v.posterUrl,
              s.content, s.rating, s.commentCount, s.createdAt
            order by s.createdAt desc
            """)
    Page<StoryFeedItemView> findFeedByMovie(@Param("movieId") Long movieId,
                                            @Param("viewerId") Long viewerId,
                                            Pageable pageable);

    @Query("""
            select new com.gudrhs8304.ticketory.feature.story.dto.StoryFeedItemView(
              s.storyId,
              new com.gudrhs8304.ticketory.feature.story.dto.StoryMemberView(m.memberId, m.name, m.avatarUrl),
              new com.gudrhs8304.ticketory.feature.story.dto.StoryMovieView(v.movieId, v.title, v.posterUrl),
              s.content,
              s.rating,
              cast(count(distinct sl.id) as int),
              s.commentCount,
              s.createdAt,
              (count(me.id) > 0)
            )
            from Story s
            join s.member m
            join s.movie  v
            left join StoryLike sl on sl.story = s
            left join StoryLike me on me.story = s
                             and :viewerId is not null
                             and me.member.memberId = :viewerId
            where s.status = com.gudrhs8304.ticketory.feature.story.StoryStatus.ACTIVE
              and m.memberId = :authorId
            group by
              s.storyId, m.memberId, m.name, m.avatarUrl,
              v.movieId, v.title, v.posterUrl,
              s.content, s.rating, s.commentCount, s.createdAt
            order by s.createdAt desc
            """)
    Page<StoryFeedItemView> findMyFeed(@Param("authorId") Long authorId,
                                       @Param("viewerId") Long viewerId,
                                       Pageable pageable);

    @Query("""
            select new com.gudrhs8304.ticketory.feature.story.dto.StoryFeedItemView(
              s.storyId,
              new com.gudrhs8304.ticketory.feature.story.dto.StoryMemberView(m.memberId, m.name, m.avatarUrl),
              new com.gudrhs8304.ticketory.feature.story.dto.StoryMovieView(v.movieId, v.title, v.posterUrl),
              s.content,
              s.rating,
              cast(count(distinct sl.id) as int),
              s.commentCount,
              s.createdAt,
              (count(me.id) > 0)
            )
            from Story s
            join s.member m
            join s.movie  v
            left join StoryLike sl on sl.story = s
            left join StoryLike me on me.story = s
                             and :viewerId is not null
                             and me.member.memberId = :viewerId
            where s.status = com.gudrhs8304.ticketory.feature.story.StoryStatus.ACTIVE
              and s.storyId = :storyId
            group by
              s.storyId, m.memberId, m.name, m.avatarUrl,
              v.movieId, v.title, v.posterUrl,
              s.content, s.rating, s.commentCount, s.createdAt
            """)
    Optional<StoryFeedItemView> findOneAsFeedItem(@Param("storyId") Long storyId,
                                                  @Param("viewerId") Long viewerId);


}