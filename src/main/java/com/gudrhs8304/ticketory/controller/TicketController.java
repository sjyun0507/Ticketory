package com.gudrhs8304.ticketory.controller;

import com.gudrhs8304.ticketory.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookings")
public class TicketController {

    private final TicketService ticketService;

    @Operation(summary = "QR 티켓 조회(PNG)", description = "본인 예매의 QR 이미지(PNG)를 반환합니다.")
    @GetMapping(value = "/{bookingId}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQr(
            @PathVariable Long bookingId,
            @RequestParam(defaultValue = "320") int size,
            @AuthenticationPrincipal(expression = "memberId") Long memberId // 앞서 만든 CustomUser 구현 기준
    ) {
        byte[] png = ticketService.getTicketQrPng(bookingId, memberId, size);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(png);
    }
}