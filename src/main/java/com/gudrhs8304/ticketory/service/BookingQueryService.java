package com.gudrhs8304.ticketory.service;

import com.gudrhs8304.ticketory.dto.BookingSummaryDTO;
import com.gudrhs8304.ticketory.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                                                     int page, int size, Sort sort) {

        if (!isAdmin && !Objects.equals(targetMemberId, authMemberId)) {
            throw new SecurityException("본인만 조회할 수 있습니다.");
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<BookingSummaryDTO> slice = bookingRepository.findSummaryPageByMemberId(targetMemberId, pageable);

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


}