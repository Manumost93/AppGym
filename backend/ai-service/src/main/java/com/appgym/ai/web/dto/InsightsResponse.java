package com.appgym.ai.web.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

public record InsightsResponse(
        @JsonPropertyDescription("Resumen ejecutivo de 2-3 frases sobre la ocupacion del negocio")
        String summary,

        @JsonPropertyDescription("Entre 2 y 5 observaciones concretas y accionables, una frase cada una")
        List<String> highlights
) {
}
