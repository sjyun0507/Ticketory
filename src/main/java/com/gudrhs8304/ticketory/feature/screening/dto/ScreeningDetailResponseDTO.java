package com.gudrhs8304.ticketory.feature.screening.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ScreeningDetailResponseDTO {
    private Long screeningId;

    // 영화 정보(필요 최소)
    private Long movieId;
    private String movieTitle;
    private String posterUrl;

    // 상영관 정보
    private Long screenId;
    private String screenName;
    private String location;     // Screen.location
    private Integer rowCount;
    private Integer colCount;

    // 상영 시간
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    // 💰 상영관 기본 요금
    private long basePrice;
}
