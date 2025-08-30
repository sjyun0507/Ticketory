package com.gudrhs8304.ticketory.feature.story.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateReq(@NotBlank String content) {}

