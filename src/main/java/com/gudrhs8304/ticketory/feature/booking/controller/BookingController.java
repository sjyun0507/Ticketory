package com.gudrhs8304.ticketory.feature.booking.controller;

import com.gudrhs8304.ticketory.feature.booking.domain.Booking;
import com.gudrhs8304.ticketory.feature.booking.dto.BookingSummaryDTO;
import com.gudrhs8304.ticketory.feature.booking.dto.InitBookingRequestDTO;
import com.gudrhs8304.ticketory.feature.booking.dto.InitBookingResponseDTO;
import com.gudrhs8304.ticketory.core.auth.CustomUserPrincipal;
import com.gudrhs8304.ticketory.feature.booking.service.BookingOrchestrator;
import com.gudrhs8304.ticketory.feature.booking.service.BookingQueryService;
import com.gudrhs8304.ticketory.feature.booking.service.BookingService;
import com.gudrhs8304.ticketory.feature.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookingController {

    private final BookingQueryService bookingQueryService;
    private final BookingService bookingService;
    private final BookingOrchestrator bookingOrchestrator;
    private final PaymentService paymentService;


    @Operation(summary = "예매내역")
    @GetMapping({"/{memberId}/bookings", "/{memberId}/booking"}) // 경로 2개를 '한 메서드'에만
    private ResponseEntity<?> getBookings(
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserPrincipal principal, // ← 이것만 사용
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "bookingTime,desc") String sort,
            @RequestParam(required = false) String status
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인이 필요합니다."));
        }
        Long authMemberId = principal.getMemberId();
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (!isAdmin && !memberId.equals(authMemberId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "본인만 조회할 수 있습니다."));
        }

        Sort sortObj = parseSort(sort);
        Page<BookingSummaryDTO> result =
                bookingQueryService.getMemberBookings(memberId, authMemberId, isAdmin, page, size, sortObj, status);
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
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long bookingId
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"message\":\"로그인이 필요합니다.\"}");
        }
        Long memberId = principal.getMemberId();
        Booking booking = bookingService.getMyBooking(memberId, bookingId);
        return ResponseEntity.ok(booking);
    }

    @Operation(summary = "예매 취소")
    @DeleteMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelBooking(
            @PathVariable Long bookingId,
            @RequestParam(name = "reason", required = false) String reason,
            @RequestParam(value = "memo", required = false) String memo,
            Authentication auth
    ) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "로그인이 필요합니다."));
        }
        Long me = extractMemberId(auth);

        String label = toReasonLabel(reason);
        String finalReason = buildFinalReason(label, memo);

        paymentService.cancel(me, bookingId, finalReason); // 본인 예매 검증은 서비스에서 그대로 유지
        return ResponseEntity.ok(Map.of("bookingId", bookingId, "status", "CANCELLED"));
    }

    /** 드롭다운 코드 → 한글 라벨 매핑 */
    private String toReasonLabel(String codeOrText) {
        if (codeOrText == null || codeOrText.isBlank()) return null;
        String c = codeOrText.trim().toUpperCase();

        return switch (c) {
            case "CHANGE_OF_PLANS" -> "일정 변경";
            case "MISTAKE"         -> "잘못된 예매";
            case "PRICE"           -> "가격/좌석 재선택";
            case "WEATHER"         -> "날씨/이동 문제";
            case "HEALTH"          -> "건강 문제";
            case "OTHER"           -> "기타";
            default                -> codeOrText; // 이미 라벨 문자열이면 그대로 저장
        };
    }

    private String buildFinalReason(String label, String memo) {
        // 최대 255자 안전 처리
        String base = (label == null || label.isBlank()) ? "" : label;
        String extra = (memo == null || memo.isBlank()) ? "" : (" - " + memo.trim());
        String combined = (base + extra).trim();
        return combined.length() > 255 ? combined.substring(0, 255) : combined;
    }

    /** Authentication에서 memberId를 최대한 안전하게 추출 */
    private Long extractMemberId(Authentication auth) {
        if (auth == null) return null;

        // 1) 커스텀 Principal을 쓰는 경우
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserPrincipal p) {
            return p.getMemberId();
        }

        // 2) 스프링 기본 UserDetails를 쓰고, username에 memberId를 넣은 경우
        if (principal instanceof org.springframework.security.core.userdetails.User u) {
            try { return Long.valueOf(u.getUsername()); } catch (Exception ignore) {}
        }

        // 3) auth.getName()이 숫자면 그대로 사용
        try { return Long.valueOf(auth.getName()); } catch (Exception ignore) {}

        // 4) JWT의 Claim을 꺼낼 수 있다면 여기서 파싱(예: sub, id, memberId 등)
        //    JwtAuthenticationToken 등을 쓰는 환경이면 형식에 맞게 보강
        if (auth.getDetails() instanceof java.util.Map<?, ?> details) {
            Object id = ((Map<?, ?>) details).get("memberId");
            if (id != null) {
                try { return Long.valueOf(String.valueOf(id)); } catch (Exception ignore) {}
            }
        }

        return null;
    }



    @DeleteMapping("/holds/{holdKey}")
    public ResponseEntity<Void> cancelHold(@PathVariable String holdKey) {
        bookingService.releaseHoldByKey(holdKey);
        return ResponseEntity.noContent().build();
    }


}