package com.gudrhs8304.ticketory.feature.story.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CommentUpdateReq(@NotBlank String content) {}
