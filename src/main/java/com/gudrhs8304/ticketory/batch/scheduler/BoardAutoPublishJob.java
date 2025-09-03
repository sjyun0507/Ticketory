package com.gudrhs8304.ticketory.batch.scheduler;

import com.gudrhs8304.ticketory.feature.board.repository.BoardPostRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@Log4j2
public class BoardAutoPublishJob {

    private final BoardPostRepository repo;

    // 매 1분마다 실행 (KST)
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    @Transactional
    public void autoPublish() {
        var now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        int updated = repo.autoPublish(now);
        if (updated > 0) {
            log.info("[board] auto-published {} posts (<= {})", updated, now);
        }
    }
}
