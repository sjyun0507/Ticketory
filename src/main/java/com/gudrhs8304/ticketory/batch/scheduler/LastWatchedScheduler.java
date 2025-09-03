package com.gudrhs8304.ticketory.batch.scheduler;

import com.gudrhs8304.ticketory.feature.member.service.LastWatchedService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LastWatchedScheduler {

    private final LastWatchedService lastWatchedService;

    /** 5분마다 전체 보정 */
    @Scheduled(cron = "0 */5 * * * *")
    public void reconcile() {
        lastWatchedService.recomputeAll();
    }
}
