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
 * Crea actividades y horarios propios de cada uno de los 3 negocios de
 * demostracion (gimnasio, box de crossfit, club de padel; ver DemoDataSeeder
 * de business-service), y una reserva confirmada del socio demo, para que la
 * pagina de reservas y el recomendador/insights de ai-service tengan datos
 * reales desde el primer arranque.
 */
@Component
@ConditionalOnProperty(value = "appgym.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private static final UUID WOD_ACTIVITY_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID WEIGHTLIFTING_ACTIVITY_ID = UUID.fromString("66666666-6666-6666-6666-666666666667");
    private static final UUID SPINNING_ACTIVITY_ID = UUID.fromString("66666666-6666-6666-6666-666666666668");
    private static final UUID YOGA_ACTIVITY_ID = UUID.fromString("66666666-6666-6666-6666-666666666669");
    private static final UUID PADEL_ACTIVITY_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");
    private static final UUID PADEL_ACTIVITY_ID_2 = UUID.fromString("77777777-7777-7777-7777-777777777778");

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

        // Box de crossfit
        Activity wod = save(new Activity(WOD_ACTIVITY_ID, DemoSeedIds.BUSINESS_ID, ActivityType.CLASS,
                "WOD del dia", "Entrenamiento funcional en grupo, apto para todos los niveles.",
                12, 60, "Coach Ana"));
        Activity weightlifting = save(new Activity(WEIGHTLIFTING_ACTIVITY_ID, DemoSeedIds.BUSINESS_ID, ActivityType.CLASS,
                "Halterofilia", "Tecnica de arrancada y dos tiempos, grupos reducidos.",
                8, 60, "Coach Ana"));

        // Gimnasio
        Activity spinning = save(new Activity(SPINNING_ACTIVITY_ID, DemoSeedIds.BUSINESS_ID_GYM, ActivityType.CLASS,
                "Spinning", "Clase de ciclo indoor de alta intensidad.", 20, 45, "Coach Luis"));
        Activity yoga = save(new Activity(YOGA_ACTIVITY_ID, DemoSeedIds.BUSINESS_ID_GYM, ActivityType.CLASS,
                "Yoga", "Sesion de movilidad y respiracion para todos los niveles.", 15, 60, "Coach Marta"));

        // Club de padel
        Activity padel1 = save(new Activity(PADEL_ACTIVITY_ID, DemoSeedIds.BUSINESS_ID_PADEL, ActivityType.COURT,
                "Pista de padel 1", "Pista cubierta.", 4, 90, null));
        Activity padel2 = save(new Activity(PADEL_ACTIVITY_ID_2, DemoSeedIds.BUSINESS_ID_PADEL, ActivityType.COURT,
                "Pista de padel 2", "Pista exterior con iluminacion nocturna.", 4, 90, null));

        ScheduleSlot wodSlot1 = saveSlot(wod, DemoSeedIds.BUSINESS_ID, nextDayAt(1, 9));
        saveSlot(wod, DemoSeedIds.BUSINESS_ID, nextDayAt(2, 9));
        saveSlot(wod, DemoSeedIds.BUSINESS_ID, nextDayAt(3, 18));
        saveSlot(weightlifting, DemoSeedIds.BUSINESS_ID, nextDayAt(2, 19));

        saveSlot(spinning, DemoSeedIds.BUSINESS_ID_GYM, nextDayAt(1, 8));
        saveSlot(spinning, DemoSeedIds.BUSINESS_ID_GYM, nextDayAt(3, 8));
        saveSlot(yoga, DemoSeedIds.BUSINESS_ID_GYM, nextDayAt(1, 19));
        saveSlot(yoga, DemoSeedIds.BUSINESS_ID_GYM, nextDayAt(4, 19));

        saveSlot(padel1, DemoSeedIds.BUSINESS_ID_PADEL, nextDayAt(1, 17));
        saveSlot(padel1, DemoSeedIds.BUSINESS_ID_PADEL, nextDayAt(2, 19));
        saveSlot(padel2, DemoSeedIds.BUSINESS_ID_PADEL, nextDayAt(1, 20));
        saveSlot(padel2, DemoSeedIds.BUSINESS_ID_PADEL, nextDayAt(3, 20));

        Booking demoBooking = new Booking(wodSlot1.getId(), DemoSeedIds.BUSINESS_ID, DemoSeedIds.MEMBER_USER_ID,
                BookingStatus.CONFIRMED);
        bookingRepository.save(demoBooking);

        log.info("Datos de demostracion sembrados: 6 actividades (3 negocios), 12 franjas horarias, 1 reserva confirmada");
    }

    private Activity save(Activity activity) {
        return activityRepository.save(activity);
    }

    private ScheduleSlot saveSlot(Activity activity, UUID businessId, Instant startTime) {
        Instant endTime = startTime.plusSeconds(activity.getDurationMinutes() * 60L);
        ScheduleSlot slot = new ScheduleSlot(activity.getId(), businessId, startTime, endTime, activity.getCapacity());
        return slotRepository.save(slot);
    }

    private Instant nextDayAt(int daysAhead, int hourUtc) {
        return Instant.now().truncatedTo(ChronoUnit.DAYS).plus(daysAhead, ChronoUnit.DAYS).plus(hourUtc, ChronoUnit.HOURS);
    }
}
