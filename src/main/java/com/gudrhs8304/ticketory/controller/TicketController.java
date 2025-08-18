package com.gudrhs8304.ticketory.controller;

import com.gudrhs8304.ticketory.security.auth.CustomUserPrincipal;
import com.gudrhs8304.ticketory.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookings")
public class TicketController {

    private final TicketService ticketService;


    @Operation(
            summary = "QR 티켓 조회(PNG)",
            description = "본인 예매의 QR 이미지(PNG)를 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "PNG 이미지",
                            content = @Content(mediaType = "image/png")),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "403", description = "본인 예매 아님"),
                    @ApiResponse(responseCode = "404", description = "예매 없음")
            }
    )
    // ❗ produces 고정 제거 (401/403 JSON 등 협상 충돌 방지)
    @GetMapping("/{bookingId}/qr")
    public ResponseEntity<?> getQr(
            @PathVariable Long bookingId,
            @RequestParam(defaultValue = "320") int size,
            @AuthenticationPrincipal Object principal // 어떤 타입이 와도 안전하게 처리
    ) {
        Long memberId = extractMemberId(principal);
        if (memberId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"message\":\"로그인이 필요합니다.\"}");
        }

        int clamped = Math.max(240, Math.min(size, 1024));
        byte[] png = ticketService.getTicketQrPng(bookingId, memberId, clamped);

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.noStore().getHeaderValue());
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.EXPIRES, "0");
        headers.setContentDisposition(
                ContentDisposition.inline().filename("ticket-" + bookingId + ".png").build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.IMAGE_PNG)
                .body(png);
    }

    /** principal 이 CustomUserPrincipal/Long/String 등 어떤 형태여도 memberId를 최대한 추출 */
    private Long extractMemberId(Object principal) {
        if (principal == null) return null;
        if (principal instanceof CustomUserPrincipal p) return p.getMemberId();
        if (principal instanceof Long l) return l;
        if (principal instanceof Integer i) return i.longValue();
        if (principal instanceof String s) {
            // JwtAuthFilter가 이름(subject)으로 memberId 문자열을 넣었다면 파싱 시도
            try { return Long.parseLong(s); } catch (NumberFormatException ignore) { return null; }
        }
        return null;
    }
}