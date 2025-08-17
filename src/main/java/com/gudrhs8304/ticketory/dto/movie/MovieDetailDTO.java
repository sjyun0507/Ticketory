package com.gudrhs8304.ticketory.dto.movie;

import com.gudrhs8304.ticketory.domain.Movie;
import com.gudrhs8304.ticketory.domain.MovieMedia;
import com.gudrhs8304.ticketory.domain.enums.MovieMediaType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

@Value
@Builder
public class MovieDetailDTO {
    Long movieId;
    String title;
    String subtitle;     // nullable
    String summary;
    String genre;
    String rating;
    Integer runningMinutes;
    LocalDate releaseDate;
    Boolean status;
    String actors;
    String director;
    String posterUrl;

    List<String> stillcutUrls; // 스틸컷 배열
    String trailerUrl;         // 예고편 1개 (없으면 null)

    public static MovieDetailDTO of(Movie movie, List<MovieMedia> medias) {
        List<String> stills = medias.stream()
                .filter(mm -> mm.getMovieMediaType() == MovieMediaType.STILL)
                .map(MovieMedia::getUrl)
                .toList();

        String trailer = medias.stream()
                .filter(mm -> mm.getMovieMediaType() == MovieMediaType.TRAILER)
                .map(MovieMedia::getUrl)
                .findFirst()
                .orElse(null);

        return new MovieDetailDTO(
                movie.getMovieId(),
                movie.getTitle(),
                movie.getSubtitle(),
                movie.getSummary(),
                movie.getGenre(),
                movie.getRating(),
                movie.getRunningMinutes(),
                movie.getReleaseDate(),
                movie.getStatus(),
                movie.getActors(),
                movie.getDirector(),
                movie.getPosterUrl(),
                stills,
                trailer
        );
    }
}
