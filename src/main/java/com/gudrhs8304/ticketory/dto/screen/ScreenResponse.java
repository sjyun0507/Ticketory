package com.gudrhs8304.ticketory.dto.screen;

public record ScreenResponse(
        Long screenId,
        String name,
        Integer rows,
        Integer cols,
        Long seatCount
) {}
