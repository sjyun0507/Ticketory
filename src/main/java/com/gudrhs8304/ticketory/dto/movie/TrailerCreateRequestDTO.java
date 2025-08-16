package com.gudrhs8304.ticketory.dto.movie;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record TrailerCreateRequestDTO(
        @Schema(example = "https://www.youtube.com/watch?v=xxxx")
        @NotBlank String url,
        @Schema(example = "메인 트레일러") String description
) {}
