package com.appgym.booking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "schedule_slots")
public class ScheduleSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "activity_id", nullable = false)
    private UUID activityId;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    /** Copiado del Activity al crear el slot: los cambios futuros de capacidad
     *  del Activity no alteran retroactivamente slots ya publicados. */
    @Column(nullable = false)
    private int capacity;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ScheduleSlot() {
    }

    public ScheduleSlot(UUID activityId, UUID businessId, Instant startTime, Instant endTime, int capacity) {
        this.activityId = activityId;
        this.businessId = businessId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.capacity = capacity;
    }

    public UUID getId() {
        return id;
    }

    public UUID getActivityId() {
        return activityId;
    }

    public UUID getBusinessId() {
        return businessId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public int getCapacity() {
        return capacity;
    }
}
