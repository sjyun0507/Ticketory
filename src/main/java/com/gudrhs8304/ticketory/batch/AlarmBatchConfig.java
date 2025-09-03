package com.gudrhs8304.ticketory.batch;

import com.gudrhs8304.ticketory.mail.service.MailSenderService;
import com.gudrhs8304.ticketory.mail.dto.BookingAlarmDTO;
import com.gudrhs8304.ticketory.feature.booking.repository.BookingRepository;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

@Log4j2
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class AlarmBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final MailSenderService mailSenderService;
    private final BookingRepository bookingRepository;

    private static final DateTimeFormatter MAIL_TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Bean
    public Job alarmJob() {
        return new JobBuilder("alarmJob", jobRepository)
                .start(alarmStep())
                .build();
    }

    @Bean
    public Step alarmStep() {
        return new StepBuilder("alarmStep", jobRepository)
                .tasklet(alarmTasklet(), transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public Tasklet alarmTasklet() {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
                final LocalDateTime cutoff = LocalDateTime.now().plusMinutes(10);
                log.info("[ALARM] cutoff <= {}", cutoff);

                final List<BookingAlarmDTO> targets = bookingRepository.findBookingAlarmDTO(cutoff);

                if (targets.isEmpty()) {
                    log.info("[ALARM] 대상 없음");
                    return RepeatStatus.FINISHED;
                }

                log.info("[ALARM] 대상 {}건", targets.size());

                for (BookingAlarmDTO dto : targets) {
                    final Long bookingId = dto.getBookingId();
                    final String to = dto.getToAddress();
                    final String title = dto.getMovieTitle();
                    final LocalDateTime startAt = dto.getStartAt();
                    final String qrUrl = dto.getQrCodeUrl();
                    final String posterUrl = dto.getPosterUrl();

                    if (to == null || to.isBlank()) {
                        log.warn("[ALARM] 이메일 주소 없음, bookingId={}", bookingId);
                        continue;
                    }

                    try {
                        mailSenderService.sendMailForAlarm(
                                to,
                                title,
                                startAt != null ? startAt.format(MAIL_TIME_FMT) : "-",
                                bookingId,
                                qrUrl,
                                posterUrl
                        );

                        // 성공 시에만 플래그 true
                        int upd = bookingRepository.updateIsSendAlarm(bookingId, true);
                        log.info("[ALARM] 메일 발송 & 플래그 갱신 완료 bookingId={}, updated={}", bookingId, upd);
                    } catch (Exception e) {
                        // 한 건 실패해도 다음 건 진행
                        log.error("[ALARM] 메일 발송 실패 bookingId={}, to={}, err={}", bookingId, to, e.toString(), e);
                    }
                }

                return RepeatStatus.FINISHED;
            }
        };
    }
}