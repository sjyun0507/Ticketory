package com.gudrhs8304.ticketory.batch.scheduler;

import com.gudrhs8304.ticketory.batch.ProgramGenerateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@Log4j2
public class AutoProgramScheduler {

    private final ProgramGenerateService programGenerateService;

    // 매일 KST 03:10에 "내일 기준 + 5일" 날짜만 생성
    // 8/29에 실행 -> (내일 8/30)+5일 = 9/4
    // 8/30에 실행 -> (내일 8/31)+5일 = 9/5
    @Scheduled(cron = "1 0 0 * * *", zone = "Asia/Seoul")
    public void generateOnlyOneDayAheadPlus5() {
        LocalDate todayKst = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate target = todayKst.plusDays(1).plusDays(6); // = today + 6 일, "내일 기준 +5"를 명시
        int created = programGenerateService.generateForAllActiveOn(target);
        log.info("[auto-program] generated {} screenings for {}", created, target);
    }
}
