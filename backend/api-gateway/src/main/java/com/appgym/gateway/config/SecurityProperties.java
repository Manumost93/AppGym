package com.appgym.gateway.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security")
public record SecurityProperties(List<String> publicPaths) {

    public SecurityProperties {
        if (publicPaths == null) {
            publicPaths = List.of();
        }
    }
}
