package com.gudrhs8304.ticketory.dto.movie;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

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

    /** 카드/상단에 쓸 대표 포스터 */
    String posterUrl;

    /** 하단 갤러리(포스터 + 스틸컷) */
    @Schema(description = "갤러리(포스터+스틸컷) URL들")
    List<String> stillcutUrls;

    /** 모든 포스터 URL (디버그/추가 활용용) */
    @Schema(description = "모든 포스터 URL")
    List<String> posterUrls;

    @Schema(description = "예고편 URL (없으면 null)")
    String trailerUrl;

    public static MovieDetailResponseDTO from(MovieDetailDTO dto) {
        // 1) 포스터 리스트(대표 + 다중) 수집
        List<String> posters = new ArrayList<>();
        if (dto.getPosterUrls() != null && !dto.getPosterUrls().isEmpty()) {
            for (String u : dto.getPosterUrls()) {
                if (u != null && !u.isBlank()) posters.add(proxify(u));
            }
        }
        if (dto.getPosterUrl() != null && !dto.getPosterUrl().isBlank()) {
            String p = proxify(dto.getPosterUrl());
            if (!posters.contains(p)) posters.add(0, p); // 대표를 맨 앞에 보장
        }

        // 2) 갤러리 구성: 포스터(여러 장) → 스틸컷(중복 제거, 순서 유지)
        LinkedHashSet<String> gallery = new LinkedHashSet<>(posters);
        if (dto.getStillcutUrls() != null) {
            for (String u : dto.getStillcutUrls()) {
                if (u == null || u.isBlank()) continue;
                gallery.add(proxify(u));
            }
        }

        // 3) 대표 포스터 결정(없으면 포스터 리스트의 첫 항목)
        String mainPoster = dto.getPosterUrl();
        if (mainPoster == null || mainPoster.isBlank()) {
            mainPoster = posters.isEmpty() ? null : posters.get(0);
        } else {
            mainPoster = proxify(mainPoster);
        }

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
                .posterUrl(mainPoster)
                .stillcutUrls(new ArrayList<>(gallery))
                .posterUrls(posters) // ← 필요 시 프론트에서 개별 포스터 접근 가능
                .trailerUrl(dto.getTrailerUrl())
                .build();
    }
}