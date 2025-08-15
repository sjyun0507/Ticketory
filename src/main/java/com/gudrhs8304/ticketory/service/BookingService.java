package com.gudrhs8304.ticketory.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.gudrhs8304.ticketory.config.JwtTokenProvider;
import com.gudrhs8304.ticketory.domain.*;
import com.gudrhs8304.ticketory.domain.enums.BookingPayStatus;
import com.gudrhs8304.ticketory.domain.enums.PaymentStatus;
import com.gudrhs8304.ticketory.dto.booking.CreateBookingRequest;
import com.gudrhs8304.ticketory.dto.booking.CreateBookingResponse;
import com.gudrhs8304.ticketory.repository.*;
import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CancelLogRepository cancelLogRepository;

    // 간단 요금 정책: 좌석 1개 12,000원
    private static final BigDecimal UNIT_PRICE = new BigDecimal("12000");

    @Transactional
    public CreateBookingResponse create(Long memberId, CreateBookingRequest req) {
        var screening = screeningRepository.findById(req.screeningId())
                .orElseThrow(() -> new IllegalArgumentException("상영이 없습니다."));

        var seats = seatRepository.findBySeatIdIn(req.seatIds());
        if (seats.size() != req.seatIds().size()) {
            throw new IllegalArgumentException("유효하지 않은 좌석이 포함되어 있습니다.");
        }
        // 같은 상영관 좌석인지 최소 검증(선택)
        Long screenId = screening.getScreen().getScreenId();
        boolean allSameScreen = seats.stream().allMatch(s -> s.getScreen().getScreenId().equals(screenId));
        if (!allSameScreen) throw new IllegalArgumentException("다른 상영관 좌석이 섞여 있습니다.");

        // 이미 예약된 좌석인지 충돌 검사
        for (Long seatId : req.seatIds()) {
            if (bookingSeatRepository.existsByScreening_ScreeningIdAndSeat_SeatId(req.screeningId(), seatId)) {
                throw new IllegalStateException("이미 예약된 좌석이 포함되어 있습니다. (seatId=" + seatId + ")");
            }
        }

        // 총액 계산
        BigDecimal total = UNIT_PRICE.multiply(BigDecimal.valueOf(seats.size()));

        // Booking 저장 (기본 PENDING or CREATED)
        Booking booking = new Booking();
        booking.setMember(new Member(memberId)); // member 엔티티에 PK만 세팅 가능한 생성자 필요하거나 setter로 id만 세팅
        booking.setScreening(screening);
        booking.setTotalPrice(total);
        booking.setPaymentStatus(BookingPayStatus.PENDING);
        booking.setBookingTime(LocalDateTime.now());
        booking = bookingRepository.save(booking);

        // BookingSeat 저장
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

    public Booking getMyBooking(Long memberId, Long bookingId) {
        return bookingRepository.findByBookingIdAndMember_MemberId(bookingId, memberId)
                .orElseThrow(() -> new SecurityException("본인 예매만 조회할 수 있습니다."));
    }

    public String buildTicketToken(Long bookingId, Long memberId, Long screeningId) {
        // 기존 JwtTokenProvider에 이미 구현되어 있음: createTicketToken(bookingId, memberId, screeningId)
        return jwtTokenProvider.createTicketToken(bookingId, memberId, screeningId);
    }

    public String buildQrPngDataUrl(String content) {
        try {
            var qrWriter = new QRCodeWriter();
            var bitMatrix = qrWriter.encode(content, BarcodeFormat.QR_CODE, 240, 240);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", out);
            String base64 = Base64.getEncoder().encodeToString(out.toByteArray());
            return "data:image/png;base64," + base64;
        } catch (WriterException | java.io.IOException e) {
            throw new RuntimeException("QR 생성 실패", e);
        }
    }

    @Transactional
    public void cancel(Long memberId, Long bookingId, @Nullable String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("예약이 없습니다."));

        // (권한 체크 필요 시 추가)

        // 예약-좌석 매핑 삭제
        bookingSeatRepository.deleteByBookingId(bookingId);

        // 예약 상태 변경
        booking.setPaymentStatus(BookingPayStatus.CANCELLED);
        bookingRepository.save(booking);

        cancelLogRepository.save(CancelLog.ofMemberCancel(booking, memberId, reason));
    }
}
