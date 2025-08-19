package com.gudrhs8304.ticketory.dto.seats;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeatHoldResponseDTO {
    private List<Long> holdIds;
    private LocalDateTime expiresAt;
}
