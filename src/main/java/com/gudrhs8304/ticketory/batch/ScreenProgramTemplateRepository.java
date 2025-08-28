package com.gudrhs8304.ticketory.batch;

import com.gudrhs8304.ticketory.domain.Screen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreenProgramTemplateRepository extends JpaRepository<ScreenProgramTemplate, Long> {
    List<ScreenProgramTemplate> findByScreenAndEnabledIsTrue(Screen screen);
}
