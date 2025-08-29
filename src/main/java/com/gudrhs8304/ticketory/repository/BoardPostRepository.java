package com.gudrhs8304.ticketory.repository;

import com.gudrhs8304.ticketory.domain.BoardPost;
import com.gudrhs8304.ticketory.domain.enums.Type;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {

    @Query("""
              select b from BoardPost b
               where (:type is null or b.type in :type)
                 and (:published is null or b.published = :published)
               order by b.createdAt desc
            """)
    Page<BoardPost> findForList(@Param("type") java.util.List<Type> type,
                                @Param("published") Boolean published,
                                Pageable pageable);

    @Query("""
            select b
            from BoardPost b
            where b.type = :type
              and b.published = true
              and (b.publishAt is null or b.publishAt <= :now)
              and (b.startDate is null or b.startDate <= :today)
              and (b.endDate   is null or b.endDate   >= :today)
            order by coalesce(b.publishAt, b.createdAt) desc, b.id desc
            """)
    List<BoardPost> findVisiblePosts(
            @Param("type") Type type,
            @Param("now") LocalDateTime now,
            @Param("today") LocalDate today
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update BoardPost b
           set b.published = true,
               b.updatedAt = CURRENT_TIMESTAMP
         where b.published = false
           and b.publishAt is not null
           and b.publishAt <= :now
    """)
    int autoPublish(@Param("now") LocalDateTime now);
}
