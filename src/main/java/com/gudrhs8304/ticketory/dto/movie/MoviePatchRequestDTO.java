package com.gudrhs8304.ticketory.dto.movie;

import java.time.LocalDate;

public record MoviePatchRequestDTO(
        String title,
        String summary,
        String genre,
        String rating,
        Integer runningMinutes,
        LocalDate releaseDate,
        Boolean status,
        String actors,
        String director
) {}
