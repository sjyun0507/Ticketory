package com.gudrhs8304.ticketory.dto.booking;

import com.gudrhs8304.ticketory.domain.enums.BookingPayStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Data
@NoArgsConstructor
public class BookingSummaryDTO {
    private Long bookingId;

    // 상영/영화/상영관 정보
    private String movieTitle;
    private LocalDateTime screeningStartAt;
    private LocalDateTime screeningEndAt;
    private String screenName;
    private String screenLocation;

    // 좌석 정보 (쿼리에서 안 뽑으면 null/빈값으로 둠)
    private List<String> seats;

    // 결제/상태
    private BookingPayStatus paymentStatus;
    private BigDecimal totalPrice;

    private String posterUrl;

    // === JPQL이 LocalDateTime을 주는 경우 (8개 파라미터) ===
    public BookingSummaryDTO(
            Long bookingId, String movieTitle,
            LocalDateTime startAt, LocalDateTime endAt,
            String screenName, String screenLocation,
            BigDecimal totalPrice, BookingPayStatus paymentStatus, String posterUrl
    ) {
        this.bookingId = bookingId;
        this.movieTitle = movieTitle;
        this.screeningStartAt = startAt;
        this.screeningEndAt = endAt;
        this.screenName = screenName;
        this.screenLocation = screenLocation;
        this.totalPrice = totalPrice;
        this.paymentStatus = paymentStatus;
        this.posterUrl = posterUrl;
    }



    // === 만약 엔티티가 Instant면 이 오버로드가 매칭됨 ===
    public BookingSummaryDTO(
            Long bookingId, String movieTitle,
            Instant startAt, Instant endAt,
            String screenName, String screenLocation,
            BigDecimal totalPrice, BookingPayStatus paymentStatus
    ) {
        ZoneId zone = ZoneId.systemDefault();
        this.bookingId = bookingId;
        this.movieTitle = movieTitle;
        this.screeningStartAt = startAt != null ? LocalDateTime.ofInstant(startAt, zone) : null;
        this.screeningEndAt   = endAt   != null ? LocalDateTime.ofInstant(endAt,   zone) : null;
        this.screenName = screenName;
        this.screenLocation = screenLocation;
        this.totalPrice = totalPrice;
        this.paymentStatus = paymentStatus;
    }
}