package com.gudrhs8304.ticketory.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.gudrhs8304.ticketory.domain.*;
import com.gudrhs8304.ticketory.domain.enums.BookingPayStatus;
import com.gudrhs8304.ticketory.domain.enums.PaymentProvider;
import com.gudrhs8304.ticketory.domain.enums.PaymentStatus;
import com.gudrhs8304.ticketory.domain.enums.PointChangeType;
import com.gudrhs8304.ticketory.dto.pay.ConfirmPaymentRequestDTO;
import com.gudrhs8304.ticketory.dto.payment.ApprovePaymentRequest;
import com.gudrhs8304.ticketory.dto.payment.TossConfirmRequestDTO;
import com.gudrhs8304.ticketory.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

import java.util.Base64; // QR data URL 인코딩

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final SeatRepository seatRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final TossPaymentService tossPaymentService;
    private final BookingSeatRepository bookingSeatRepository;
    private final PointService pointService;

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    /** (시뮬) 간단 승인 API */
    @Transactional
    public Payment approve(Long memberId, ApprovePaymentRequest req) {
        Booking booking = bookingRepository.findById(req.bookingId())
                .orElseThrow(() -> new IllegalArgumentException("예매가 없습니다."));

        if (!booking.getMember().getMemberId().equals(memberId)) {
            throw new SecurityException("본인 예매만 결제할 수 있습니다.");
        }

        if (booking.getTotalPrice() == null || req.amount() == null
                || booking.getTotalPrice().compareTo(req.amount()) != 0) {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }

        Payment p = new Payment();
        p.setBooking(booking);
        p.setProvider(PaymentProvider.valueOf(req.method().toUpperCase())); // CARD/KAKAO/...
        p.setAmount(req.amount());                 // 실결제금액
        p.setPaymentKey("SIM-" + UUID.randomUUID());
        p.setStatus(PaymentStatus.PAID);
        p.setPaidAt(LocalDateTime.now());
        paymentRepository.save(p);

        booking.setPaymentStatus(BookingPayStatus.PAID);
        return p;
    }

    @Transactional
    public void cancel(Long memberId, Long bookingId, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("예매가 없습니다."));

        // 본인 또는 관리자만 허용하고 싶다면 권한 체크 추가
        if (booking.getMember() == null || !Objects.equals(booking.getMember().getMemberId(), memberId)) {
            throw new SecurityException("본인 예매만 취소할 수 있습니다.");
        }

        // 상영 시작 전 규정 체크 (원하면 완화/제거)
        LocalDateTime startAt = booking.getScreening().getStartAt();
        if (startAt != null && !LocalDateTime.now().isBefore(startAt)) {
            throw new IllegalStateException("상영 시작 이후에는 취소할 수 없습니다.");
        }

        // 가장 최근 결제(있을 수도/없을 수도)
        Payment payment = paymentRepository
                .findTopByBooking_BookingIdOrderByPaymentIdDesc(bookingId)
                .orElse(null);

        // === 케이스 A: 결제 승인 전(미결제 또는 PENDING 등) 취소 ===
        if (payment == null || payment.getStatus() == PaymentStatus.PENDING) {
            // 1) PAYMENT 상태
            if (payment != null) {
                payment.setStatus(PaymentStatus.CANCELLED);
                payment.setCancelledAt(LocalDateTime.now());
                paymentRepository.save(payment);
            }
            // 2) BOOKING 상태
            booking.setPaymentStatus(BookingPayStatus.CANCELLED);
            bookingRepository.save(booking);

            // 3) 좌석 해제 — HOLD 제거
            //   - holdKey로 지우는 구현이 있다면 그것도 가능. 여기서는 bookingId 기준 메서드 사용(이미 보유)
            seatHoldRepository.deleteByBookingId(bookingId);

            // (선택) 좌석 점유상태를 별도 테이블로 관리한다면 AVAILABLE 처리
            // seatRepository.releaseSeatsByBookingId(bookingId);

            log.info("[CANCEL:PRE-APPROVE] bookingId={}, reason={}", bookingId, reason);
            return;
        }

        // === 케이스 B: 결제 승인 후(PAID) 취소/환불 ===
        if (payment.getStatus() == PaymentStatus.PAID) {
            // 1) PG 환불 호출(연동 시)
            try {
                // tossPaymentService.cancel(payment.getPaymentKey(), payment.getAmount(), reason); // 필요 시 구현
            } catch (Exception e) {
                // 환불 연동 실패 시 롤백할지/재시도할지 정책 결정
                throw new IllegalStateException("PG 환불 실패: " + e.getMessage(), e);
            }

            // 2) PAYMENT 상태
            payment.setStatus(PaymentStatus.CANCELLED);
            payment.setCancelledAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // 3) BOOKING 상태
            booking.setPaymentStatus(BookingPayStatus.CANCELLED);
            bookingRepository.save(booking);

            // 4) 좌석 해제 — BOOKING_SEAT 삭제(해당 회차 좌석 재개방)
            bookingSeatRepository.deleteByBooking_BookingId(bookingId);

            // (선택) 좌석 점유상태 별도 관리 시 AVAILABLE 처리
            // seatRepository.releaseSeatsByBookingId(bookingId);

            // 5) 포인트 롤백
            //   사용 포인트 = 총액 - 실결제금액
            int usedPoints = booking.getTotalPrice()
                    .subtract(payment.getAmount())
                    .max(BigDecimal.ZERO)
                    .intValue();
            if (usedPoints > 0) {
                pointService.applyAndLog(
                        booking.getMember(),
                        booking,
                        payment,
                        PointChangeType.CANCEL,   // 환급
                        +usedPoints,
                        "예매 취소: 포인트 환급"
                );
            }

            //   적립 회수 = 실결제금액 * 5% (내림)
            int earned = payment.getAmount()
                    .multiply(BigDecimal.valueOf(0.05))
                    .setScale(0, RoundingMode.FLOOR)
                    .intValue();
            if (earned > 0) {
                pointService.applyAndLog(
                        booking.getMember(),
                        booking,
                        payment,
                        PointChangeType.CANCEL,   // 회수(마이너스)
                        -earned,
                        "예매 취소: 적립 회수"
                );
            }

            log.info("[CANCEL:POST-APPROVE] bookingId={}, refundAmount={}, usedPointsBack={}, earnedRevoke={}",
                    bookingId, payment.getAmount(), usedPoints, earned);
            return;
        }

        // === 그 외(이미 CANCELLED 등) ===
        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            // 멱등 처리
            booking.setPaymentStatus(BookingPayStatus.CANCELLED);
            bookingRepository.save(booking);
            // 좌석 정리도 안전하게 한번 더
            seatHoldRepository.deleteByBookingId(bookingId);
            bookingSeatRepository.deleteByBooking_BookingId(bookingId);
            log.info("[CANCEL:IDEMPOTENT] bookingId={} already cancelled", bookingId);
            return;
        }

        // 다른 상태가 있다면 필요 시 분기 추가
        throw new IllegalStateException("취소할 수 없는 결제 상태: " + payment.getStatus());
    }

    /** 토스 승인 성공 → 내부 확정 처리(간단 래퍼) */
    @Transactional
    public void confirmAndFinalize(ConfirmPaymentRequestDTO req) {
        tossPaymentService.confirm(
                new TossConfirmRequestDTO(req.paymentKey(), req.orderId(), req.amount())
        );

        Payment payment = paymentRepository.findByOrderId(req.orderId())
                .orElseThrow(() -> new IllegalArgumentException("결제 정보 없음"));
        Booking booking = payment.getBooking();

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        booking.setPaymentStatus(BookingPayStatus.PAID);
        bookingRepository.save(booking);

        seatHoldRepository.deleteByBookingId(booking.getBookingId());

        log.info("[TOSS] finalized bookingId={}, paymentId={}, status=PAID",
                booking.getBookingId(), payment.getPaymentId());
    }

    /** 주문 선생성 + 기존 PENDING 결제행에 orderId 부착 */
    @Transactional
    public void createOrderAndAttach(Long bookingId, String orderId, BigDecimal amount) {
        int updated = paymentRepository.attachOrderIdToPendingByBookingId(bookingId, orderId);

        if (updated == 0) {
            Payment p = new Payment();
            p.setBooking(bookingRepository.getReferenceById(bookingId));
            p.setAmount(amount);
            p.setProvider(PaymentProvider.TOSS);
            p.setStatus(PaymentStatus.PENDING);
            p.setOrderId(orderId);
            paymentRepository.save(p);
        } else {
            paymentRepository.updateAmountByOrderId(orderId, amount);
        }

        log.info("[ORDER] bookingId={}, orderId={}, amount={}", bookingId, orderId, amount);
    }

    /** 결제 최종 확정(좌석확정 + 포인트 사용/적립 + QR 생성) */
    @Transactional
    public void confirm(String paymentKey, String orderId, BigDecimal approvedAmount) {
        Payment payment = paymentRepository.findByOrderIdForUpdate(orderId)
                .orElseThrow(() -> new IllegalStateException("payment not found by orderId"));

        if (payment.getAmount().compareTo(approvedAmount) != 0) {
            log.warn("[AMOUNT-MISMATCH] orderId={}, saved={}, approved={}",
                    orderId, payment.getAmount(), approvedAmount);
            throw new IllegalArgumentException("amount mismatch");
        }

        Booking booking = payment.getBooking();
        Screening screening = booking.getScreening();
        Long screeningId = screening.getScreeningId();

        LocalDateTime now = LocalDateTime.now();

        // holdKey 보정: paymentKey 비어있으면 orderId 사용
        String holdKey = payment.getPaymentKey() != null && !payment.getPaymentKey().isBlank()
                ? payment.getPaymentKey()
                : orderId;

        var holds = seatHoldRepository.findActiveByHoldKeyForUpdate(holdKey, screeningId, now);
        if (holds.isEmpty()) {
            throw new IllegalStateException("no active holds for this paymentKey/order");
        }

        for (SeatHold h : holds) {
            BookingSeat bs = BookingSeat.builder()
                    .booking(booking)
                    .screening(screening)
                    .seat(h.getSeat())
                    .build();
            bookingSeatRepository.save(bs);
        }

        seatHoldRepository.deleteAll(holds);

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaymentKey(paymentKey);
        payment.setPaidAt(now);
        booking.setPaymentStatus(BookingPayStatus.PAID);

        // ==== 포인트 사용/적립 ====
        // 총액(Booking.totalPrice) - 실결제(Payment.amount) = 사용포인트
        int usedPoints = booking.getTotalPrice()
                .subtract(payment.getAmount())
                .max(BigDecimal.ZERO)
                .intValue();

        // 포인트 사용 기록 (부호: -)
        if (usedPoints > 0) {
            pointService.applyAndLog(
                    booking.getMember(),
                    booking,
                    payment,
                    PointChangeType.USE,
                    -usedPoints,
                    "예매 포인트 사용"
            );
        }

        // 포인트 적립 (실결제금액의 5% -> 내림/FLOOR)
        int earn = payment.getAmount()
                .multiply(BigDecimal.valueOf(0.05))
                .setScale(0, RoundingMode.FLOOR)
                .intValue();

        if (earn > 0) {
            pointService.applyAndLog(
                    booking.getMember(),
                    booking,
                    payment,
                    PointChangeType.EARN,
                    +earn,
                    "결제 적립 5%"
            );
        }

        // DB 컬럼 추가 없이 응답/로깅용으로만 쓰고 싶다면 Payment에 @Transient Integer pointsUsed 추가
        try { payment.setPointsUsed(usedPoints); } catch (Exception ignore) {}

        Member member = booking.getMember();
        if (member != null) {
            int cur = member.getPointBalance() == null ? 0 : member.getPointBalance();
            if (usedPoints > 0) {
                cur = Math.max(0, cur - usedPoints);        // 사용 차감
            }
            earn = payment.getAmount()
                    .multiply(BigDecimal.valueOf(0.05))
                    .setScale(0, RoundingMode.FLOOR)
                    .intValue();
            member.setPointBalance(cur + earn);             // 5% 적립
        }

        // ==== QR 생성 (data URL) ====
        String payload = buildQrPayload(booking);
        String dataUrl = generateQrPngDataUrl(payload, 240, 240);
        booking.setQrCodeUrl(dataUrl);

        log.info("[CONFIRM] bookingId={}, usedPoints={}, earned(5%)={}, cardAmount={}",
                booking.getBookingId(), usedPoints,
                payment.getAmount().multiply(BigDecimal.valueOf(0.05)).setScale(0, RoundingMode.FLOOR).intValue(),
                payment.getAmount());
    }

    /** QR payload (JWT 사용 시 이 부분에서 생성) */
    private String buildQrPayload(Booking booking) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("bookingId", booking.getBookingId());
            claims.put("screeningId", booking.getScreening().getScreeningId());
            claims.put("issuedAt", System.currentTimeMillis());
            // JWT 토큰 생성 로직이 있다면 여기에
            return "BOOKING:" + booking.getBookingId();
        } catch (Exception e) {
            return "BOOKING:" + booking.getBookingId();
        }
    }

    /** ZXing → PNG → data:image/png;base64,... */
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
}