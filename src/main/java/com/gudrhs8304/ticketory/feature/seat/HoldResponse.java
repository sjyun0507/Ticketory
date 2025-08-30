package com.gudrhs8304.ticketory.feature.seat;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class HoldResponse {
    /** 선택한 예약(임시) id — 없으면 null */
    private Long bookingId;

    /** 상영 id */
    private Long screeningId;

    /** 이번에 홀드 성공한 좌석 id 목록 */
    private List<Long> seatIds;

    /** 생성된 seat_hold 행들의 id 목록 (필요 없으면 null/빈 리스트) */
    private List<Long> holdIds;

    /** 홀드 만료시각 */
    private LocalDateTime holdExpiresAt;

    /** 홀드 유지 시간(초) */
    private Integer holdSeconds;

    /* 필요하면 확장용 필드들 */
    private Long paymentId;          // PENDING 결제 id
    private String paymentStatus;    // "PENDING" 등
    private Integer totalPrice;      // 최종 결제 예정 금액(원)
}
