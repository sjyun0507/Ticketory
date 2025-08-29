package com.gudrhs8304.ticketory.mail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingAlarmDTO {
    private Long bookingId;
    private String toAddress;
    private String movieTitle;
    private LocalDateTime startAt;

    private String qrCodeUrl;
    private String posterUrl;
}
