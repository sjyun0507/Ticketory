package com.gudrhs8304.ticketory.dto.movie;

import com.gudrhs8304.ticketory.domain.Movie;
import com.gudrhs8304.ticketory.domain.MovieMedia;
import com.gudrhs8304.ticketory.domain.enums.MovieMediaType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

    // 대표 포스터(단건, 카드/리스트용)
    String posterUrl;

    // 🔹 여러 포스터(상세 갤러리용)
    List<String> posterUrls;

    // 스틸컷 배열
    List<String> stillcutUrls;

    // 예고편 1개 (없으면 null)
    String trailerUrl;

    public static MovieDetailDTO of(Movie movie, List<MovieMedia> medias) {
        // 1) 스틸컷 수집
        List<String> stills = medias.stream()
                .filter(mm -> mm.getMovieMediaType() == MovieMediaType.STILL)
                .map(MovieMedia::getUrl)
                .collect(Collectors.toList());

        // 2) 예고편(단건)
        String trailer = medias.stream()
                .filter(mm -> mm.getMovieMediaType() == MovieMediaType.TRAILER)
                .map(MovieMedia::getUrl)
                .findFirst()
                .orElse(null);

        // 3) 포스터들 수집 (대표 포함, 중복 제거 + 순서 유지)
        LinkedHashSet<String> postersSet = new LinkedHashSet<>();

        // 대표 포스터를 맨 앞에
        if (movie.getPosterUrl() != null && !movie.getPosterUrl().isBlank()) {
            postersSet.add(movie.getPosterUrl());
        }

        // MovieMedia 의 POSTER 타입 추가
        medias.stream()
                .filter(mm -> mm.getMovieMediaType() == MovieMediaType.POSTER)
                .map(MovieMedia::getUrl)
                .forEach(url -> {
                    if (url != null && !url.isBlank()) postersSet.add(url);
                });

        List<String> posters = new ArrayList<>(postersSet);

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
                // 대표 포스터(단건)
                movie.getPosterUrl(),
                // 여러 포스터
                posters,
                // 스틸컷
                stills,
                // 예고편
                trailer
        );
    }
}