package com.gudrhs8304.ticketory.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.gudrhs8304.ticketory.domain.*;
import com.gudrhs8304.ticketory.domain.enums.BookingPayStatus;
import com.gudrhs8304.ticketory.domain.enums.PaymentProvider;
import com.gudrhs8304.ticketory.domain.enums.PaymentStatus;
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
        if (!booking.getMember().getMemberId().equals(memberId)) {
            throw new SecurityException("본인 예매만 취소할 수 있습니다.");
        }
        booking.setPaymentStatus(BookingPayStatus.CANCELLED);
        // (환불/좌석해제 등은 필요 시 추가)
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

        // DB 컬럼 추가 없이 응답/로깅용으로만 쓰고 싶다면 Payment에 @Transient Integer pointsUsed 추가
        try { payment.setPointsUsed(usedPoints); } catch (Exception ignore) {}

        Member member = booking.getMember();
        if (member != null) {
            int cur = member.getPointBalance() == null ? 0 : member.getPointBalance();
            if (usedPoints > 0) {
                cur = Math.max(0, cur - usedPoints);        // 사용 차감
            }
            int earn = payment.getAmount()
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