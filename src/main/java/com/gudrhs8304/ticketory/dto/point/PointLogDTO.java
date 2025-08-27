package com.gudrhs8304.ticketory.dto.point;

import com.gudrhs8304.ticketory.domain.PointLog;
import com.gudrhs8304.ticketory.domain.enums.PointChangeType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PointLogDTO {
    private Long id;
    private PointChangeType changeType;
    private Integer amount;         // +적립 / -사용 / -회수 등
    private Integer balanceAfter;   // 이 변경 후 잔액
    private String description;
    private Long bookingId;
    private Long paymentId;
    private LocalDateTime createdAt;

    public static PointLogDTO from(PointLog pl) {
        return PointLogDTO.builder()
                .id(pl.getId())
                .changeType(pl.getChangeType())
                .amount(pl.getAmount())
                .balanceAfter(pl.getBalanceAfter())
                .description(pl.getDescription())
                .bookingId(pl.getBooking() != null ? pl.getBooking().getBookingId() : null)
                .paymentId(pl.getPayment() != null ? pl.getPayment().getPaymentId() : null)
                .createdAt(pl.getCreatedAt())
                .build();
    }
}
