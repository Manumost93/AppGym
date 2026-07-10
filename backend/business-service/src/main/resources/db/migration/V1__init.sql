CREATE TABLE businesses (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name           VARCHAR(255) NOT NULL,
    type           VARCHAR(30)  NOT NULL,
    description    TEXT,
    contact_email  VARCHAR(255),
    contact_phone  VARCHAR(50),
    address        VARCHAR(255),
    primary_color  VARCHAR(20),
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE membership_plans (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id   UUID NOT NULL REFERENCES businesses (id) ON DELETE CASCADE,
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    price_cents   INTEGER NOT NULL,
    currency      VARCHAR(3) NOT NULL DEFAULT 'EUR',
    duration_days INTEGER NOT NULL,
    active        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_membership_plans_business_id ON membership_plans (business_id);
