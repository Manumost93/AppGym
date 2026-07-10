package com.appgym.ai.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.appgym.ai.client.ActivityInfo;
import com.appgym.ai.client.BookingServiceClient;
import com.appgym.ai.client.BookingSummary;
import com.appgym.ai.client.SlotSummary;
import com.appgym.ai.config.AnthropicProperties;
import com.appgym.ai.web.dto.RecommendationResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class RecommendationService {

    private static final String SYSTEM_PROMPT = """
            Eres el recomendador de AppGym. A partir del historial de reservas de un socio
            y de las proximas clases o pistas disponibles, sugiere que deberia reservar a
            continuacion. Prioriza actividades similares a las que ya ha reservado, variedad
            de horarios, y plazas con disponibilidad real. Responde siempre en espanol.
            """;

    private final AnthropicClient client;
    private final AnthropicProperties properties;
    private final BookingServiceClient bookingServiceClient;

    public RecommendationService(AnthropicClient client, AnthropicProperties properties,
                                  BookingServiceClient bookingServiceClient) {
        this.client = client;
        this.properties = properties;
        this.bookingServiceClient = bookingServiceClient;
    }

    public RecommendationResponse recommend(UUID businessId, UUID memberId) {
        List<BookingSummary> history = bookingServiceClient.findBookingsForMember(memberId);
        List<ActivityInfo> activities = bookingServiceClient.findActivities(businessId);
        Instant now = Instant.now();
        List<SlotSummary> upcomingSlots = bookingServiceClient.findUpcomingSlots(businessId, now,
                now.plus(14, ChronoUnit.DAYS));

        if (history.isEmpty() && upcomingSlots.isEmpty()) {
            return new RecommendationResponse(
                    "Todavia no hay datos suficientes para hacerte una recomendacion personalizada.",
                    List.of("Echa un vistazo a las clases disponibles y reserva tu primera sesion."));
        }

        String prompt = buildPrompt(history, activities, upcomingSlots);

        StructuredMessageCreateParams<RecommendationResponse> params = MessageCreateParams.builder()
                .model(properties.model().recommend())
                .maxTokens(512L)
                .system(SYSTEM_PROMPT)
                .addUserMessage(prompt)
                .outputConfig(RecommendationResponse.class)
                .build();

        StructuredMessage<RecommendationResponse> response = client.messages().create(params);

        return response.content().stream()
                .flatMap(block -> block.text().stream())
                .findFirst()
                .map(typed -> typed.text())
                .orElse(new RecommendationResponse("No se pudo generar una recomendacion en este momento.", List.of()));
    }

    private String buildPrompt(List<BookingSummary> history, List<ActivityInfo> activities,
                                List<SlotSummary> upcomingSlots) {
        String historyText = history.isEmpty()
                ? "Sin reservas previas."
                : history.stream()
                        .map(b -> "- " + b.activityName() + " (" + b.status() + ")")
                        .collect(Collectors.joining("\n"));

        String activitiesText = activities.isEmpty()
                ? "Sin actividades registradas."
                : activities.stream()
                        .map(a -> "- " + a.name() + " [" + a.type() + "]"
                                + (a.instructorName() != null ? " con " + a.instructorName() : ""))
                        .collect(Collectors.joining("\n"));

        String slotsText = upcomingSlots.isEmpty()
                ? "Sin franjas horarias proximas."
                : upcomingSlots.stream()
                        .map(s -> "- " + s.activityName() + " el " + s.startTime()
                                + " (" + s.confirmedCount() + "/" + s.capacity() + " plazas ocupadas"
                                + (s.full() ? ", completo" : "") + ")")
                        .collect(Collectors.joining("\n"));

        return """
                Historial de reservas del socio:
                %s

                Actividades que ofrece el negocio:
                %s

                Proximas franjas horarias disponibles:
                %s
                """.formatted(historyText, activitiesText, slotsText);
    }
}
