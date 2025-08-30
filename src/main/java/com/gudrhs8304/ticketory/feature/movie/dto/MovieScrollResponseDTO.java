package com.gudrhs8304.ticketory.feature.movie.dto;

import java.util.List;

public record MovieScrollResponseDTO(
        List<MovieListItemDTO> items,
        Long nextCursor
) {
    public static MovieScrollResponseDTO of(List<MovieListItemDTO> rows, Long nextCursor) {
        return new MovieScrollResponseDTO(rows, nextCursor);
    }
}
