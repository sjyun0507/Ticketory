package com.gudrhs8304.ticketory.feature.payment.dto;

public record CreateOrderRequest(Long bookingId, Integer usedPoint, String orderId) {}

