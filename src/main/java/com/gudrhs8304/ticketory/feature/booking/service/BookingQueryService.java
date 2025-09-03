package com.gudrhs8304.ticketory.feature.booking.service;

import com.gudrhs8304.ticketory.feature.booking.repository.BookingRepository;
import com.gudrhs8304.ticketory.feature.booking.dto.BookingSummaryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class BookingQueryService {

    private final BookingRepository bookingRepository;

    /**
     * 본인(memberId) 또는 ADMIN만 접근 가능하게 서비스에서 한 번 더 가드.
     */
    @Transactional(readOnly = true)
    public Page<BookingSummaryDTO> getMemberBookings(Long targetMemberId,
                                                     Long authMemberId,
                                                     boolean isAdmin,
                                                     int page, int size, Sort sort,
                                                     String status) {

        if (!isAdmin && !Objects.equals(targetMemberId, authMemberId)) {
            throw new SecurityException("본인만 조회할 수 있습니다.");
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<BookingSummaryDTO> base = bookingRepository.findSummaryPageByMemberIdAll(targetMemberId, pageable);

        // status 파라미터가 주어지면 클라이언트가 원하는 상태만 필터링
        Page<BookingSummaryDTO> slice;
        if (status != null && !status.isBlank()) {
            final String want = status.trim().toUpperCase();
            List<BookingSummaryDTO> filtered = base.getContent().stream()
                    .filter(dto -> {
                        String ps = String.valueOf(dto.getPaymentStatus());
                        String up = ps == null ? "" : ps.toUpperCase();
                        if ("PAID".equals(want)) return "PAID".equals(up);
                        if ("CANCELLED".equals(want) || "CANCELED".equals(want)) return "CANCELLED".equals(up) || "CANCELED".equals(up);
                        if (want.startsWith("CANCEL")) return up.startsWith("CANCEL");
                        return true; // 알 수 없는 값이면 전체 반환과 동일
                    })
                    .toList();
            slice = new org.springframework.data.domain.PageImpl<>(filtered, base.getPageable(), filtered.size());
        } else {
            slice = base;
        }

        if (slice.isEmpty()) return slice;

        // 좌석 라벨 병합
        List<Long> bookingIds = slice.getContent().stream()
                .map(BookingSummaryDTO::getBookingId)
                .toList();

        List<Object[]> seatRows = bookingRepository.findSeatLabelsByBookingIds(bookingIds);
        Map<Long, List<String>> seatMap = seatRows.stream()
                .collect(Collectors.groupingBy(
                        row -> (Long) row[0],
                        Collectors.mapping(row -> (String) row[1], Collectors.toList())
                ));

        slice.forEach(dto -> dto.setSeats(seatMap.getOrDefault(dto.getBookingId(), List.of())));
        return slice;
    }

    public Page<BookingSummaryDTO> findEligible(Long memberId, Pageable pageable) {
        var now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        return bookingRepository.findEligibleForStory(memberId, now, pageable);
    }


}