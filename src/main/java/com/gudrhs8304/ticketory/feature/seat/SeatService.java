package com.gudrhs8304.ticketory.feature.seat;

import com.gudrhs8304.ticketory.feature.booking.BookingSeatRepository;
import com.gudrhs8304.ticketory.feature.member.enums.HoldStatus;
import com.gudrhs8304.ticketory.feature.member.enums.SeatStatus;
import com.gudrhs8304.ticketory.feature.member.enums.SeatStatusType;
import com.gudrhs8304.ticketory.feature.screen.domain.Seat;
import com.gudrhs8304.ticketory.feature.screen.domain.SeatHold;
import com.gudrhs8304.ticketory.feature.screening.ScreeningRepository;
import com.gudrhs8304.ticketory.feature.screening.SeatHoldRepository;
import com.gudrhs8304.ticketory.feature.screening.SeatRepository;
import com.gudrhs8304.ticketory.feature.screening.domain.Screening;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final BookingSeatRepository bookingSeatRepository;

    private static final int DEFAULT_HOLD_SECONDS = 120;

    // --- 좌석 맵 조회 (screen seats + holds + booked overlay)
    @Transactional(readOnly = true)
    public SeatMapResponseDTO getSeatMap(Long screeningId) {
        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new IllegalArgumentException("상영을 찾을 수 없습니다.: " + screeningId));
        Long screenId = screening.getScreen().getScreenId();
        int rowCount = screening.getScreen().getRowCount();
        int colCount = screening.getScreen().getColCount();

        // 스크린의 모든 좌석(전역 상태: AVAILABLE/DISABLED)
        List<Seat> seats = seatRepository.findAllByScreenId(screenId);

        LocalDateTime now = LocalDateTime.now();

        Set<Long> holdSeatIds = seatHoldRepository.findActiveByScreening(screeningId, now)
                .stream()
                .map(h -> h.getSeat().getSeatId())
                .collect(Collectors.toSet());

        // 프로젝트의 '예매 확정 좌석' 테이블에 맞게 이 메서드 구현
        Set<Long> bookedSeatIds = findBookedSeatIds(screeningId);

        // ✅ rows 배열 생성 (A, B, C, ...)
        List<String> rows = new ArrayList<>();
        for (int i = 0; i < rowCount; i++) {
            rows.add(String.valueOf((char) ('A' + i)));
        }

        // ✅ 프론트 명칭으로 status 매핑
        List<SeatMapResponseDTO.SeatItem> items = seats.stream().map(s -> {
            boolean disabled = s.getStatus() == SeatStatus.DISABLED;
            SeatStatusType mapped;
            if (disabled) {
                mapped = SeatStatusType.BLOCKED; // 미운영
            } else if (bookedSeatIds.contains(s.getSeatId())) {
                mapped = SeatStatusType.SOLD; // 예매완료
            } else if (holdSeatIds.contains(s.getSeatId())) {
                mapped = SeatStatusType.HELD; // 임시 선점
            } else {
                mapped = SeatStatusType.AVAILABLE; // 선택 가능
            }

            return SeatMapResponseDTO.SeatItem.builder()
                    .seatId(s.getSeatId())
                    .rowLabel(s.getRowLabel())
                    .colNumber(s.getColNumber())
                    .status(mapped)
                    .type(s.getSeatType() != null ? s.getSeatType().name() : "NORMAL")
                    .build();
        }).toList();

        return SeatMapResponseDTO.builder()
                .screeningId(screeningId)
                .screenId(screenId)
                .rows(rows)
                .cols(colCount)
                .seats(items)
                .build();
    }

    // --- HOLD 생성
    @Transactional
    public SeatHoldResponseDTO createHold(SeatHoldRequestDTO req) {
        if (req.getScreeningId() == null) throw new IllegalArgumentException("screeningId가 필요합니다.");
        if (req.getSeatIds() == null || req.getSeatIds().isEmpty())
            throw new IllegalArgumentException("seatIds가 비어있습니다.");

        int ttl = Optional.ofNullable(req.getHoldSeconds()).orElse(DEFAULT_HOLD_SECONDS);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expires = now.plusSeconds(ttl);

        Screening screening = screeningRepository.findById(req.getScreeningId())
                .orElseThrow(() -> new IllegalArgumentException("상영이 존재하지 않습니다."));
        Long screenId = screening.getScreen().getScreenId();

        // 좌석이 모두 해당 스크린에 속하는지 검증
        boolean ok = seatRepository.allSeatsBelongToScreen(req.getSeatIds(),  screenId);
        if (!ok) throw new IllegalArgumentException("좌석이 상영관에 속하지 않습니다.");

        // 경합 방지: 좌석 잠금
        var seats = seatRepository.lockSeatsForUpdate(req.getSeatIds());

        // 이미 BOOKED 좌석 포함 여부
        Set<Long> bookedSeatIds = bookingSeatRepository.findSeatIdsByScreeningPaid(req.getScreeningId());
        if (seats.stream().anyMatch(s -> bookedSeatIds.contains(s.getSeatId())))
            throw new IllegalStateException("이미 예매 완료된 좌석이 포함되어 있습니다.");

        // 이미 유효한 HOLD 포함 여부
        if (seatHoldRepository.existsAnyActiveHold(req.getScreeningId(), req.getSeatIds(), now))
            throw new IllegalStateException("이미 홀드된 좌석이 포함되어 있습니다.");

        // HOLD 생성
        List<Long> holdIds = new ArrayList<>();
        for (Seat seat : seats) {
            SeatHold h = SeatHold.builder()
                    .screening(screening)
                    .seat(seat)
                    .expiresAt(expires)
                    .holdKey(req.getHoldKey())
                    .holdTime(ttl)
                    .status(HoldStatus.HOLD)
                    .build();
            seatHoldRepository.save(h);
            holdIds.add(h.getHoldId());
        }

        return SeatHoldResponseDTO.builder()
                .holdIds(holdIds)
                .expiresAt(expires)
                .build();
    }

    // --- HOLD 연장
    @Transactional
    public SeatHoldResponseDTO extendHold(Long holdId, Integer holdSeconds) {
        int ttl = Optional.ofNullable(holdSeconds).orElse(DEFAULT_HOLD_SECONDS);
        SeatHold h = seatHoldRepository.findById(holdId)
                .orElseThrow(() -> new IllegalArgumentException("holdId가 올바르지 않습니다."));
        LocalDateTime newExp = LocalDateTime.now().plusSeconds(ttl);
        h.setExpiresAt(newExp);
        h.setUpdatedAt(LocalDateTime.now());
        return SeatHoldResponseDTO.builder()
                .holdIds(List.of(h.getHoldId()))
                .expiresAt(newExp)
                .build();
    }

    // --- HOLD 해제
    @Transactional
    public void releaseHold(Long holdId) {
        seatHoldRepository.deleteById(holdId); // 존재하지 않아도 무시
    }

    // --- 만료 정리(배치/스케줄러용)
    @Transactional
    public int purgeExpiredHolds() {
        return seatHoldRepository.deleteExpired(LocalDateTime.now());
    }

    // ====== 여기만 너희 프로젝트의 확정 예매 테이블에 맞춰 구현 ======
    private Set<Long> findBookedSeatIds(Long screeningId) {
        // TODO: 예) reservation_seat 테이블에서 screeningId=... AND paid=true 로 seatId 조회
        return bookingSeatRepository.findSeatIdsByScreeningPaid(screeningId);
    }
}
