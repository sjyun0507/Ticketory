package com.gudrhs8304.ticketory.feature.movie.dto;

import java.time.LocalDate;

public record MoviePatchRequestDTO(
        String title,
        String subtitle,
        String summary,
        String genre,
        String rating,
        Integer runningMinutes,
        LocalDate releaseDate,
        Boolean status,
        String actors,
        String director
) {}
