package com.gudrhs8304.ticketory.batch.runner;

import com.gudrhs8304.ticketory.batch.ProgramGenerateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProgramTestRunner implements CommandLineRunner {

    private final ProgramGenerateService programGenerateService;

    @Override
    public void run(String... args) throws Exception {
        // 오늘 기준 +6일 날짜로 생성 (스케줄러와 동일한 로직)
        LocalDate target = LocalDate.now().plusDays(6);

        int created = programGenerateService.generateForAllActiveOn(target);
        log.info("[TEST] generated {} screenings for {}", created, target);
    }
}
