package com.gudrhs8304.ticketory.feature.story.dto.response;

import com.gudrhs8304.ticketory.feature.booking.enums.BookingPayStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class EligibleBookingRes {
    private Long bookingId;
    private Long movieId;
    private String movieTitle;
    private LocalDateTime screeningStartAt;
    private LocalDateTime screeningEndAt;
    private String screenName;
    private BookingPayStatus paymentStatus; // PAID, PENDING 등
    private boolean hasStory;               // 동일 bookingId로 이미 스토리 작성했는지
}
