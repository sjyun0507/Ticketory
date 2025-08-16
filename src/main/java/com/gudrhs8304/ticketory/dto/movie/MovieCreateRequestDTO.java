package com.gudrhs8304.ticketory.dto.movie;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MovieCreateRequestDTO(
        @NotBlank
        @Schema(example = "인터스텔라")
        String title,

        @Schema(example = "우주 탐사 SF 대서사시")
        String summary,

        @Schema(example = "SF, 드라마")
        String genre,

        @Schema(example = "12")
        String rating,

        @Min(1) @Schema(example = "169")
        Integer runningMinutes,

        @Schema(example = "2014-11-06")
        LocalDate releaseDate,

        @Schema(example = "false")
        Boolean status,   // 현재 상영 여부 (null이면 기본 false)

        @Schema(example = "매튜 맥커너히, 앤 해서웨이")
        String actors,

        @Schema(example = "크리스토퍼 놀란")
        String director
) {}
