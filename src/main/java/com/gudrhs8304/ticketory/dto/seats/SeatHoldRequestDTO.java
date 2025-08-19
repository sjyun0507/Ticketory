package com.gudrhs8304.ticketory.dto.seats;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeatHoldRequestDTO {
    private Long screeningId;
    private List<Long> seatIds;
    private Integer ttlSeconds; // 기본 180초
    private String holdKey;     // 선택
}
