package com.gudrhs8304.ticketory.batch;

import com.gudrhs8304.ticketory.feature.movie.Movie;
import com.gudrhs8304.ticketory.feature.screen.ScreenRepository;
import com.gudrhs8304.ticketory.feature.screen.Screen;
import com.gudrhs8304.ticketory.feature.screening.ScreeningRepository;
import com.gudrhs8304.ticketory.feature.screening.Screening;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgramGenerateService {

    private final ScreenRepository screenRepository;
    private final ScreeningRepository screeningRepository;
    private final ScreenProgramTemplateRepository templateRepository;
    private final ScreenProgramSlotRepository slotRepository;

    private boolean matchesWeekdayMask(String mask, DayOfWeek dow) {
        if (mask == null || mask.length() != 7) return true;
        int idx = switch (dow) {
            case MONDAY -> 0; case TUESDAY -> 1; case WEDNESDAY -> 2; case THURSDAY -> 3;
            case FRIDAY -> 4; case SATURDAY -> 5; case SUNDAY -> 6;
        };
        char c = mask.charAt(idx);
        return (c == 'Y' || c == 'y' || c == '1' || c == 'T');
    }

    @Transactional
    public int generateForScreenOnDate(Long screenId, LocalDate date) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new IllegalArgumentException("screen not found: " + screenId));

        int created = 0;
        var templates = templateRepository.findByScreenAndEnabledIsTrue(screen);

        for (ScreenProgramTemplate tmpl : templates) {
            var slots = slotRepository.findByTemplateWithJoinsOrderByStartTimeAscPriorityAsc(tmpl);

            for (ScreenProgramSlot slot : slots) {
                // 요일 마스크 적용
                if (!matchesWeekdayMask(slot.getWeekdays(), date.getDayOfWeek())) continue;

                Movie mv = slot.getMovie();
                // 영화 상태/삭제 여부
                if (mv == null || !Boolean.TRUE.equals(mv.getStatus())) {
                    log.info("[program] skip slot {}: movie inactive", slot.getSlotId());
                    continue;
                }

                Integer runtime = mv.getRunningMinutes();
                if (runtime == null || runtime <= 0) {
                    log.warn("[program] skip slot {}: movie runtime missing", slot.getSlotId());
                    continue;
                }

                LocalDateTime startAt = LocalDateTime.of(date, slot.getStartTime());
                LocalDateTime endAt   = startAt.plusMinutes(runtime);

                // 청소 시간까지 고려(일관성)
                int clean = screen.getCleanMinutes() != null ? screen.getCleanMinutes() : 0;
                LocalDateTime endWithClean = endAt.plusMinutes(clean);

                if (screeningRepository.existsByScreen_ScreenIdAndStartAt(screen.getScreenId(), startAt)) {
                    log.info("[program] exists same startAt. skip {} (screen {})", startAt, screen.getScreenId());
                    continue;
                }
                if (screeningRepository.existsOverlap(screen.getScreenId(), startAt, endWithClean)) {
                    log.info("[program] overlap. skip {}~{} (screen {})", startAt, endWithClean, screen.getScreenId());
                    continue;
                }

                screeningRepository.save(
                        Screening.builder()
                                .screen(screen)
                                .movie(mv)
                                .startAt(startAt)
                                .endAt(endAt)
                                .isBooking(false) // NOT NULL 컬럼 방지
                                .build()
                );
                created++;
            }
        }
        return created;
    }

    @Transactional
    public int generateForScreenBetween(Long screenId, LocalDate from, LocalDate toInclusive) {
        int sum = 0;
        for (LocalDate d = from; !d.isAfter(toInclusive); d = d.plusDays(1)) {
            sum += generateForScreenOnDate(screenId, d);
        }
        return sum;
    }

    @Transactional
    public int generateForAllActiveOn(LocalDate date) {
        int total = 0;
        var screens = screenRepository.findByIsActiveTrue();
        for (Screen sc : screens) {
            total += generateForScreenOnDate(sc.getScreenId(), date);
        }
        return total;
    }

    /** 날짜마다 '새 트랜잭션'으로 커밋 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int generateForDate(LocalDate date) {
        int created = 0;
        List<Screen> screens = screenRepository.findByIsActiveTrue();

        for (Screen sc : screens) {
            var templates = templateRepository.findByScreenAndEnabledIsTrue(sc);
            for (ScreenProgramTemplate t : templates) {
                // ⚠️ fetch join 사용
                var slots = slotRepository.findByTemplateWithJoinsOrderByStartTimeAscPriorityAsc(t);

                for (ScreenProgramSlot slot : slots) {

                    if (!matchesWeekdayMask(slot.getWeekdays(), date.getDayOfWeek())) continue;

                    Movie mv = slot.getMovie(); // fetch됨
                    Integer runtime = (mv != null) ? mv.getRunningMinutes() : null;
                    if (mv == null || !Boolean.TRUE.equals(mv.getStatus())) continue;

                    LocalDateTime startAt = LocalDateTime.of(date, slot.getStartTime());
                    LocalDateTime endAt   = startAt.plusMinutes(runtime);

                    if (screeningRepository.existsByScreen_ScreenIdAndStartAt(sc.getScreenId(), startAt)) continue;

                    LocalDateTime endWithClean = endAt.plusMinutes(
                            sc.getCleanMinutes() != null ? sc.getCleanMinutes() : 0
                    );
                    if (screeningRepository.existsOverlap(sc.getScreenId(), startAt, endWithClean)) continue;

                    screeningRepository.save(
                            Screening.builder()
                                    .movie(mv)
                                    .screen(sc)
                                    .startAt(startAt)
                                    .endAt(endAt)
                                    .isBooking(false)
                                    .build()
                    );
                    created++;
                }
            }
        }
        return created;
    }

    /** 날짜 범위 일괄 생성 (각 날짜는 REQUIRES_NEW로 커밋) */
    public int generateBetween(LocalDate from, LocalDate toInclusive) {
        int sum = 0;
        for (LocalDate d = from; !d.isAfter(toInclusive); d = d.plusDays(1)) {
            sum += generateForDate(d);
        }
        return sum;
    }

    private boolean matchesWeekdays(String mask, DayOfWeek dow) {
        if (mask == null || mask.length() != 7) return true;
        int idx = switch (dow) {
            case MONDAY -> 0; case TUESDAY -> 1; case WEDNESDAY -> 2; case THURSDAY -> 3;
            case FRIDAY -> 4; case SATURDAY -> 5; case SUNDAY -> 6;
        };
        char c = mask.charAt(idx);
        return (c == 'Y' || c == 'y' || c == '1' || c == 'T');
    }
}