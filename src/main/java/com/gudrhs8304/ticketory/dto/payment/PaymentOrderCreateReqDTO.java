package com.gudrhs8304.ticketory.dto.payment;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class PaymentOrderCreateReqDTO {
    private Long memberId;
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
