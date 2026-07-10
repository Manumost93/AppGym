package com.appgym.ai.web;

import com.appgym.ai.service.ChatService;
import com.appgym.ai.web.dto.ChatRequest;
import com.appgym.ai.web.dto.ChatResponse;
import com.appgym.common.security.JwtClaims;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponse chat(
            @RequestHeader(value = JwtClaims.HEADER_BUSINESS_ID, required = false) UUID businessId,
            @Valid @RequestBody ChatRequest request) {
        return new ChatResponse(chatService.chat(businessId, request.messages()));
    }
}
