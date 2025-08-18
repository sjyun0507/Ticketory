package com.gudrhs8304.ticketory.dto.movie;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.gudrhs8304.ticketory.controller.ProxyController.proxify;

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

    @Schema(description = "갤러리(포스터+스틸컷) URL들")
    List<String> stillcutUrls;

    @Schema(description = "예고편 URL (없으면 null)")
    String trailerUrl;

    public static MovieDetailResponseDTO from(MovieDetailDTO dto) {

        // 1) 대표 포스터 + 스틸컷 → 갤러리로 합치기 (중복 제거, 순서 유지)
        LinkedHashSet<String> gallery = new LinkedHashSet<>();

        if (dto.getPosterUrl() != null && !dto.getPosterUrl().isBlank()) {
            gallery.add(proxify(dto.getPosterUrl()));
        }

        if (dto.getStillcutUrls() != null) {
            for (String u : dto.getStillcutUrls()) {
                if (u == null || u.isBlank()) continue;
                gallery.add(proxify(u));
            }
        }

        // 2) 응답 빌드
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
                // 대표 포스터도 프록시로 정규화
                .posterUrl(dto.getPosterUrl() == null ? null : proxify(dto.getPosterUrl()))
                // 프론트 수정없이 갤러리로 쓰게 유지 (포스터+스틸컷)
                .stillcutUrls(new ArrayList<>(gallery))
                .trailerUrl(dto.getTrailerUrl())
                .build();
    }
}