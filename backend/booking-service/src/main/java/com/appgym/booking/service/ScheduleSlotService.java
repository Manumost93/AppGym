package com.appgym.booking.service;

import com.appgym.booking.domain.Activity;
import com.appgym.booking.domain.ScheduleSlot;
import com.appgym.booking.repository.BookingRepository;
import com.appgym.booking.repository.ScheduleSlotRepository;
import com.appgym.booking.web.dto.ScheduleSlotRequest;
import com.appgym.booking.web.dto.ScheduleSlotResponse;
import com.appgym.common.dto.BookingStatus;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleSlotService {

    private final ScheduleSlotRepository slotRepository;
    private final BookingRepository bookingRepository;
    private final ActivityService activityService;

    public ScheduleSlotService(ScheduleSlotRepository slotRepository, BookingRepository bookingRepository,
                                ActivityService activityService) {
        this.slotRepository = slotRepository;
        this.bookingRepository = bookingRepository;
        this.activityService = activityService;
    }

    @Transactional
    public ScheduleSlotResponse create(UUID businessId, ScheduleSlotRequest request) {
        Activity activity = activityService.findOwned(businessId, request.activityId());
        Instant endTime = request.startTime().plusSeconds(activity.getDurationMinutes() * 60L);

        ScheduleSlot slot = new ScheduleSlot(activity.getId(), businessId, request.startTime(), endTime,
                activity.getCapacity());
        slotRepository.save(slot);

        return toResponse(slot, activity);
    }

    public List<ScheduleSlotResponse> listByRange(UUID businessId, Instant from, Instant to) {
        List<ScheduleSlot> slots = slotRepository.findByBusinessIdAndStartTimeBetweenOrderByStartTimeAsc(
                businessId, from, to);

        return slots.stream()
                .map(slot -> toResponse(slot, activityService.findOwned(businessId, slot.getActivityId())))
                .toList();
    }

    ScheduleSlot findOwned(UUID businessId, UUID slotId) {
        return slotRepository.findByIdAndBusinessId(slotId, businessId)
                .orElseThrow(() -> new NoSuchElementException("Franja horaria no encontrada: " + slotId));
    }

    private ScheduleSlotResponse toResponse(ScheduleSlot slot, Activity activity) {
        long confirmed = bookingRepository.countBySlotIdAndStatus(slot.getId(), BookingStatus.CONFIRMED);
        long waitlist = bookingRepository.countBySlotIdAndStatus(slot.getId(), BookingStatus.WAITLIST);

        return new ScheduleSlotResponse(
                slot.getId(),
                activity.getId(),
                activity.getName(),
                activity.getType(),
                activity.getInstructorName(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getCapacity(),
                confirmed,
                waitlist,
                confirmed >= slot.getCapacity()
        );
    }
}
