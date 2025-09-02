package com.gudrhs8304.ticketory.feature.screening.dto;

import com.gudrhs8304.ticketory.feature.screening.domain.Screening;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ScreeningItemDTO {
    private Long screeningId;
    private Long movieId;

    private Long screenId;
    private String screenName;
    private String location;   // Screen.location => region 대체

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    public static ScreeningItemDTO from(Screening s) {
        var sc = s.getScreen();
        return ScreeningItemDTO.builder()
                .screeningId(s.getScreeningId())
                .movieId(s.getMovie().getMovieId())
                .screenId(sc.getScreenId())
                .screenName(sc.getName())
                .location(sc.getLocation())
                .startAt(s.getStartAt())
                .endAt(s.getEndAt())
                .build();
    }
}