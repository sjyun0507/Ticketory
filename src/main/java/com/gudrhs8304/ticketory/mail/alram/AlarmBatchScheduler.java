package com.gudrhs8304.ticketory.mail.alram;

import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Log4j2
@Component
public class AlarmBatchScheduler {
    private final JobLauncher jobLauncher;
    @Qualifier("alarmJob")
    private final Job ordersStatsJob;

    public AlarmBatchScheduler(JobLauncher jobLauncher, @Qualifier("alarmJob")Job ordersStatsJob) {
        this.jobLauncher = jobLauncher;
        this.ordersStatsJob = ordersStatsJob;
    }

    // 1분 마다 실행
    @Scheduled(cron = "0 * * * * *")
    // @Scheduled(cron = "10 * * * * *")
    public void run() {
        try {
            log.info("알람 메일 배치 작업 시작 - 대상 시간: {}", LocalDateTime.now());
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp",
                            System.currentTimeMillis())
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(ordersStatsJob, jobParameters);
        } catch (Exception e) {
            log.error("매출 통계 배치 작업 실행 중 오류 발생", e);
        }
    }
}
