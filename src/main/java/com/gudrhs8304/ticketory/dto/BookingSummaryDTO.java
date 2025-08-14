package com.gudrhs8304.ticketory.dto;

import com.gudrhs8304.ticketory.domain.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;



@Data
@NoArgsConstructor
public class BookingSummaryDTO {
    private Long bookingId;

    // 상영/영화/상영관 정보
    private String movieTitle;
    private LocalDateTime screeningStartAt;
    private LocalDateTime screeningEndAt; // 필요 없으면 제거 가능

    // 좌석 정보 (A5, A6 ...)
    private List<String> seats;

    // 결제/상태
    private PaymentStatus paymentStatus;   // PENDING/PAID/CANCELLED
    private BigDecimal totalPrice;      // "12000" 또는 "12,000" 등 포맷은 프론트에서 해도 됨
    private String qrCodeUrl;       // null 가능

    // 생성 시각(BaseTimeEntity.createdAt)
    private LocalDateTime bookedAt; // = booking.createdAt

    // JPQL new(...) 에서 사용할 생성자 (6개)
    public BookingSummaryDTO(Long bookingId, String movieTitle,
                             LocalDateTime startAt, LocalDateTime endAt,
                             BigDecimal totalPrice, PaymentStatus paymentStatus) {
        this.bookingId = bookingId;
        this.movieTitle = movieTitle;
        this.screeningStartAt = startAt;
        this.screeningEndAt = endAt;
        this.totalPrice = totalPrice;
        this.paymentStatus = paymentStatus;
    }

}