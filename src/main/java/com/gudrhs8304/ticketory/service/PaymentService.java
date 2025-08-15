package com.gudrhs8304.ticketory.service;

import com.gudrhs8304.ticketory.domain.*;
import com.gudrhs8304.ticketory.domain.enums.BookingPayStatus;
import com.gudrhs8304.ticketory.domain.enums.PaymentProvider;
import com.gudrhs8304.ticketory.domain.enums.PaymentStatus;
import com.gudrhs8304.ticketory.dto.payment.ApprovePaymentRequest;
import com.gudrhs8304.ticketory.repository.BookingRepository;
import com.gudrhs8304.ticketory.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

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
}