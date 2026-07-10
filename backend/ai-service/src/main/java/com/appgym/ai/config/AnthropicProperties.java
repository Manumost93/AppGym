package com.appgym.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "anthropic")
public record AnthropicProperties(String apiKey, Model model) {

    public record Model(String chat, String recommend, String insights) {
    }
}
