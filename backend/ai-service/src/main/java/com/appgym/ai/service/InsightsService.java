package com.appgym.ai.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.appgym.ai.client.BookingServiceClient;
import com.appgym.ai.client.SlotSummary;
import com.appgym.ai.config.AnthropicProperties;
import com.appgym.ai.web.dto.InsightsResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Genera un resumen ejecutivo para el BUSINESS_ADMIN a partir de datos ya
 * agregados por este propio servicio (ocupacion de franjas horarias de los
 * ultimos 30 dias y los proximos 14). Claude nunca ve datos en bruto de la
 * base de datos: solo el resumen numerico que le pasamos.
 */
@Service
public class InsightsService {

    private static final String SYSTEM_PROMPT = """
            Eres el analista de datos de AppGym. Recibes un resumen agregado de ocupacion
            de un negocio (gimnasio, box de crossfit o club de padel) y debes redactar un
            resumen ejecutivo breve y observaciones accionables para el dueno del negocio.
            No inventes cifras que no se te den. Responde siempre en espanol.
            """;

    private final AnthropicClient client;
    private final AnthropicProperties properties;
    private final BookingServiceClient bookingServiceClient;

    public InsightsService(AnthropicClient client, AnthropicProperties properties,
                            BookingServiceClient bookingServiceClient) {
        this.client = client;
        this.properties = properties;
        this.bookingServiceClient = bookingServiceClient;
    }

    public InsightsResponse insights(UUID businessId) {
        Instant now = Instant.now();
        List<SlotSummary> slots = bookingServiceClient.findUpcomingSlots(businessId,
                now.minus(30, ChronoUnit.DAYS), now.plus(14, ChronoUnit.DAYS));

        if (slots.isEmpty()) {
            return new InsightsResponse(
                    "Todavia no hay franjas horarias suficientes para generar un analisis.",
                    List.of("Crea actividades y franjas horarias para empezar a recibir insights."));
        }

        String aggregatedData = aggregate(slots);

        StructuredMessageCreateParams<InsightsResponse> params = MessageCreateParams.builder()
                .model(properties.model().insights())
                .maxTokens(768L)
                .system(SYSTEM_PROMPT)
                .addUserMessage("Datos agregados de ocupacion (ultimos 30 dias y proximos 14):\n\n" + aggregatedData)
                .outputConfig(InsightsResponse.class)
                .build();

        StructuredMessage<InsightsResponse> response = client.messages().create(params);

        return response.content().stream()
                .flatMap(block -> block.text().stream())
                .findFirst()
                .map(typed -> typed.text())
                .orElse(new InsightsResponse("No se pudo generar el analisis en este momento.", List.of()));
    }

    private String aggregate(List<SlotSummary> slots) {
        int totalSlots = slots.size();
        long totalConfirmed = slots.stream().mapToLong(SlotSummary::confirmedCount).sum();
        long totalCapacity = slots.stream().mapToLong(SlotSummary::capacity).sum();
        long slotsWithWaitlist = slots.stream().filter(s -> s.waitlistCount() > 0).count();
        long fullSlots = slots.stream().filter(SlotSummary::full).count();
        double occupancyRate = totalCapacity == 0 ? 0 : (100.0 * totalConfirmed / totalCapacity);

        Map<String, Long> countByActivity = slots.stream()
                .collect(Collectors.groupingBy(SlotSummary::activityName, Collectors.counting()));
        String topActivity = countByActivity.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> e.getKey() + " (" + e.getValue() + " franjas)")
                .orElse("sin datos");

        return """
                Total de franjas horarias analizadas: %d
                Ocupacion media: %.1f%% (%d plazas confirmadas de %d totales)
                Franjas completas (sin plazas libres): %d
                Franjas con lista de espera: %d
                Actividad mas programada: %s
                """.formatted(totalSlots, occupancyRate, totalConfirmed, totalCapacity, fullSlots,
                slotsWithWaitlist, topActivity);
    }
}
