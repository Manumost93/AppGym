package com.appgym.booking.web.dto;

import com.appgym.common.dto.ActivityType;
import java.time.Instant;
import java.util.UUID;

public record ScheduleSlotResponse(
        UUID id,
        UUID activityId,
        String activityName,
        ActivityType activityType,
        String instructorName,
        Instant startTime,
        Instant endTime,
        int capacity,
        long confirmedCount,
        long waitlistCount,
        boolean full
) {
}
