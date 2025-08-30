package com.gudrhs8304.ticketory.feature.screen.dto;

public record ScreenResponse(
        Long screenId,
        String name,
        Integer rows,
        Integer cols,
        Long seatCount
) {}
