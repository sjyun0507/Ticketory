package com.gudrhs8304.ticketory.service;

import com.gudrhs8304.ticketory.domain.*;
import com.gudrhs8304.ticketory.domain.enums.BookingPayStatus;
import com.gudrhs8304.ticketory.domain.enums.PaymentProvider;
import com.gudrhs8304.ticketory.domain.enums.PaymentStatus;
import com.gudrhs8304.ticketory.dto.booking.InitBookingRequestDTO;
import com.gudrhs8304.ticketory.dto.booking.InitBookingResponseDTO;
import com.gudrhs8304.ticketory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BookingOrchestrator {

    private final ScreeningRepository screeningRepo;
    private final SeatRepository seatRepo;
    private final SeatHoldRepository seatHoldRepo;
    private final BookingRepository bookingRepo;
    private final BookingSeatRepository bookingSeatRepo;
    private final PaymentRepository paymentRepo;
    private final MemberRepository memberRepo;

    private static final int DEFAULT_HOLD_SECONDS = 120;

    @Transactional
    public InitBookingResponseDTO initBooking(Long memberId, String idemKey, InitBookingRequestDTO req) {
        if (req.screeningId() == null || req.seatIds() == null || req.seatIds().isEmpty()) {
            throw new IllegalArgumentException("screeningId/seatIds는 필수입니다.");
        }
        // 좌석 수 == 인원 수(성인+청소년) 체크 (원하면 유지/삭제)
        int adults = Optional.ofNullable(req.counts()).map(m -> m.getOrDefault("adult", 0)).orElse(0);
        int teens  = Optional.ofNullable(req.counts()).map(m -> m.getOrDefault("teen", 0)).orElse(0);
        if (req.seatIds().size() != adults + teens) {
            throw new IllegalArgumentException("선택 좌석 수와 인원 수가 일치하지 않습니다.");
        }

        int holdSec = DEFAULT_HOLD_SECONDS;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusSeconds(holdSec);

        // 1) 상영 로드
        Screening screening = screeningRepo.findById(req.screeningId())
                .orElseThrow(() -> new IllegalArgumentException("상영 정보 없음"));
        Long screenId = screening.getScreen().getScreenId();

        // 2) 좌석 소속 검증 + 잠금
        boolean ok = seatRepo.allSeatsBelongToScreen(req.seatIds(), screenId);
        if (!ok) throw new IllegalArgumentException("선택 좌석 중 상영관 외 좌석 포함");
        List<Seat> seats = seatRepo.lockSeatsForUpdate(req.seatIds());

        // 3) 중복 점유 검사(확정/홀드)
        Set<Long> bookedSeatIds = bookingSeatRepo.findSeatIdsByScreeningPaidOrExists(req.screeningId());
        if (seats.stream().anyMatch(s -> bookedSeatIds.contains(s.getSeatId())))
            throw new IllegalStateException("이미 예매 완료된 좌석 포함");

        boolean hasActiveHold = seatHoldRepo.existsAnyActiveHold(req.screeningId(), req.seatIds(), now);
        if (hasActiveHold) throw new IllegalStateException("이미 홀드된 좌석 포함");

        // 4) HOLD 생성
        List<Long> holdIds = new ArrayList<>();
        for (Seat seat : seats) {
            SeatHold h = SeatHold.builder()
                    .screening(screening)
                    .seat(seat)
                    .holdTime(holdSec)
                    .expiresAt(expiresAt)
                    .holdKey(idemKey)
                    .build();
            seatHoldRepo.save(h);
            holdIds.add(h.getHoldId());
        }

        // 5) 가격 계산 (예: adult 14000, teen 11000)
        int cntAdult = Optional.ofNullable(req.counts()).map(m -> m.getOrDefault("adult", 0)).orElse(0);
        int cntTeen  = Optional.ofNullable(req.counts()).map(m -> m.getOrDefault("teen", 0)).orElse(0);
        int total = cntAdult * 14000 + cntTeen * 11000;

        // 6) BOOKING
        Member memberRef = memberRepo.getReferenceById(memberId);
        Booking booking = Booking.builder()
                .member(memberRef)
                .screening(screening)
                .bookingTime(now)
                .totalPrice(new BigDecimal(total))
                .paymentStatus(BookingPayStatus.PENDING)
                .build();
        bookingRepo.save(booking);

        // 7) BOOKING_SEAT (screening_id + seat_id UNIQUE)
        for (Seat seat : seats) {
            BookingSeat bs = BookingSeat.builder()
                    .booking(booking)
                    .screening(screening)
                    .seat(seat)
                    .build();
            bookingSeatRepo.save(bs);
        }

        // 8) PAYMENT(PENDING)  — orderId(필수)와 paymentKey(선택) 세팅
        String orderId = newOrderId(booking.getBookingId(), idemKey);
        String paymentKey = (idemKey == null || idemKey.isBlank())
                ? "IDEMP-" + UUID.randomUUID()
                : idemKey;

        PaymentProvider provider = PaymentProvider.CARD;
        if (req.provider() != null) {
            try { provider = PaymentProvider.valueOf(req.provider()); } catch (Exception ignore) {}
        }

        Payment pay = Payment.builder()
                .booking(booking)
                .orderId(orderId)                 // ★ NOT NULL/UNIQUE
                .paymentKey(paymentKey)           // (선택) 프런트 Idempotency-Key 재사용
                .provider(provider)
                .status(PaymentStatus.PENDING)
                .amount(new BigDecimal(total))
                .build();
        paymentRepo.save(pay);

        return new InitBookingResponseDTO(
                booking.getBookingId(),
                pay.getPaymentId(),
                holdIds,
                expiresAt.toString(),
                pay.getStatus().name(),
                pay.getProvider().name(),
                total
        );
    }

    /** 주문번호 생성: 예) ORD-20250825-<bookingId>-AB12CD34 */
    private String newOrderId(Long bookingId, String idemKey) {
        String ymd = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String shortUuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "ORD-" + ymd + "-" + bookingId + "-" + shortUuid;
    }

    @Transactional
    public void releaseHold(Long memberId, Long bookingId) {
        // (선택) 본인 예매만 허용
        Booking booking = bookingRepo.findById(bookingId).orElse(null);
        if (booking == null) return;
        Long ownerId = (booking.getMember() != null) ? booking.getMember().getMemberId() : null;
        if (memberId != null && !Objects.equals(ownerId, memberId)) {
            return;
        }

        // 1) 좌석 상태 AVAILABLE로
        seatRepo.releaseSeatsByBookingId(bookingId);

        // 2) seat_hold 해제 (행 삭제 또는 releasedAt 마킹)
        seatHoldRepo.deleteByBookingId(bookingId);
        // 또는: seatHoldRepo.markReleasedByBookingId(bookingId);
    }
}