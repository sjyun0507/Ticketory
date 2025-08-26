package com.gudrhs8304.ticketory.controller;

import com.gudrhs8304.ticketory.domain.Booking;
import com.gudrhs8304.ticketory.domain.Payment;
import com.gudrhs8304.ticketory.domain.enums.BookingPayStatus;
import com.gudrhs8304.ticketory.domain.enums.PaymentProvider;
import com.gudrhs8304.ticketory.domain.enums.PaymentStatus;
import com.gudrhs8304.ticketory.dto.pay.ConfirmPaymentRequestDTO;
import com.gudrhs8304.ticketory.dto.payment.PaymentOrderCreateReqDTO;
import com.gudrhs8304.ticketory.dto.payment.TossConfirmRequestDTO;
import com.gudrhs8304.ticketory.dto.payment.TossInitiateRequestDTO;
import com.gudrhs8304.ticketory.dto.payment.TossInitiateResponseDTO;
import com.gudrhs8304.ticketory.repository.BookingRepository;
import com.gudrhs8304.ticketory.repository.PaymentRepository;
import com.gudrhs8304.ticketory.service.BookingService;
import com.gudrhs8304.ticketory.service.PaymentService;
import com.gudrhs8304.ticketory.service.TossPaymentService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/payments"})
@Log4j2
public class PaymentsController {
    private final BookingService bookingService;
    private final TossPaymentService tossPaymentService;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    /** 결제 시작 (주문/임시 결제 생성) */
    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody PaymentOrderCreateReqDTO req) {
        Long bookingId = req.getBookingId();

        if (bookingId == null && req.getMemberId() != null) {
            bookingId = bookingRepository
                    .findTopByMember_MemberIdAndPaymentStatusOrderByCreatedAtDesc(
                            req.getMemberId(), BookingPayStatus.PENDING
                    )
                    .map(Booking::getBookingId)
                    .orElse(null);
        }
        if (bookingId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "bookingId 가 없고, 최근 PENDING 예매도 찾지 못했습니다.");
        }

        // ✅ 서버 기준 금액 계산
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "booking not found"));
        long usedPoint = req.getUsedPoint(); // 원시 long: null 아님
        BigDecimal serverAmount = booking.getTotalPrice()
                .subtract(BigDecimal.valueOf(usedPoint))
                .max(BigDecimal.ZERO);

        // orderId 생성
        String orderId = (req.getOrderId() == null || req.getOrderId().isBlank())
                ? "ORD-" + LocalDate.now() + "-" + bookingId + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase()
                : req.getOrderId();

        // ✅ 결제행에 orderId 부착 + 금액 저장/갱신
        paymentService.createOrderAndAttach(bookingId, orderId, serverAmount);

        // 프론트는 수정하지 말라 했으니 orderId만 그대로 반환
        return ResponseEntity.ok(Map.of("orderId", orderId));
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
        paymentService.confirm(req.paymentKey(), req.orderId(), req.amount().longValue());
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

}