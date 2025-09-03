package com.gudrhs8304.ticketory.feature.booking.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CancellationHistoryRes {
    private Long cancelId;
    private Long bookingId;
    private String title;
    private Integer canceledAmount;
    private LocalDateTime canceledAt;
    private String reason;

    // ★ Refund 관련 추가
    private String refundTid;
    private LocalDateTime refundedAt;
}
