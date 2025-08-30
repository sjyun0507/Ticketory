package com.gudrhs8304.ticketory.feature.story.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StoryCreateRequest {
    @NotNull
    private Long bookingId;

    @NotBlank
    private String content;
}
