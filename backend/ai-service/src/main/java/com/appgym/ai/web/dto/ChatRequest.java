package com.appgym.ai.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ChatRequest(@Valid @NotEmpty List<ChatMessageDto> messages) {
}
