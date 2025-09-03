package com.gudrhs8304.ticketory.feature.movie.dto;

import com.gudrhs8304.ticketory.feature.movie.domain.Movie;
import lombok.Builder;

import java.time.LocalDate;

@Builder

public record MovieListItemDTO(
        Long movieId,
        String title,
        String posterUrl,
        String genre,
        String rating,
        LocalDate releaseDate,
        Boolean status,
        String summary,
        Integer runningMinutes
) {
    public static MovieListItemDTO from(Movie m) {
        return new MovieListItemDTO(
                m.getMovieId(),
                m.getTitle(),
                m.getPosterUrl(),
                m.getGenre(),
                m.getRating(),
                m.getReleaseDate(),
                m.getStatus(),
                m.getSummary(),
                m.getRunningMinutes()
        );
    }
}

