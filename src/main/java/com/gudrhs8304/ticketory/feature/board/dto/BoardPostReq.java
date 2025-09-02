package com.gudrhs8304.ticketory.feature.board.dto;

import com.gudrhs8304.ticketory.feature.board.enums.Type;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BoardPostReq(
        @NotNull Type type,
        @NotBlank String title,
        @NotBlank String content,
        String bannerUrl,
        LocalDate startDate,
        LocalDate endDate,
        LocalDateTime publishAt,   // null이면 기본 true
        Boolean published
) {}
