package com.gudrhs8304.ticketory.feature.story.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StoryCreateRequest {
    @NotNull
    private Long bookingId;

    @NotNull
    @DecimalMin("0.0") @DecimalMax("5.0")
    @Digits(integer = 1, fraction = 1)  // 0.0~5.0, 소수 1자리
    private BigDecimal rating;

    @NotBlank
    private String content;
}
