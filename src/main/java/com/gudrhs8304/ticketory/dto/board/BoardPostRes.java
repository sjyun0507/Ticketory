package com.gudrhs8304.ticketory.dto.board;

import com.gudrhs8304.ticketory.domain.enums.Type;

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
        Boolean published,
        LocalDateTime createdAt
) {}
