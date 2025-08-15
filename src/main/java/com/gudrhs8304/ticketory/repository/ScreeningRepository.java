package com.gudrhs8304.ticketory.repository;

import com.gudrhs8304.ticketory.domain.Screening;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {
    long countByScreen_ScreenId(Long screenId);
}
