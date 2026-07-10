package com.appgym.booking.service;

import com.appgym.booking.domain.Activity;
import com.appgym.booking.repository.ActivityRepository;
import com.appgym.booking.web.dto.ActivityRequest;
import com.appgym.booking.web.dto.ActivityResponse;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityService {

    private final ActivityRepository repository;

    public ActivityService(ActivityRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ActivityResponse create(UUID businessId, ActivityRequest request) {
        Activity activity = new Activity(
                UUID.randomUUID(),
                businessId,
                request.type(),
                request.name(),
                request.description(),
                request.capacity(),
                request.durationMinutes(),
                request.instructorName()
        );
        repository.save(activity);
        return ActivityResponse.from(activity);
    }

    public List<ActivityResponse> listActive(UUID businessId) {
        return repository.findByBusinessIdAndActiveTrueOrderByNameAsc(businessId).stream()
                .map(ActivityResponse::from)
                .toList();
    }

    @Transactional
    public ActivityResponse update(UUID businessId, UUID activityId, ActivityRequest request) {
        Activity activity = findOwned(businessId, activityId);
        activity.setName(request.name());
        activity.setDescription(request.description());
        activity.setCapacity(request.capacity());
        activity.setDurationMinutes(request.durationMinutes());
        activity.setInstructorName(request.instructorName());
        return ActivityResponse.from(activity);
    }

    @Transactional
    public void deactivate(UUID businessId, UUID activityId) {
        Activity activity = findOwned(businessId, activityId);
        activity.setActive(false);
    }

    Activity findOwned(UUID businessId, UUID activityId) {
        return repository.findByIdAndBusinessId(activityId, businessId)
                .orElseThrow(() -> new NoSuchElementException("Actividad no encontrada: " + activityId));
    }
}
