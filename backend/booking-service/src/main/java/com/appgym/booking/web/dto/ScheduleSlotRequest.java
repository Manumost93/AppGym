package com.appgym.booking.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record ScheduleSlotRequest(
        @NotNull UUID activityId,
        @NotNull Instant startTime
) {
}
