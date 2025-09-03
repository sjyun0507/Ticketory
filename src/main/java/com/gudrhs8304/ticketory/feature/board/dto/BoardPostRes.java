package com.gudrhs8304.ticketory.feature.board.dto;

import com.gudrhs8304.ticketory.feature.board.enums.Type;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BoardPostRes(
        Long id,
        Type type,
        String title,
        String content,
        String bannerUrl,
        LocalDate startDate,
        LocalDate endDate,
        LocalDateTime publishAt,
        LocalDateTime createdAt
        , Boolean published
) {}
