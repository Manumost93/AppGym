package com.appgym.booking;

import static org.assertj.core.api.Assertions.assertThat;

import com.appgym.booking.service.ActivityService;
import com.appgym.booking.service.BookingService;
import com.appgym.booking.service.ScheduleSlotService;
import com.appgym.booking.web.dto.ActivityRequest;
import com.appgym.booking.web.dto.ActivityResponse;
import com.appgym.booking.web.dto.BookingResponse;
import com.appgym.booking.web.dto.ScheduleSlotRequest;
import com.appgym.booking.web.dto.ScheduleSlotResponse;
import com.appgym.common.dto.ActivityType;
import com.appgym.common.dto.BookingStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Verifica contra un Postgres real (Testcontainers) la regla de negocio mas
 * delicada de booking-service: el cupo de un slot nunca se supera y, al
 * liberarse una plaza confirmada, la reserva mas antigua en lista de espera
 * se promociona automaticamente.
 */
@SpringBootTest
@Testcontainers
class BookingFlowIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ScheduleSlotService slotService;

    @Autowired
    private BookingService bookingService;

    @Test
    void secondBookingWaitlistsAndPromotesOnCancellation() {
        UUID businessId = UUID.randomUUID();

        ActivityResponse activity = activityService.create(businessId,
                new ActivityRequest(ActivityType.CLASS, "WOD del dia", "Entrenamiento funcional", 1, 60, "Coach Ana"));

        ScheduleSlotResponse slot = slotService.create(businessId,
                new ScheduleSlotRequest(activity.id(), Instant.now().plusSeconds(3600)));

        UUID memberA = UUID.randomUUID();
        UUID memberB = UUID.randomUUID();

        BookingResponse bookingA = bookingService.book(businessId, memberA, slot.id());
        assertThat(bookingA.status()).isEqualTo(BookingStatus.CONFIRMED);

        BookingResponse bookingB = bookingService.book(businessId, memberB, slot.id());
        assertThat(bookingB.status()).isEqualTo(BookingStatus.WAITLIST);

        bookingService.cancel(memberA, bookingA.id());

        BookingResponse memberBAfterPromotion = bookingService.listMine(memberB).get(0);
        assertThat(memberBAfterPromotion.status()).isEqualTo(BookingStatus.CONFIRMED);
    }
}
