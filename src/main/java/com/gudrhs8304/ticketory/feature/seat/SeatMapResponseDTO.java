package com.gudrhs8304.ticketory.feature.seat;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeatMapResponseDTO {
    private Long screeningId;
    private Long screenId;

    private List<String> rows;   // ["A","B", ...]
    private int cols;            // 20

    private List<SeatItem> seats;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SeatItem {
        private Long seatId;
        private String rowLabel;
        private Integer colNumber;
        private SeatStatusType status; // AVAILABLE / HOLD / BOOKED
        private String type;

        // 프론트에서 바로 쓰는 ID ("A1", "B2")
        public String getId() {
            return rowLabel + colNumber;
        }
    }
}
