package com.gudrhs8304.ticketory.repository;

import com.gudrhs8304.ticketory.domain.BoardPost;
import com.gudrhs8304.ticketory.domain.enums.Type;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

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
}
