package com.gudrhs8304.ticketory.feature.pricing.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 적용된 할인 1건 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountLine {
    private String code;    // GLOBAL_WED_DISCOUNT
    private String name;    // 수요일 할인
    private int percent;    // 20
    private int amount;     // 할인액
}
