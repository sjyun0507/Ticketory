package com.gudrhs8304.ticketory.scheduler;

import com.gudrhs8304.ticketory.service.SeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeatHoldCleaner {

    private final SeatService seatService;

    @Scheduled(fixedDelay = 60_000) // 1분마다 실행
    public void purge() {
        int deleted = seatService.purgeExpiredHolds();
        if (deleted > 0) log.info("[SeatHoldCleaner] purged expired holds: {}", deleted);
    }
}
