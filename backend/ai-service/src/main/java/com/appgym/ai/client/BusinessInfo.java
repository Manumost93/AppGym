package com.appgym.ai.client;

import com.appgym.common.dto.BusinessType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Subconjunto de la respuesta de business-service que necesita ai-service
 * para dar contexto al asistente (nombre y tipo de negocio).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BusinessInfo(String name, BusinessType type) {
}
