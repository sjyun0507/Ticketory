package com.gudrhs8304.ticketory.feature.payment;

import com.gudrhs8304.ticketory.feature.booking.domain.Booking;
import com.gudrhs8304.ticketory.feature.booking.BookingPayStatus;
import com.gudrhs8304.ticketory.feature.payment.dto.ConfirmPaymentRequestDTO;
import com.gudrhs8304.ticketory.feature.payment.dto.PaymentOrderCreateReqDTO;
import com.gudrhs8304.ticketory.feature.booking.BookingRepository;
import com.gudrhs8304.ticketory.feature.booking.BookingService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.web.servlet.view.RedirectView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/payments"})
@Log4j2
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class PaymentsController {
    private final BookingService bookingService;
    private final TossPaymentService tossPaymentService;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody PaymentOrderCreateReqDTO req) {
        Long bookingId = req.getBookingId();

        // bookingId 없으면 멤버의 최신 PENDING 예매/결제에서 보정
        if (bookingId == null && req.getMemberId() != null) {
            bookingId = bookingRepository
                    .findTopByMember_MemberIdAndPaymentStatusOrderByCreatedAtDesc(
                            req.getMemberId(), BookingPayStatus.PENDING
                    )
                    .map(Booking::getBookingId)
                    .orElse(null);

            if (bookingId == null) {
                var pend = paymentRepository
                        .findTopByBooking_Member_MemberIdAndStatusOrderByPaymentIdDesc(
                                req.getMemberId(), PaymentStatus.PENDING
                        );
                if (pend.isPresent()) {
                    bookingId = pend.get().getBooking().getBookingId();
                }
            }
        }

        if (bookingId == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "NO_PENDING_BOOKING: 결제 생성 전 예매 초기화가 필요합니다. /api/bookings/init 호출 후 bookingId를 넘겨주세요."
            );
        }

//        Booking booking = bookingRepository.findById(bookingId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "booking not found"));

        Booking booking = bookingRepository.findWithMemberByBookingId(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "booking not found"));


        // 포인트 클램프(보유/총액 한도 내)
        int have = Optional.ofNullable(booking.getMember().getPointBalance()).orElse(0);
        int want = Optional.ofNullable(req.getUsedPoint()).map(BigDecimal::intValue).orElse(0);
        if (want < 0) want = 0;
        if (want > have) want = have;
        if (BigDecimal.valueOf(want).compareTo(booking.getTotalPrice()) > 0) {
            want = booking.getTotalPrice().intValue();
        }

        BigDecimal payable = booking.getTotalPrice().subtract(BigDecimal.valueOf(want));
        if (payable.compareTo(BigDecimal.ZERO) < 0) payable = BigDecimal.ZERO;

        String orderId = (req.getOrderId() == null || req.getOrderId().isBlank())
                ? "ORD-" + java.time.LocalDate.now() + "-" + bookingId + "-" +
                java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase()
                : req.getOrderId();

        // 서버 금액으로만 pending 결제 생성/갱신
        paymentService.createOrderAndAttach(bookingId, orderId, payable);

        // 프론트는 서버 확정 금액만 사용 (lineItems 불필요)
        return ResponseEntity.ok(java.util.Map.of(
                "orderId",        orderId,
                "bookingId",      bookingId,
                "totalAmount",    booking.getTotalPrice(),
                "pointsUsed",     want,
                "payableAmount",  payable
        ));
    }

//    /** 토스 결제 승인(프론트 successUrl에서 호출) */
//    @PostMapping("/payments/confirm")
//    public ResponseEntity<Payment> confirm(@RequestBody TossConfirmRequestDTO req) {
//        return ResponseEntity.ok(tossPaymentService.confirm(req));
//    }

    /** 결제 단건 조회 */
    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> get(@PathVariable Long paymentId) {
        return ResponseEntity.of(tossPaymentService.findById(paymentId));
    }

    /** 토스 결제 성공 이후 프론트에서 호출 */
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirm(@RequestBody ConfirmPaymentRequestDTO req) {
        log.info("결제 확인 요청: orderId={}, paymentKey={}, amount={}", req.orderId(), req.paymentKey(), req.amount());
        paymentService.confirm(req.paymentKey(), req.orderId(), req.amount());
        return ResponseEntity.noContent().build();
    }

//    /** 토스 결제 승인(프론트 successUrl에서 호출) */
//    @PostMapping("/payments/confirm")
//    public ResponseEntity<Payment> confirm(@RequestBody TossConfirmRequestDTO req) {
//        return ResponseEntity.ok(tossPaymentService.confirm(req));
//    }

    @Transactional
    public void createOrUpdatePending(Long bookingId, String orderId, BigDecimal amount) {

        // 기존 PENDING + paidAt is null 에 orderId 붙이기 시도
        int updated = paymentRepository.attachOrderIdToPendingByBookingId(bookingId, orderId);

        if (updated == 0) {
            // 없으면 새로 생성
            Payment p = new Payment();
            p.setBooking(bookingRepository.getReferenceById(bookingId));
            p.setAmount(amount);                     // 서버가 확정한 금액 저장
            p.setProvider(PaymentProvider.TOSS);
            p.setStatus(PaymentStatus.PENDING);
            p.setOrderId(orderId);
            paymentRepository.save(p);
        } else {
            // 이미 있는 PENDING이라면 금액 동기화
            paymentRepository.updateAmountByOrderId(orderId, amount);
        }
    }

    /** 토스 성공 redirect(백엔드가 confirm 실행 후 프론트로 리다이렉트) */
    @GetMapping("/redirect/success")
    public RedirectView successRedirect(
            @RequestParam("paymentKey") String paymentKey,
            @RequestParam("orderId") String orderId,
            @RequestParam("amount") String amountStr
    ) {
        BigDecimal amount = new BigDecimal(amountStr);
        paymentService.confirm(paymentKey, orderId, amount); // 좌석확정/포인트/QR

        // 프론트 성공 페이지로 넘겨주고 싶으면 쿼리 붙여서 리다이렉트
        RedirectView rv = new RedirectView();
        rv.setUrl("http://localhost:5173/success?orderId=" + orderId);
        rv.setExposeModelAttributes(false);
        return rv;
    }

    /** 토스 실패 redirect(백엔드가 그냥 프론트로 릴레이) */
    @GetMapping("/redirect/fail")
    public RedirectView failRedirect(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "orderId", required = false) String orderId
    ) {
        RedirectView rv = new RedirectView();
        // 원하는 실패 페이지 경로로 연결
        rv.setUrl("http://localhost:5173/fail" +
                (orderId != null ? ("?orderId=" + orderId) : ""));
        rv.setExposeModelAttributes(false);
        return rv;
    }

}