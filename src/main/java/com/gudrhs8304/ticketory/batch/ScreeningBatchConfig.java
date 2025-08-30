package com.gudrhs8304.ticketory.batch;

import com.gudrhs8304.ticketory.feature.screening.ScreeningRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;

@Log4j2
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ScreeningBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final ScreeningRepository screeningRepository;

    @Bean
    public Job screeningJob() {
        return new JobBuilder("screeningJob", jobRepository)
                .start(screeningStep())
                .build();
    }

    @Bean
    public Step screeningStep() {
        return new
                StepBuilder("screeningStep", jobRepository)
                .tasklet(screeningTasklet(), transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public Tasklet screeningTasklet() {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution,
                                        ChunkContext chunkContext) throws Exception {

                screeningRepository.updateIsBookingEnd(LocalDateTime.now().plusMinutes(30L));
                return RepeatStatus.FINISHED;
            }
        };
    }
}
