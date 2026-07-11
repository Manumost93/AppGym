ALTER TABLE users
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN paid    BOOLEAN     NOT NULL DEFAULT FALSE;

CREATE INDEX idx_users_business_id_status ON users (business_id, status);
