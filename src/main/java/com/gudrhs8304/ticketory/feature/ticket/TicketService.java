package com.gudrhs8304.ticketory.feature.ticket;

import com.gudrhs8304.ticketory.core.jwt.JwtTokenProvider;
import com.gudrhs8304.ticketory.feature.booking.domain.Booking;
import com.gudrhs8304.ticketory.feature.booking.BookingRepository;
import com.gudrhs8304.ticketory.feature.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final BookingRepository bookingRepository;
    private final JwtTokenProvider jwt; // 이미 프로젝트에 존재
    private final FileStorage fileStorage;

    @Value("${app.media.qr-dir:qrs}")  // ← 여기가 누락돼서 발생한 오류
    private String qrDir;




    @Transactional(readOnly = true)
    public String getTicketQrDataUri(Long bookingId, Long memberId) {
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "예매 없음"));

        if (b.getMember() == null || !b.getMember().getMemberId().equals(memberId)) {
            // 403이 더 어울립니다
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인만 조회 가능");
        }

        String dataUri = b.getQrCodeUrl(); // DB에 "data:image/png;base64,..."가 저장돼 있다고 가정
        if (dataUri == null || dataUri.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "QR 없음");
        }

        // 혹시 예전에 URL이 저장돼 있던 데이터가 남아있다면 404로 처리(원치 않으면 여기서 변환 로직 추가)
        if (!dataUri.startsWith("data:image/")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "QR 없음");
        }

        return dataUri;
    }

    @Transactional(readOnly = true)
    public byte[] getTicketQrPng(Long bookingId, Long memberId, int sizeIgnored) {
        // size 파라미터는 과거 외부생성용이었으면 무시해도 OK (보관된 이미지를 그대로 반환)
        String dataUri = getTicketQrDataUri(bookingId, memberId);
        int comma = dataUri.indexOf(',');
        if (comma < 0) throw new IllegalStateException("잘못된 data URI");
        return java.util.Base64.getDecoder().decode(dataUri.substring(comma + 1));
    }
}