package com.gudrhs8304.ticketory.service;

import com.gudrhs8304.ticketory.batch.ProgramGenerateService;
import com.gudrhs8304.ticketory.domain.Screening;
import com.gudrhs8304.ticketory.repository.ScreeningRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Log4j2
class ScreeningAutoServiceTest {

    @Autowired
    private ProgramGenerateService programGenerateService;

    @Autowired
    private ScreeningRepository screeningRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void testGenerateForOneWeek_persistAndLog() {
        // 기준 주(오늘 포함 7일)
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(6);

        // 1) 일주일치 생성 (서비스 내부에서 날짜마다 커밋됨)
        int totalCreated = programGenerateService.generateBetween(startDate, endDate);
        log.info("일주일 총 생성된 상영회차 수 = {}", totalCreated);
        assertThat(totalCreated).isGreaterThan(0);

        // 2) 범위 내 실제로 들어갔는지 카운트(연관 엔티티 접근 안 함 → Lazy 문제 없음)
        long persistedCount = screeningRepository.findAll().stream()
                .filter(s ->
                        !s.getStartAt().toLocalDate().isBefore(startDate) &&
                                !s.getStartAt().toLocalDate().isAfter(endDate)
                )
                .count();
        log.info("일주일 범위 내 screening 실제 건수 = {}", persistedCount);
        assertThat(persistedCount).isGreaterThan(0);

        // 3) 날짜별 상세 로그 (영화/상영관 이름까지) — fetch join으로 Lazy 예외 방지
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            LocalDateTime from = d.atStartOfDay();
            LocalDateTime to = d.plusDays(1).atStartOfDay(); // [from, to) 구간

            List<Screening> dayList = em.createQuery(
                            "select s " +
                                    "from Screening s " +
                                    "join fetch s.movie m " +
                                    "join fetch s.screen sc " +
                                    "where s.startAt >= :from and s.startAt < :to " +
                                    "order by s.startAt",
                            Screening.class
                    )
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .getResultList();

            log.info("[{}] 건수 = {}", d, dayList.size());
            dayList.forEach(s -> log.info("  - 영화:{}, 상영관:{}, 시작:{}, 종료:{}",
                    s.getMovie().getTitle(),
                    s.getScreen().getName(),
                    s.getStartAt(),
                    s.getEndAt()
            ));
        }
    }
}