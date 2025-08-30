package com.gudrhs8304.ticketory.feature.booking.dto;

import java.math.BigDecimal;
import java.util.List;

public record InitBookingResponseDTO(
        Long bookingId,
        Long paymentId,
        List<Long> holdIds,
        String expiresAt,
        String paymentStatus,
        String provider,
        BigDecimal totalAmount,
        Integer pointsUsed,
        BigDecimal payableAmount
) {}
