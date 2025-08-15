package com.gudrhs8304.ticketory.dto.error;

public record ApiError(String code, String message, String redirectUrl) {}