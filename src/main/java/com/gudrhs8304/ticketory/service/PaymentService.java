package com.gudrhs8304.ticketory.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.gudrhs8304.ticketory.domain.*;
import com.gudrhs8304.ticketory.domain.enums.BookingPayStatus;
import com.gudrhs8304.ticketory.domain.enums.MovieMediaType;
import com.gudrhs8304.ticketory.domain.enums.PaymentProvider;
import com.gudrhs8304.ticketory.domain.enums.PaymentStatus;
import com.gudrhs8304.ticketory.dto.movie.MovieDetailDTO;
import com.gudrhs8304.ticketory.dto.movie.MovieDetailResponseDTO;
import com.gudrhs8304.ticketory.dto.pay.ConfirmPaymentRequestDTO;
import com.gudrhs8304.ticketory.dto.payment.ApprovePaymentRequest;
import com.gudrhs8304.ticketory.dto.payment.PaymentOrderCreateReqDTO;
import com.gudrhs8304.ticketory.dto.payment.TossConfirmRequestDTO;
import com.gudrhs8304.ticketory.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final MovieRepository movieRepository;
    private final MovieMediaRepository movieMediaRepository;
    private final SeatRepository seatRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final TossPaymentService tossPaymentService;
    private final BookingSeatRepository bookingSeatRepository;
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);



    @Transactional
    public Payment approve(Long memberId, ApprovePaymentRequest req) {
        Booking booking = bookingRepository.findById(req.bookingId())
                .orElseThrow(() -> new IllegalArgumentException("예매가 없습니다."));

        // 본인만 결제
        if (!booking.getMember().getMemberId().equals(memberId)) {
            throw new SecurityException("본인 예매만 결제할 수 있습니다.");
        }

        // 간단 검증: 금액 일치
        if (booking.getTotalPrice() == null || req.amount() == null
                || booking.getTotalPrice().compareTo(req.amount()) != 0) {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }

        // 결제 엔티티 저장 (시뮬)
        Payment p = new Payment();
        p.setBooking(booking);
        p.setProvider(PaymentProvider.valueOf(req.method().toUpperCase())); // CARD/KAKAO/...
        p.setAmount(req.amount());
        p.setPaymentKey("SIM-" + UUID.randomUUID());
        p.setStatus(PaymentStatus.PAID);
        p.setPaidAt(LocalDateTime.now());
        paymentRepository.save(p);

        // 예매 확정
        booking.setPaymentStatus(BookingPayStatus.PAID);

        return p;
    }

    @Transactional
    public void cancel(Long memberId, Long bookingId, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("예매가 없습니다."));
        if (!booking.getMember().getMemberId().equals(memberId)) {
            throw new SecurityException("본인 예매만 취소할 수 있습니다.");
        }
        booking.setPaymentStatus(BookingPayStatus.CANCELLED);
        // (선택) 환불/좌석 해제/로그 적재는 다음 단계에서
    }

    public MovieDetailDTO getMovieDetail(Long movieId) {
        var movie = movieRepository.findByMovieIdAndDeletedAtIsNull(movieId)
                .orElseThrow(() -> new IllegalArgumentException("영화를 찾을 수 없습니다: " + movieId));

        var medias = movieMediaRepository.findByMovie_MovieIdAndMovieMediaTypeIn(
                movieId, List.of(MovieMediaType.STILL, MovieMediaType.TRAILER)
        );

        // ✅ 서비스는 내부 DTO를 리턴
        return MovieDetailDTO.of(movie, medias);
    }

    /**
     * 토스 승인 성공 → 내부 확정 처리
     */
    @Transactional
    public void confirmAndFinalize(ConfirmPaymentRequestDTO req) {
        // 1) 토스 승인(기존 서비스 호출)
        var approved = tossPaymentService.confirm(
                new TossConfirmRequestDTO(req.paymentKey(), req.orderId(), req.amount())
        );

        // 2) 결제/예매 엔티티 갱신
        Payment payment = paymentRepository.findByOrderId(req.orderId())
                .orElseThrow(() -> new IllegalArgumentException("결제 정보 없음"));
        Booking booking = payment.getBooking();

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now()); // approved 안에 시간이 있으면 그걸 사용해도 OK
        paymentRepository.save(payment);

        booking.setPaymentStatus(BookingPayStatus.PAID);
        bookingRepository.save(booking);

        // 3) seat_hold 정리
        seatHoldRepository.deleteByBookingId(booking.getBookingId());

        log.info("[TOSS] finalized bookingId={}, paymentId={}, status=PAID", booking.getBookingId(), payment.getPaymentId());

    }


    /**
     * 주문ID를 결제(PENDING)에 부착
     */
    // 주문 선생성 + 기존 PENDING 결제행에 orderId 붙이기
    @Transactional
    public void createOrderAndAttach(Long bookingId, String orderId, BigDecimal amount) {
        // 1) 기존 PENDING + paidAt null에 orderId 부착
        int updated = paymentRepository.attachOrderIdToPendingByBookingId(bookingId, orderId);

        if (updated == 0) {
            // 2) 없으면 새로 생성
            Payment p = new Payment();
            p.setBooking(bookingRepository.getReferenceById(bookingId));
            p.setAmount(amount);
            p.setProvider(PaymentProvider.TOSS);
            p.setStatus(PaymentStatus.PENDING);
            p.setOrderId(orderId);
            paymentRepository.save(p);
        } else {
            // 3) 있었다면 그 행의 금액도 최신으로 맞춰줌
            paymentRepository.updateAmountByOrderId(orderId, amount);
        }

        log.info("[ORDER] bookingId={}, orderId={}, amount={}", bookingId, orderId, amount);
    }


    /** 결제 확정 */
    @Transactional
    public void confirm(String paymentKey, String orderId, BigDecimal approvedAmount) {
        // 1) 결제행 잠금 조회
        Payment payment = paymentRepository.findByOrderIdForUpdate(orderId)
                .orElseThrow(() -> new IllegalStateException("payment not found by orderId"));

        // 2) 금액 검증
        if (payment.getAmount().compareTo(approvedAmount) != 0) {
            log.warn("[AMOUNT-MISMATCH] orderId={}, saved={}, approved={}",
                    orderId, payment.getAmount(), approvedAmount);
            throw new IllegalArgumentException("amount mismatch");
        }

        // 3) 관련 엔티티
        Booking booking   = payment.getBooking();
        Screening screening = booking.getScreening();
        Long screeningId  = screening.getScreeningId();

        // 4) 활성 홀드(만료 전) 잠금 조회
        LocalDateTime now = LocalDateTime.now();
        List<SeatHold> holds = seatHoldRepository.findActiveByHoldKeyForUpdate(
                payment.getPaymentKey(), screeningId, now
        );
        if (holds.isEmpty()) {
            // 프론트가 페이지를 오래 놔둬서 hold 만료된 경우 등
            throw new IllegalStateException("no active holds for this paymentKey/order");
        }

        // 5) booking_seat 생성 (UNIQUE(screening_id, seat_id) 충돌 시 -> 이미 선점된 좌석)
        for (SeatHold h : holds) {
            BookingSeat bs = BookingSeat.builder()
                    .booking(booking)
                    .screening(screening)
                    .seat(h.getSeat())
                    .build();
            bookingSeatRepository.save(bs);
        }

        // (선택) 좌석 상태 관리가 있으면 여기서 점유 처리
        // seatRepository.markOccupiedBySeatIds(holds.stream().map(h -> h.getSeat().getSeatId()).toList());

        // 6) 홀드 해제/삭제
        seatHoldRepository.deleteAll(holds);
        // 또는 seatHoldRepository.deleteByHoldKey(payment.getPaymentKey());

        // 7) 결제/예매 상태 업데이트
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaymentKey(paymentKey);     // 최종 결제키 저장
        payment.setPaidAt(now);
        booking.setPaymentStatus(BookingPayStatus.PAID);

        String qrPayload = buildQrPayload(booking);           // 아래 메서드
        String dataUrl   = generateQrPngDataUrl(qrPayload, 240, 240);
        booking.setQrCodeUrl(dataUrl);
    }

    /** QR에 담을 내용 (JWT 있으면 사용, 없으면 텍스트로) */
    private String buildQrPayload(Booking booking) {
        try {
            // JwtTokenProvider 를 사용 중이면 여기에 주입해서 claim 생성
            Map<String, Object> claims = new HashMap<>();
            claims.put("bookingId", booking.getBookingId());
            claims.put("screeningId", booking.getScreening().getScreeningId());
            claims.put("issuedAt", System.currentTimeMillis());
            // 예: jwtTokenProvider.generateTokenWithClaims(claims, 60 * 60);
            // JWT 안 쓰면 아래 텍스트 리턴:
            return "BOOKING:" + booking.getBookingId();
        } catch (Exception e) {
            return "BOOKING:" + booking.getBookingId();
        }
    }

    /** ZXing으로 QR PNG → data URL 생성 */
    private String generateQrPngDataUrl(String text, int width, int height) {
        try {
            var writer = new QRCodeWriter();
            var matrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height);
            try (var baos = new ByteArrayOutputStream()) {
                MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
                String b64 = Base64.getEncoder().encodeToString(baos.toByteArray());
                return "data:image/png;base64," + b64;
            }
        } catch (Exception e) {
            throw new RuntimeException("QR 생성 실패", e);
        }
    }

    // 필요시 남겨둬도 되는 보조 메서드
    @Transactional
    public void attachOrderId(long bookingId, String orderId) {
        int updated = paymentRepository.attachOrderIdToPendingByBookingId(bookingId, orderId);
        if (updated == 0) {
            throw new IllegalStateException("PENDING payment row not found: bookingId=" + bookingId);
        }
    }
}