package com.gudrhs8304.ticketory.feature.admin;

import com.gudrhs8304.ticketory.feature.screen.domain.Screen;

import com.gudrhs8304.ticketory.feature.member.enums.SeatStatus;
import com.gudrhs8304.ticketory.feature.member.enums.SeatType;
import com.gudrhs8304.ticketory.feature.screen.dto.CreateScreenRequest;
import com.gudrhs8304.ticketory.feature.screen.dto.UpdateScreenRequest;
import com.gudrhs8304.ticketory.feature.screen.domain.Seat;
import com.gudrhs8304.ticketory.feature.screen.ScreenRepository;
import com.gudrhs8304.ticketory.feature.screening.SeatRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminScreenService {

    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;

    @Transactional
    public Screen createScreen(CreateScreenRequest req) {
        // 1) 스크린 생성
        // 1) 스크린 생성
        Screen screen = Screen.builder()
                .name(req.name())
                .rowCount(req.rows()) // Screen 엔티티에 rowCount, colCount 필드가 있어야 함
                .colCount(req.cols())
                .location(req.location()) // DTO에 location 있으면
                .description(req.description()) // DTO에 description 있으면
                .isActive(Boolean.TRUE.equals(req.isActive())) // null이면 false 방지
                .build();

        screen = screenRepository.save(screen);

        // 2) 좌석 생성
        generateSeats(screen, req.rows(), req.cols());
        return screen;
    }

    @Transactional
    public Screen updateScreen(Long screenId, UpdateScreenRequest req) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new IllegalArgumentException("스크린이 없습니다. id=" + screenId));

        if (req.name() != null && !req.name().isBlank()) screen.setName(req.name());
        if (req.location() != null) screen.setLocation(req.location());
        if (req.description() != null) screen.setDescription(req.description());
        if (req.isActive() != null) screen.setIsActive(req.isActive());

        // rows/cols가 둘 다 들어왔을 때만 레이아웃 재생성
        if (req.rows() != null && req.cols() != null) {
            screen.setRowCount(req.rows());
            screen.setColCount(req.cols());

            // 1) 기존 좌석 벌크 삭제 + 즉시 flush
            seatRepository.deleteByScreenId(screenId);

            // 2) 재생성
            generateSeats(screen, req.rows(), req.cols());
        }

        return screen;
    }

    @Transactional
    public void deleteScreen(Long screenId) {
        seatRepository.deleteByScreenId(screenId);
        screenRepository.deleteById(screenId);
    }

    /** A.. rows, 1..cols 로 좌석 생성 (중복 안전장치 포함) */
    private void generateSeats(Screen screen, int rows, int cols) {
        Long sid = screen.getScreenId();
        for (int r = 0; r < rows; r++) {
            String rowLabel = String.valueOf((char) ('A' + r)); // A,B,C...
            for (int c = 1; c <= cols; c++) {
                // 혹시 모를 중복에 대한 방어 (idempotent)
                if (seatRepository.existsByScreen_ScreenIdAndRowLabelAndColNumber(sid, rowLabel, c)) continue;

                Seat seat = Seat.builder()
                        .screen(screen)
                        .rowLabel(rowLabel)
                        .colNumber(c)
                        .seatType(SeatType.NORMAL)
                        .status(SeatStatus.AVAILABLE)
                        .build();

                seatRepository.save(seat);
            }
        }
    }
}