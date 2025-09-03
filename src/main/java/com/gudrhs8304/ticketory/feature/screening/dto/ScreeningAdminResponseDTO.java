package com.gudrhs8304.ticketory.feature.screening.dto;

import com.gudrhs8304.ticketory.feature.screening.domain.Screening;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class ScreeningAdminResponseDTO {
    Long screeningId;
    Long movieId;
    Long screenId;
    LocalDateTime startAt;
    LocalDateTime endAt;

    public static ScreeningAdminResponseDTO from(Screening s) {
        return ScreeningAdminResponseDTO.builder()
                .screeningId(s.getScreeningId())
                .movieId(s.getMovie().getMovieId())
                .screenId(s.getScreen().getScreenId())
                .startAt(s.getStartAt())
                .endAt(s.getEndAt())
                .build();
    }
}
