package com.gudrhs8304.ticketory.feature.pricing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QuoteItemReq {

    /**
     * 예: "ADULT", "TEEN", "CHILD"
     */
    @NotBlank
    private String kind;

    @Min(0)
    private int unitPrice;

    @Min(1)
    private int qty;
}
