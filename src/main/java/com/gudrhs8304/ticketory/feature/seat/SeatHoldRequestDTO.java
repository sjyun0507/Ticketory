package com.gudrhs8304.ticketory.feature.seat;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeatHoldRequestDTO {
    private Long screeningId;
    private List<Long> seatIds;
    private Integer holdSeconds; // 기본 120초
    private String holdKey;     // 선택
}
