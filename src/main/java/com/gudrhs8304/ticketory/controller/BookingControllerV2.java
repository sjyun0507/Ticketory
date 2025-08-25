package com.gudrhs8304.ticketory.controller;

import com.gudrhs8304.ticketory.dto.booking.InitBookingRequestDTO;
import com.gudrhs8304.ticketory.dto.booking.InitBookingResponseDTO;
import com.gudrhs8304.ticketory.security.auth.CustomUserPrincipal;
import com.gudrhs8304.ticketory.security.oauth.CustomOAuth2UserService;
import com.gudrhs8304.ticketory.service.BookingOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class BookingControllerV2 {

    private final BookingOrchestrator bookingOrchestrator;

    @PostMapping("/bookings")
    public ResponseEntity<InitBookingResponseDTO> initBooking(
            @RequestHeader(value = "Idempotency-Key", required = false) String idemKey,
            @AuthenticationPrincipal(errorOnInvalidType = false) Object principal,
            @RequestBody InitBookingRequestDTO req
    ) {
        Long memberId = extractMemberId(principal);

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        InitBookingResponseDTO resp = bookingOrchestrator.initBooking(memberId, idemKey, req);
        return ResponseEntity.ok(resp);
    }

    /** principal에서 memberId를 안전하게 뽑아내는 헬퍼 */
    private Long extractMemberId(Object principal) {
        if (principal instanceof CustomUserPrincipal p) {
            return p.getMemberId();   // ← 여기서 Long만 뽑아서 return
        }
        return null; // 비로그인(anonymousUser) 등
    }


}