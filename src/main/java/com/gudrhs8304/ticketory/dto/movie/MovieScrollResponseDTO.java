package com.gudrhs8304.ticketory.dto.movie;

import com.gudrhs8304.ticketory.domain.Movie;
import java.util.List;

public record MovieScrollResponseDTO(
        List<MovieListItemDTO> items,
        Long nextCursor
) {
    public static MovieScrollResponseDTO of(List<MovieListItemDTO> rows, Long nextCursor) {
        return new MovieScrollResponseDTO(rows, nextCursor);
    }
}
