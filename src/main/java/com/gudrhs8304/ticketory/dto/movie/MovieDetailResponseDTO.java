package com.gudrhs8304.ticketory.dto.movie;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Value
@Builder
@Schema(name = "MovieDetailResponse")
public class MovieDetailResponseDTO {
    Long movieId;
    String title;
    String subtitle;
    String summary;
    String genre;
    String rating;
    Integer runningMinutes;
    LocalDate releaseDate;
    Boolean status;
    String actors;
    String director;
    String posterUrl;

    @Schema(description = "스틸컷 이미지 URL들")
    List<String> stillcutUrls;

    @Schema(description = "예고편 URL (없으면 null)")
    String trailerUrl;

    public static MovieDetailResponseDTO from(MovieDetailDTO dto) {
        return MovieDetailResponseDTO.builder()
                .movieId(dto.getMovieId())
                .title(dto.getTitle())
                .subtitle(dto.getSubtitle())
                .summary(dto.getSummary())
                .genre(dto.getGenre())
                .rating(dto.getRating())
                .runningMinutes(dto.getRunningMinutes())
                .releaseDate(dto.getReleaseDate())
                .status(dto.getStatus())
                .actors(dto.getActors())
                .director(dto.getDirector())
                .posterUrl(dto.getPosterUrl())
                .stillcutUrls(dto.getStillcutUrls() == null ? Collections.emptyList() : dto.getStillcutUrls())
                .trailerUrl(dto.getTrailerUrl())
                .build();
    }
}