package com.gudrhs8304.ticketory.dto.movie;

import com.gudrhs8304.ticketory.domain.enums.MovieMediaType;

public record MovieMediaResponseDTO(
        Long mediaId,
        Long movieId,
        MovieMediaType mediaType,
        String url,
        String description
) {}
