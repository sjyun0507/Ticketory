package com.gudrhs8304.ticketory.feature.screening.dto;

import com.gudrhs8304.ticketory.feature.screening.Screening;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class ScreeningAdminListItemDTO {
    Long screeningId;
    Long movieId;
    String movieTitle;
    Long screenId;
    String screenName;
    LocalDateTime startAt;
    LocalDateTime endAt;
    Boolean active;

    public static ScreeningAdminListItemDTO from(Screening s) {
        return ScreeningAdminListItemDTO.builder()
                .screeningId(s.getScreeningId())
                .movieId(s.getMovie().getMovieId())
                .movieTitle(s.getMovie().getTitle())
                .screenId(s.getScreen().getScreenId())
                .screenName(s.getScreen().getName())
                .startAt(s.getStartAt())
                .endAt(s.getEndAt())
                .active(Boolean.TRUE) // 필요 시 상태 컬럼 추가해서 치환
                .build();
    }
}
