package com.appgym.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * URLs internas (red de docker-compose) de los otros microservicios. ai-service
 * las llama directamente, sin pasar por api-gateway, fijando el mismo tipo de
 * cabeceras de confianza que el gateway ya usa (X-Business-Id / X-User-Id),
 * ya que ambos servicios son alcanzables solo dentro de la red interna.
 */
@ConfigurationProperties(prefix = "internal-services")
public record InternalServicesProperties(String businessServiceUrl, String bookingServiceUrl) {
}
