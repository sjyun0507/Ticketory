package com.gudrhs8304.ticketory.feature.story.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public record BookingSummaryRes(
        Long bookingId,
        Long screeningId,
        Long movieId,
        String movieTitle,
        LocalDateTime startAt,
        LocalDateTime endAt,
        BigDecimal paidAmount
) {}
