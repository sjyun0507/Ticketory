package com.gudrhs8304.ticketory.feature.pricing.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuoteResponse {
    private int originalTotal;
    private int finalTotal;
    private List<DiscountLine> discounts;
    private List<QuoteLineRes> breakdown;
}
