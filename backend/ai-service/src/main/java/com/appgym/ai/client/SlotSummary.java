package com.appgym.ai.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SlotSummary(
        String activityName,
        Instant startTime,
        int capacity,
        long confirmedCount,
        long waitlistCount,
        boolean full
) {
}
