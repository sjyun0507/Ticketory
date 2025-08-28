package com.gudrhs8304.ticketory.repository;

import com.gudrhs8304.ticketory.domain.Screen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreenRepository extends JpaRepository<Screen, Long> {
    // 활성화된 상영관만 조회
    List<Screen> findByIsActiveTrue();
}
