CREATE TABLE activities (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id       UUID NOT NULL,
    type              VARCHAR(10) NOT NULL,
    name              VARCHAR(255) NOT NULL,
    description       TEXT,
    capacity          INTEGER NOT NULL,
    duration_minutes  INTEGER NOT NULL,
    instructor_name   VARCHAR(255),
    active            BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_activities_business_id ON activities (business_id);

CREATE TABLE schedule_slots (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    activity_id  UUID NOT NULL REFERENCES activities (id) ON DELETE CASCADE,
    business_id  UUID NOT NULL,
    start_time   TIMESTAMPTZ NOT NULL,
    end_time     TIMESTAMPTZ NOT NULL,
    capacity     INTEGER NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_schedule_slots_business_start ON schedule_slots (business_id, start_time);
CREATE INDEX idx_schedule_slots_activity_id ON schedule_slots (activity_id);

CREATE TABLE bookings (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slot_id      UUID NOT NULL REFERENCES schedule_slots (id) ON DELETE CASCADE,
    business_id  UUID NOT NULL,
    member_id    UUID NOT NULL,
    status       VARCHAR(20) NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    cancelled_at TIMESTAMPTZ
);

CREATE INDEX idx_bookings_slot_id ON bookings (slot_id);
CREATE INDEX idx_bookings_member_id ON bookings (member_id);

-- Un miembro solo puede tener una reserva activa (confirmada o en lista de espera)
-- por franja horaria; puede cancelarla y volver a reservar despues.
CREATE UNIQUE INDEX uq_bookings_slot_member_active ON bookings (slot_id, member_id)
    WHERE status <> 'CANCELLED';
