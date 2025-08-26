package com.gudrhs8304.ticketory.dto.booking;

import java.math.BigDecimal;
import java.util.List;

public record InitBookingResponseDTO(
        Long bookingId,
        Long paymentId,
        List<Long> holdIds,
        String holdExpiresAt,       // ISO-8601 문자열
        String paymentStatus,       // "PENDING"
        String provider,
        BigDecimal totalPrice
) {}
