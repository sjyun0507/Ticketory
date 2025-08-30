package com.gudrhs8304.ticketory.feature.booking.dto;


import java.util.List;
import java.util.Map;

public record InitBookingRequestDTO(
        Long screeningId,
        List<Long> seatIds,            // seat_id 배열
        Map<String, Integer> counts,   // { "adult": 2, "teen": 0 }
        Integer holdSeconds,           // 없으면 기본 120
        String provider,                // "TOSS"|"KAKAO"|"CARD"...
        Integer pointsUsed
) {}
