package com.gudrhs8304.ticketory.controller;

import com.gudrhs8304.ticketory.domain.Booking;
import com.gudrhs8304.ticketory.dto.booking.*;
import com.gudrhs8304.ticketory.security.auth.CustomUserPrincipal;
import com.gudrhs8304.ticketory.service.BookingOrchestrator;
import com.gudrhs8304.ticketory.service.BookingQueryService;
import com.gudrhs8304.ticketory.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookingController {

    private final BookingQueryService bookingQueryService;
    private final BookingService bookingService;
    private final BookingOrchestrator bookingOrchestrator;

    @Operation(summary = "예매내역")
    @GetMapping("/{memberId}/booking")
    public ResponseEntity<Page<BookingSummaryDTO>> getBookings(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "bookingTime,desc") String sort,
            Authentication authentication
    ) {
        Long authMemberId = null;
        boolean isAdmin = false;

        if (authentication != null && authentication.isAuthenticated()) {
            try {
                // JwtAuthFilter에서 Authentication#setName(memberId)로 넣었다는 전제
                authMemberId = Long.valueOf(authentication.getName());
            } catch (NumberFormatException ignore) {}
            isAdmin = authentication.getAuthorities()
                    .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        Sort sortObj = parseSort(sort);
        Page<BookingSummaryDTO> result =
                bookingQueryService.getMemberBookings(memberId, authMemberId, isAdmin, page, size, sortObj);

        return ResponseEntity.ok(result);
    }

    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by(Sort.Order.desc("bookingTime"));
        }
        if (!sortParam.contains("&") && !sortParam.contains(";")) {
            String[] parts = sortParam.split(",");
            String prop = parts[0].trim();
            Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1]))
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
            return Sort.by(new Sort.Order(dir, prop));
        }
        Sort sort = Sort.unsorted();
        for (String token : sortParam.split("[;&]")) {
            String[] parts = token.split(",");
            if (parts.length == 0) continue;
            String prop = parts[0].trim();
            Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1]))
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = sort.and(Sort.by(new Sort.Order(dir, prop)));
        }
        return sort.isUnsorted() ? Sort.by(Sort.Order.desc("bookingTime")) : sort;
    }

    @Operation(summary = "예매 생성(결제 전)")
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

    @Operation(summary = "예매 상세(본인만)")
    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<?> getBooking(
            Authentication authentication,
            @PathVariable Long bookingId
    ) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"message\":\"로그인이 필요합니다.\"}");
        }
        Long memberId = Long.valueOf(authentication.getName());
        Booking booking = bookingService.getMyBooking(memberId, bookingId);
        return ResponseEntity.ok(booking);
    }

    @Operation(summary = "예매 취소")
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