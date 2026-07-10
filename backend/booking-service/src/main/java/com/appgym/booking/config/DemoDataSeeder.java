package com.appgym.booking.config;

import com.appgym.booking.domain.Activity;
import com.appgym.booking.domain.Booking;
import com.appgym.booking.domain.ScheduleSlot;
import com.appgym.booking.repository.ActivityRepository;
import com.appgym.booking.repository.BookingRepository;
import com.appgym.booking.repository.ScheduleSlotRepository;
import com.appgym.common.demo.DemoSeedIds;
import com.appgym.common.dto.ActivityType;
import com.appgym.common.dto.BookingStatus;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Crea un par de actividades del negocio de demostracion (mismo business_id
 * que auth-service/business-service), unas franjas horarias en los proximos
 * dias, y una reserva confirmada del socio demo, para que la pagina de
 * reservas y el recomendador/insights de ai-service tengan datos reales
 * desde el primer arranque.
 */
@Component
@ConditionalOnProperty(value = "appgym.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private static final UUID WOD_ACTIVITY_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID PADEL_ACTIVITY_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");

    private final ActivityRepository activityRepository;
    private final ScheduleSlotRepository slotRepository;
    private final BookingRepository bookingRepository;

    public DemoDataSeeder(ActivityRepository activityRepository, ScheduleSlotRepository slotRepository,
                           BookingRepository bookingRepository) {
        this.activityRepository = activityRepository;
        this.slotRepository = slotRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (activityRepository.existsById(WOD_ACTIVITY_ID)) {
            return;
        }

        Activity wod = new Activity(WOD_ACTIVITY_ID, DemoSeedIds.BUSINESS_ID, ActivityType.CLASS,
                "WOD del dia", "Entrenamiento funcional en grupo, apto para todos los niveles.",
                12, 60, "Coach Ana");
        activityRepository.save(wod);

        Activity padel = new Activity(PADEL_ACTIVITY_ID, DemoSeedIds.BUSINESS_ID, ActivityType.COURT,
                "Pista de padel 1", "Pista cubierta.", 4, 90, null);
        activityRepository.save(padel);

        Instant tomorrow9am = nextDayAt(1, 9);
        ScheduleSlot wodSlot1 = saveSlot(wod, tomorrow9am);
        saveSlot(wod, nextDayAt(2, 9));
        saveSlot(wod, nextDayAt(3, 18));
        saveSlot(padel, nextDayAt(1, 17));
        saveSlot(padel, nextDayAt(2, 19));

        Booking demoBooking = new Booking(wodSlot1.getId(), DemoSeedIds.BUSINESS_ID, DemoSeedIds.MEMBER_USER_ID,
                BookingStatus.CONFIRMED);
        bookingRepository.save(demoBooking);

        log.info("Datos de demostracion sembrados: 2 actividades, 5 franjas horarias, 1 reserva confirmada");
    }

    private ScheduleSlot saveSlot(Activity activity, Instant startTime) {
        Instant endTime = startTime.plusSeconds(activity.getDurationMinutes() * 60L);
        ScheduleSlot slot = new ScheduleSlot(activity.getId(), DemoSeedIds.BUSINESS_ID, startTime, endTime,
                activity.getCapacity());
        return slotRepository.save(slot);
    }

    private Instant nextDayAt(int daysAhead, int hourUtc) {
        return Instant.now().truncatedTo(ChronoUnit.DAYS).plus(daysAhead, ChronoUnit.DAYS).plus(hourUtc, ChronoUnit.HOURS);
    }
}
