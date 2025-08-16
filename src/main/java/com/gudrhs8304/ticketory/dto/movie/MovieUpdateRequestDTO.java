package com.gudrhs8304.ticketory.dto.movie;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record MovieUpdateRequestDTO(
        @NotBlank String title,
        String summary,
        String genre,
        String rating,
        @Min(1) Integer runningMinutes,
        LocalDate releaseDate,
        Boolean status,
        String actors,
        String director
) {}
