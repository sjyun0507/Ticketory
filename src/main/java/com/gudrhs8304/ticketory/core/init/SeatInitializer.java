package com.gudrhs8304.ticketory.core.init;

import com.gudrhs8304.ticketory.feature.screen.Screen;
import com.gudrhs8304.ticketory.feature.seat.SeatStatus;
import com.gudrhs8304.ticketory.feature.seat.SeatType;
import com.gudrhs8304.ticketory.feature.seat.Seat;
import com.gudrhs8304.ticketory.feature.screen.ScreenRepository;
import com.gudrhs8304.ticketory.feature.seat.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeatInitializer implements CommandLineRunner {

    private final SeatRepository seatRepository;
    private final ScreenRepository screenRepository;

    @Override
    public void run(String... args) {
        log.info("=== SeatInitializer 시작 ===");

        var screens = screenRepository.findAll();
        for (Screen screen : screens) {
            Long sid = screen.getScreenId();

            // 이미 좌석이 있으면 skip (중복방지)
            if (seatRepository.existsByScreen_ScreenId(sid)) {
                log.info("Screen {} 은 이미 좌석이 있음, skip", sid);
                continue;
            }

            int rows = screen.getRowCount();
            int cols = screen.getColCount();

            for (int r = 0; r < rows; r++) {
                String rowLabel = String.valueOf((char) ('A' + r)); // A,B,C...

                for (int c = 1; c <= cols; c++) {
                    Seat seat = Seat.builder()
                            .screen(screen)
                            .rowLabel(rowLabel)
                            .colNumber(c)
                            .seatType(SeatType.NORMAL)
                            .status(SeatStatus.AVAILABLE) // 전역 상태
                            .build();

                    seatRepository.save(seat);
                }
            }

            log.info("Screen {} 좌석 {} x {} 개 생성 완료", sid, rows, cols);
        }

        log.info("=== SeatInitializer 완료 ===");
    }
}