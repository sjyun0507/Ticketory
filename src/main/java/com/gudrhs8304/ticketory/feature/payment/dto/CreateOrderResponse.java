package com.gudrhs8304.ticketory.feature.payment.dto;

import java.math.BigDecimal;

public record CreateOrderResponse(String orderId, BigDecimal totalAmount,
                                  int pointsUsed, BigDecimal payableAmount,
                                  Long paymentId, String paymentStatus) {}
