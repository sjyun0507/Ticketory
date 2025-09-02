package com.gudrhs8304.ticketory.feature.story;

import com.gudrhs8304.ticketory.feature.story.dto.BookmarkedStoryItemDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByMember_MemberIdAndStory_StoryId(Long memberId, Long storyId);

    @Modifying
    @Query("delete from Bookmark b where b.member.memberId = :memberId and b.story.storyId = :storyId")
    int deleteByMemberAndStory(@Param("memberId") Long memberId, @Param("storyId") Long storyId);

    /**
     * 북마크 목록(내가 북마크한 스토리들) — 응답 DTO로 바로 조회
     * liked/bookmarked 는 viewerId(=나) 기준으로 계산.
     * StoryLike 엔티티 이름이 다르면 아래 서브쿼리 부분만 네 프로젝트에 맞춰 바꿔줘.
     */
    @Query("""
select new com.gudrhs8304.ticketory.feature.story.dto.BookmarkedStoryItemDTO(
   s.storyId,
   mv.movieId,
   mv.title,
   coalesce(
      (
         select mm.url
           from com.gudrhs8304.ticketory.feature.movie.MovieMedia mm
          where mm.movie = mv
            and mm.movieMediaType = com.gudrhs8304.ticketory.feature.movie.MovieMediaType.POSTER
            and mm.mediaId = (
                select min(mm2.mediaId)
                  from com.gudrhs8304.ticketory.feature.movie.MovieMedia mm2
                 where mm2.movie = mv
                   and mm2.movieMediaType = com.gudrhs8304.ticketory.feature.movie.MovieMediaType.POSTER
            )
      ),
      mv.posterUrl
   ),
   s.createdAt,
   s.likeCount,
   s.commentCount,
   (case when sl2.id is not null then true else false end),
   true
)
from com.gudrhs8304.ticketory.feature.story.StoryBookmark b
  join b.story s
  join s.movie mv
  left join com.gudrhs8304.ticketory.feature.story.StoryLike sl2
         on sl2.story = s and sl2.member.memberId = :viewerId
where b.member.memberId = :ownerId
  and s.status = com.gudrhs8304.ticketory.feature.story.StoryStatus.ACTIVE
order by b.createdAt desc
""")
    Page<BookmarkedStoryItemDTO> findBookmarkedFeed(
            @Param("ownerId") Long ownerId,
            @Param("viewerId") Long viewerId,
            Pageable pageable);
}
