package com.gudrhs8304.ticketory.dto.board;

import com.gudrhs8304.ticketory.domain.enums.Type;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BoardPostReq(
        @NotNull Type type,
        @NotBlank String title,
        @NotBlank String content,
        String bannerUrl,
        LocalDate startDate,
        LocalDate endDate,
        Boolean published   // null이면 기본 true
) {}
