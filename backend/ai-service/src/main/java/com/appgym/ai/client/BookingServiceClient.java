package com.appgym.ai.client;

import com.appgym.ai.config.InternalServicesProperties;
import com.appgym.common.security.JwtClaims;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Llamadas internas (red de docker-compose) a booking-service. ai-service
 * nunca lee la base de datos de booking-service directamente: solo consume
 * estos endpoints ya existentes, igual que haria el gateway para un usuario.
 */
@Component
public class BookingServiceClient {

    private final RestClient restClient;

    public BookingServiceClient(InternalServicesProperties properties) {
        this.restClient = RestClient.builder().baseUrl(properties.bookingServiceUrl()).build();
    }

    public List<BookingSummary> findBookingsForMember(UUID memberId) {
        try {
            BookingSummary[] bookings = restClient.get()
                    .uri("/api/booking/bookings/me")
                    .header(JwtClaims.HEADER_USER_ID, memberId.toString())
                    .retrieve()
                    .body(BookingSummary[].class);
            return bookings != null ? List.of(bookings) : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public List<ActivityInfo> findActivities(UUID businessId) {
        try {
            ActivityInfo[] activities = restClient.get()
                    .uri("/api/booking/activities")
                    .header(JwtClaims.HEADER_BUSINESS_ID, businessId.toString())
                    .retrieve()
                    .body(ActivityInfo[].class);
            return activities != null ? List.of(activities) : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public List<SlotSummary> findUpcomingSlots(UUID businessId, Instant from, Instant to) {
        try {
            SlotSummary[] slots = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/booking/slots")
                            .queryParam("from", from)
                            .queryParam("to", to)
                            .build())
                    .header(JwtClaims.HEADER_BUSINESS_ID, businessId.toString())
                    .retrieve()
                    .body(SlotSummary[].class);
            return slots != null ? List.of(slots) : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }
}
