package com.gudrhs8304.ticketory.feature.story.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StoryUpdateRequest {
    @NotBlank
    private String content;
}
