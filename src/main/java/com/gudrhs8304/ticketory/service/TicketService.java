package com.gudrhs8304.ticketory.service;

import com.gudrhs8304.ticketory.config.JwtTokenProvider;
import com.gudrhs8304.ticketory.domain.Booking;
import com.gudrhs8304.ticketory.repository.BookingRepository;
import com.gudrhs8304.ticketory.storage.FileStorage;
import com.gudrhs8304.ticketory.util.QrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final BookingRepository bookingRepository;
    private final JwtTokenProvider jwt; // 이미 프로젝트에 존재
    private final FileStorage fileStorage;

    @Value("${app.media.qr-dir:qrs}")  // ← 여기가 누락돼서 발생한 오류
    private String qrDir;


    @Transactional // 저장이 일어날 수 있으니 readOnly 아님
    public byte[] getTicketQrPng(Long bookingId, Long authMemberId, int size) {
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("예매를 찾을 수 없습니다."));

        // 본인 소유 검증
        if (b.getMember() == null || !b.getMember().getMemberId().equals(authMemberId)) {
            throw new SecurityException("본인 예매만 조회할 수 있습니다.");
        }

        // 1) 이미 URL이 있으면 다운로드 시도
        if (b.getQrCodeUrl() != null && !b.getQrCodeUrl().isBlank()) {
            try {
                return fileStorage.downloadAsBytes(b.getQrCodeUrl());
            } catch (Exception ignore) {
                // 저장소 파일이 삭제/손상된 경우에만 아래에서 재생성
            }
        }

        // 2) 신규 생성 (또는 복구 생성)
        String token = jwt.createTicketToken(
                b.getBookingId(),
                b.getMember().getMemberId(),
                b.getScreening().getScreeningId()
        );
        String payload = "ticketory://ticket?token=" + token;

        int finalSize = Math.max(size, 240);
        byte[] png = QrUtil.toPng(payload, finalSize);

        // 3) 업로드 경로/파일명: qrs/YYYY/MM/booking-123.png
        LocalDate today = LocalDate.now();
        String relPath = String.format("%s/%04d/%02d", qrDir, today.getYear(), today.getMonthValue());
        String filename = String.format("booking-%d.png", b.getBookingId());

        String publicUrl = fileStorage.saveBytes(relPath, filename, png);

        // 4) DB 저장
        b.setQrCodeUrl(publicUrl);
        bookingRepository.save(b);

        // 5) 생성한 PNG 반환
        return png;
    }
}