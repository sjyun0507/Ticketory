package com.gudrhs8304.ticketory.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.gudrhs8304.ticketory.config.JwtTokenProvider;
import com.gudrhs8304.ticketory.domain.*;
import com.gudrhs8304.ticketory.domain.enums.BookingPayStatus;
import com.gudrhs8304.ticketory.domain.enums.PricingKind;
import com.gudrhs8304.ticketory.dto.booking.CreateBookingRequest;
import com.gudrhs8304.ticketory.dto.booking.CreateBookingResponse;
import com.gudrhs8304.ticketory.repository.*;
import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final PricingService pricingService;
    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final JwtTokenProvider jwtTokenProvider;

    private static final BigDecimal DEFAULT_UNIT_PRICE = new BigDecimal("12000");

    @Transactional
    public CreateBookingResponse create(Long memberId, CreateBookingRequest req) {
        var screening = screeningRepository.findById(req.screeningId())
                .orElseThrow(() -> new IllegalArgumentException("상영이 없습니다."));

        var seats = seatRepository.findBySeatIdIn(req.seatIds());
        if (seats.size() != req.seatIds().size()) {
            throw new IllegalArgumentException("유효하지 않은 좌석이 포함되어 있습니다.");
        }

        Long screenId = screening.getScreen().getScreenId();
        boolean allSameScreen = seats.stream()
                .allMatch(s -> s.getScreen().getScreenId().equals(screenId));
        if (!allSameScreen) {
            throw new IllegalArgumentException("다른 상영관 좌석이 섞여 있습니다.");
        }

        for (Long seatId : req.seatIds()) {
            if (bookingSeatRepository.existsByScreening_ScreeningIdAndSeat_SeatId(req.screeningId(), seatId)) {
                throw new IllegalStateException("이미 예약된 좌석이 포함되어 있습니다. (seatId=" + seatId + ")");
            }
        }

        // ✅ 상영관 단가 계산 (성인 기준 예시. 필요하면 req에 kind 추가)
        BigDecimal unitPrice;
        try {
            unitPrice = pricingService.resolvePrice(screenId, com.gudrhs8304.ticketory.domain.enums.PricingKind.ADULT, LocalDateTime.now());
            if (unitPrice == null) unitPrice = DEFAULT_UNIT_PRICE;
        } catch (Exception e) {
            unitPrice = DEFAULT_UNIT_PRICE;
        }

        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(seats.size()));

        Booking booking = new Booking();
        booking.setMember(new Member(memberId));
        booking.setScreening(screening);
        booking.setTotalPrice(total);
        booking.setPaymentStatus(BookingPayStatus.PENDING);
        booking.setBookingTime(LocalDateTime.now());
        booking = bookingRepository.save(booking);

        for (Seat seat : seats) {
            BookingSeat bs = new BookingSeat();
            bs.setBooking(booking);
            bs.setScreening(screening);
            bs.setSeat(seat);
            bookingSeatRepository.save(bs);
        }

        return new CreateBookingResponse(
                booking.getBookingId(),
                screening.getScreeningId(),
                req.seatIds(),
                total,
                booking.getPaymentStatus().name()
        );
    }

    @Transactional
    public Booking getMyBooking(Long memberId, Long bookingId) {
        // 본인 소유 예매만 조회
        return bookingRepository.findByBookingIdAndMember_MemberId(bookingId, memberId)
                .orElseThrow(() -> new SecurityException("본인 예매만 조회할 수 있습니다."));
    }

    @Transactional
    public void cancel(Long memberId, Long bookingId, @Nullable String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("예약이 없습니다."));

        // (선택) 본인 확인
        if (memberId != null && !booking.getMember().getMemberId().equals(memberId)) {
            throw new SecurityException("본인 예매만 취소할 수 있습니다.");
        }

        // 좌석 매핑 삭제 (레포지토리에 메서드가 있어야 합니다)
        bookingSeatRepository.deleteByBookingId(bookingId);

        // 상태 변경
        booking.setPaymentStatus(BookingPayStatus.CANCELLED);
        bookingRepository.save(booking);

        // 취소 로그 남기기(선택)
//        cancelLogRepository.save(CancelLog.ofMemberCancel(booking, memberId, reason));
    }
}