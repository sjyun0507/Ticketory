package com.gudrhs8304.ticketory.service;

import com.gudrhs8304.ticketory.config.JwtTokenProvider;
import com.gudrhs8304.ticketory.domain.Booking;
import com.gudrhs8304.ticketory.repository.BookingRepository;
import com.gudrhs8304.ticketory.util.QrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final BookingRepository bookingRepository;
    private final JwtTokenProvider jwt; // 이미 프로젝트에 존재

    @Transactional(readOnly = true)
    public byte[] getTicketQrPng(Long bookingId, Long authMemberId, int size) {
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("예매를 찾을 수 없습니다."));

        // 본인 소유 검증
        if (b.getMember() == null || !b.getMember().getMemberId().equals(authMemberId)) {
            throw new SecurityException("본인 예매만 조회할 수 있습니다.");
        }

        // 결제완료만 QR 노출 (필요 시)
        // if (b.getPaymentStatus() != PaymentStatus.PAID) { ... }

        // 티켓 전용 JWT (검표 서버에서만 검증)
        String ticketToken = jwt.createTicketToken(
                b.getBookingId(),
                b.getMember().getMemberId(),
                b.getScreening().getScreeningId()
        );
        // QR 콘텐츠: 스킴 붙여 의도 명확히
        String payload = "ticketory://ticket?token=" + ticketToken;

        return QrUtil.toPng(payload, Math.max(size, 240)); // 최소 240px
    }
}