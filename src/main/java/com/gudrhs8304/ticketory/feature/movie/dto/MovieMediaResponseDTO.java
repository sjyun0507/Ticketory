package com.gudrhs8304.ticketory.feature.movie.dto;

import com.gudrhs8304.ticketory.feature.movie.MovieMediaType;

public record MovieMediaResponseDTO(
        Long mediaId,
        Long movieId,
        MovieMediaType mediaType,
        String url,
        String description
) {}
