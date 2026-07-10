package com.appgym.ai.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.appgym.ai.client.BusinessInfo;
import com.appgym.ai.client.BusinessServiceClient;
import com.appgym.ai.config.AnthropicProperties;
import com.appgym.ai.web.dto.ChatMessageDto;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private static final String BASE_SYSTEM_PROMPT = """
            Eres el asistente virtual de AppGym, una plataforma para gestionar gimnasios,
            boxes de crossfit y clubes de padel. Ayudas a los socios con dudas sobre clases,
            reservas, planes de membresia y horarios. Responde en espanol, de forma breve,
            cercana y concreta. Si no tienes informacion suficiente para responder algo
            especifico del negocio (precios exactos, normas internas, etc.), dilo con
            honestidad y sugiere contactar con el negocio directamente.
            """;

    private final AnthropicClient client;
    private final AnthropicProperties properties;
    private final BusinessServiceClient businessServiceClient;

    public ChatService(AnthropicClient client, AnthropicProperties properties,
                        BusinessServiceClient businessServiceClient) {
        this.client = client;
        this.properties = properties;
        this.businessServiceClient = businessServiceClient;
    }

    public String chat(UUID businessId, List<ChatMessageDto> history) {
        String systemPrompt = buildSystemPrompt(businessId);

        List<MessageParam> messages = history.stream()
                .map(m -> MessageParam.builder()
                        .role("assistant".equals(m.role()) ? MessageParam.Role.ASSISTANT : MessageParam.Role.USER)
                        .content(m.content())
                        .build())
                .toList();

        MessageCreateParams params = MessageCreateParams.builder()
                .model(properties.model().chat())
                .maxTokens(1024L)
                .system(systemPrompt)
                .messages(messages)
                .build();

        Message response = client.messages().create(params);

        return response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(text -> text.text())
                .findFirst()
                .orElse("Lo siento, no he podido generar una respuesta.");
    }

    private String buildSystemPrompt(UUID businessId) {
        if (businessId == null) {
            return BASE_SYSTEM_PROMPT;
        }
        return businessServiceClient.findByBusinessId(businessId)
                .map(BusinessInfo::name)
                .map(name -> BASE_SYSTEM_PROMPT + "\nEl negocio del usuario que te escribe se llama \"" + name + "\".")
                .orElse(BASE_SYSTEM_PROMPT);
    }
}
