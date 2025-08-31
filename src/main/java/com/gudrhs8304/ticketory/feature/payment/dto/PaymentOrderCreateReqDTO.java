package com.gudrhs8304.ticketory.feature.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
@Data
public class PaymentOrderCreateReqDTO {
    private Long bookingId;   // 없으면 memberId 최신 PENDING으로 대체
    private Long memberId;    // 선택

    private BigDecimal usedPoint; // 프론트가 숫자로 보냄(0 가능)

    private String orderId;   // 비워두면 서버 생성

    // 프론트에서 오긴 하지만 서버 계산에 안씀 (그대로 둬도 됨)
    private String orderMethod;
    private String orderTime;
    private String status;
    private Integer earnedPoint;
    private List<Object> items;
}
