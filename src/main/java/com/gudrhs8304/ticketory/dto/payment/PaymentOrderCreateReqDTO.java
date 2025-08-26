package com.gudrhs8304.ticketory.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.List;
@Data
public class PaymentOrderCreateReqDTO {
    private Long bookingId;
    private Long memberId;
    private String orderId;
    @NotNull
    private long totalAmount;
    private long usedPoint;
    private String orderMethod;
    private String orderTime;
    private String status;
    private long earnedPoint;
    private List<Item> items;

    @Getter
    @Setter
    public static class Item {
        private Long id;
        private Long movieId;
        private Long screeningId;
        private Long seatId;
        private String name;
        private long price;
        private int quantity;
    }
}
