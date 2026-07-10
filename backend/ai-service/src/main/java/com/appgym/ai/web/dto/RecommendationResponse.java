package com.appgym.ai.web.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

/**
 * Forma de salida estructurada que le pedimos a Claude para /api/ai/recommend
 * (output_config.format), asi el frontend puede renderizar tarjetas sin tener
 * que parsear texto libre.
 */
public record RecommendationResponse(
        @JsonPropertyDescription("Una frase breve y cercana resumiendo la recomendacion")
        String headline,

        @JsonPropertyDescription("Entre 2 y 4 sugerencias concretas de clases, pistas u horarios, una frase cada una")
        List<String> suggestions
) {
}
