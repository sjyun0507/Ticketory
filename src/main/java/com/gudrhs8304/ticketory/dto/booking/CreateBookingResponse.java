package com.gudrhs8304.ticketory.dto.booking;


import java.math.BigDecimal;
import java.util.List;

public record CreateBookingResponse(
        Long bookingId,
        Long screeningId,
        List<Long> seatIds,
        BigDecimal totalPrice,
        String paymentStatus
) {}
