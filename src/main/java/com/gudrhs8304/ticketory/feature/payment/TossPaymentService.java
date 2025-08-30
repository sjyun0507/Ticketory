package com.gudrhs8304.ticketory.feature.payment;

import com.gudrhs8304.ticketory.feature.booking.domain.Booking;
import com.gudrhs8304.ticketory.feature.booking.BookingPayStatus;
import com.gudrhs8304.ticketory.feature.payment.dto.TossCancelRequest;
import com.gudrhs8304.ticketory.feature.payment.dto.TossConfirmRequestDTO;
import com.gudrhs8304.ticketory.feature.payment.dto.TossInitiateRequestDTO;
import com.gudrhs8304.ticketory.feature.payment.dto.TossInitiateResponseDTO;
import com.gudrhs8304.ticketory.feature.booking.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class TossPaymentService {

    private final WebClient tossClient;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    @Value("${toss.client-key}")
    private String clientKey;
    @Value("${toss.success-url}")
    private String successUrl;
    @Value("${toss.fail-url}")
    private String failUrl;

    /** 1) 결제 시작(프론트에서 tosspayments.js 호출에 필요한 파라미터 제공) */
    @Transactional
    public TossInitiateResponseDTO initiate(TossInitiateRequestDTO req, Long memberId) {
        Booking booking = bookingRepository.findById(req.bookingId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예매입니다."));

        // 금액 서버 검증 (프론트가 보낸 amount와 서버 계산 금액 일치 체크)
        BigDecimal serverAmount = booking.getTotalPrice();
        if (serverAmount == null || req.amount() == null || serverAmount.compareTo(req.amount()) != 0) {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }

        // 고유 orderId 생성 (bookingId + UUID 조합 추천)
        String orderId = "B" + booking.getBookingId() + "-" + UUID.randomUUID();

        // PENDING 결제 레코드 생성
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setProvider(PaymentProvider.TOSS);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setOrderId(orderId);
        payment.setAmount(serverAmount);
        paymentRepository.save(payment);

        String orderName = "영화 예매 #" + booking.getBookingId();

        return new TossInitiateResponseDTO(
                orderId,
                serverAmount,
                orderName,
                booking.getMember().getEmail(),
                successUrl,
                failUrl,
                clientKey
        );
    }

    /** 2) 결제 승인 */
    @Transactional
    public Payment confirm(TossConfirmRequestDTO req) {
        // orderId로 결제 레코드 조회
        Payment payment = paymentRepository.findByOrderId(req.orderId())
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        if (payment.getStatus() == PaymentStatus.PAID) {
            return payment; // 멱등 처리
        }

        // 금액 검증
        if (payment.getAmount() == null || req.amount() == null || payment.getAmount().compareTo(req.amount()) != 0) {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }

        // Toss 승인 API 호출
        Map<String, Object> body = Map.of(
                "paymentKey", req.paymentKey(),
                "orderId", req.orderId(),
                "amount", req.amount()
        );

        try {
            Map<String, Object> res = tossClient.post()
                    .uri("/v1/payments/confirm")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // 승인 성공 → 상태 업데이트
            payment.setPaymentKey(req.paymentKey());
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);

            var booking = payment.getBooking();
            booking.setPaymentStatus(BookingPayStatus.PAID);
            bookingRepository.save(booking);

            // TODO: 좌석 확정, 포인트 적립 등 후처리 연결
            return payment;

        } catch (WebClientResponseException e) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw e;
        }
    }

    public Optional<Payment> findById(Long id) {
        return paymentRepository.findById(id);
    }

    /** 3) 결제 취소 */
    @Transactional
    public Payment cancel(TossCancelRequest req) {
        Payment payment = paymentRepository.findByPaymentKey(req.paymentKey())
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        if (payment.getStatus() == PaymentStatus.CANCELLED
                || payment.getStatus() == PaymentStatus.REFUNDED) {
            return payment; // 멱등
        }

        Map<String, Object> body = Map.of("cancelReason", req.cancelReason());

        try {
            Map<String, Object> res = tossClient.post()
                    .uri("/v1/payments/{paymentKey}/cancel", req.paymentKey())
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            payment.setStatus(PaymentStatus.CANCELLED);
            paymentRepository.save(payment);
            // TODO: 좌석/예매/재고 롤백 등 후처리
            return payment;

        } catch (WebClientResponseException e) {
            throw e;
        }
    }
}
