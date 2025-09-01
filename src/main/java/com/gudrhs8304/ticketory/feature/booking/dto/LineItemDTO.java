package com.gudrhs8304.ticketory.feature.booking.dto;

import java.math.BigDecimal;

public record LineItemDTO(
        String kind,            // "ADULT" | "TEEN"
        BigDecimal unitPrice,   // 규칙 적용된 단가
        int qty,                // 수량
        BigDecimal subtotal     // unitPrice * qty
) {
    public static LineItemDTO of(String kind, BigDecimal unitPrice, int qty) {
        BigDecimal sub = unitPrice.multiply(BigDecimal.valueOf(qty));
        return new LineItemDTO(kind, unitPrice, qty, sub);
    }
}
