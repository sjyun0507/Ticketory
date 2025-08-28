package com.gudrhs8304.ticketory.batch;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ScreenProgramSlotRepository extends JpaRepository<ScreenProgramSlot, Long> {
    @Query("""
    select sps
    from ScreenProgramSlot sps
      join fetch sps.template t
      join fetch t.screen sc
      left join fetch sps.movie m
    where t = :template
    order by sps.startTime, sps.priority
    """)
    List<ScreenProgramSlot> findByTemplateWithJoinsOrderByStartTimeAscPriorityAsc(@Param("template") ScreenProgramTemplate template);
}
