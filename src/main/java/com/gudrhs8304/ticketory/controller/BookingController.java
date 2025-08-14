package com.gudrhs8304.ticketory.controller;

import com.gudrhs8304.ticketory.dto.BookingSummaryDTO;
import com.gudrhs8304.ticketory.service.BookingQueryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class BookingController {
    private final BookingQueryService bookingQueryService;

    @Operation(summary = "예매내역")
    @GetMapping("/{memberId}/booking")
    public ResponseEntity<Page<BookingSummaryDTO>> getBookings(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            // 예: ?sort=bookingTime,desc 혹은 여러개 sort 파라미터 가능
            @RequestParam(defaultValue = "bookingTime,desc") String sort,
            Authentication authentication
    ) {
        // 인증 주체 파싱
        Long authMemberId = null;
        boolean isAdmin = false;

        if (authentication != null && authentication.isAuthenticated()) {
            try {
                authMemberId = Long.valueOf(authentication.getName()); // JwtAuthFilter에서 subject=memberId 로 셋팅
            } catch (NumberFormatException ignore) {}
            isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        // 정렬 파라미터 파싱 (예: "bookingTime,desc")
        Sort sortObj = parseSort(sort);

        Page<BookingSummaryDTO> result =
                bookingQueryService.getMemberBookings(memberId, authMemberId, isAdmin, page, size, sortObj);

        return ResponseEntity.ok(result);
    }

    private Sort parseSort(String sortParam) {
        // 다중 정렬 지원: ?sort=bookingTime,desc&sort=createdAt,desc ...
        // 단일 쿼리스트링 "bookingTime,desc"도 지원
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by(Sort.Order.desc("bookingTime")); // TODO: Booking 엔티티의 필드명 확인
        }

        // 쉼표로만 넘어올 때
        if (!sortParam.contains("&") && !sortParam.contains(";")) {
            String[] parts = sortParam.split(",");
            String prop = parts[0].trim();
            Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1])) ? Sort.Direction.ASC : Sort.Direction.DESC;
            return Sort.by(new Sort.Order(dir, prop));
        }

        // 여러 개일 때(세미콜론 등으로 묶었다고 가정)
        Sort sort = Sort.unsorted();
        for (String token : sortParam.split("[;&]")) {
            String[] parts = token.split(",");
            if (parts.length == 0) continue;
            String prop = parts[0].trim();
            Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1])) ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = sort.and(Sort.by(new Sort.Order(dir, prop)));
        }
        return sort.isUnsorted() ? Sort.by(Sort.Order.desc("bookingTime")) : sort;
    }
}
