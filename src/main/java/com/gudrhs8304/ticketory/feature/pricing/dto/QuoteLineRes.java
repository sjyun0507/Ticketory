package com.gudrhs8304.ticketory.feature.pricing.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 응답에 돌려주는 라인 상세 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuoteLineRes {
    private String kind;
    private int unitPrice;
    private int qty;
    private int subtotal;
}
