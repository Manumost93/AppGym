package com.appgym.ai.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChatMessageDto(
        @Pattern(regexp = "user|assistant") String role,
        @NotBlank String content
) {
}
