package com.gudrhs8304.ticketory.controller;

import com.gudrhs8304.ticketory.dto.booking.InitBookingRequestDTO;
import com.gudrhs8304.ticketory.dto.booking.InitBookingResponseDTO;
import com.gudrhs8304.ticketory.security.auth.CustomUserPrincipal;
import com.gudrhs8304.ticketory.security.oauth.CustomOAuth2UserService;
import com.gudrhs8304.ticketory.service.BookingOrchestrator;
import com.gudrhs8304.ticketory.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class BookingControllerV2 {

    private final BookingOrchestrator bookingOrchestrator;
    private final BookingService bookingService;

    @PostMapping("/bookings")
    public ResponseEntity<InitBookingResponseDTO> initBooking(
            @RequestHeader(value = "Idempotency-Key", required = false) String idemKey,
            @AuthenticationPrincipal(errorOnInvalidType = false) Object principal,
            @RequestBody InitBookingRequestDTO req
    ) {
        Long memberId = (principal instanceof CustomUserPrincipal p) ? p.getMemberId() : null;

        if (memberId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(bookingOrchestrator.initBooking(memberId, idemKey, req));
    }

    /** principal에서 memberId를 안전하게 뽑아내는 헬퍼 */
    private Long extractMemberId(Object principal) {
        if (principal instanceof CustomUserPrincipal p) {
            return p.getMemberId();   // ← 여기서 Long만 뽑아서 return
        }
        return null; // 비로그인(anonymousUser) 등
    }

    @DeleteMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<Void> cancel(Authentication authentication,
                                       @PathVariable Long bookingId) {
        Long memberId = null;
        if (authentication != null && authentication.isAuthenticated()) {
            try { memberId = Long.valueOf(authentication.getName()); } catch (NumberFormatException ignore) {}
        }
        bookingService.releaseHold(memberId, bookingId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/holds/{holdKey}")
    public ResponseEntity<Void> cancelHold(@PathVariable String holdKey) {
        bookingService.releaseHoldByKey(holdKey);
        return ResponseEntity.noContent().build();
    }




}