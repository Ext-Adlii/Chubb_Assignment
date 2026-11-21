-- =============================================================
-- V1: Initial schema for Chubb Claims Platform
-- =============================================================

-- ----- claimants -----
CREATE TABLE claimants (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    external_id   VARCHAR(64)  NOT NULL UNIQUE,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255),
    phone         VARCHAR(50),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ----- officers -----
CREATE TABLE officers (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id   VARCHAR(64)  NOT NULL UNIQUE,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ----- claims -----
-- claimant_id and assigned_officer_id are VARCHAR to match the Claim JPA entity (String fields).
-- Referential integrity to claimants/officers is enforced at the application layer.
CREATE TABLE claims (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    claim_number        VARCHAR(32)    NOT NULL UNIQUE,
    type                VARCHAR(32)    NOT NULL,
    market              VARCHAR(64)    NOT NULL,
    claimant_id         VARCHAR(255)   NOT NULL,
    policy_number       VARCHAR(64)    NOT NULL,
    description         TEXT,
    incident_date       DATE           NOT NULL,
    status              VARCHAR(32)    NOT NULL,
    assigned_officer_id VARCHAR(255),
    reserve_amount      NUMERIC(15,2),
    version             BIGINT         NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ    NOT NULL DEFAULT now()
);

-- ----- info_requests -----
CREATE TABLE info_requests (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    claim_id        UUID         NOT NULL REFERENCES claims(id),
    requested_by    VARCHAR(255) NOT NULL,
    question        TEXT         NOT NULL,
    response        TEXT,
    requested_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    responded_at    TIMESTAMPTZ
);

-- ----- status_history -----
CREATE TABLE status_history (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    claim_id     UUID         NOT NULL REFERENCES claims(id),
    from_status  VARCHAR(32),
    to_status    VARCHAR(32)  NOT NULL,
    changed_by   VARCHAR(255),
    changed_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ----- decisions -----
CREATE TABLE decisions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    claim_id    UUID         NOT NULL REFERENCES claims(id),
    decision    VARCHAR(32)  NOT NULL,
    decided_by  VARCHAR(255) NOT NULL,
    rationale   TEXT,
    amount      NUMERIC(15,2),
    decided_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ----- idempotency_keys -----
CREATE TABLE idempotency_keys (
    key         VARCHAR(255) PRIMARY KEY,
    claim_id    UUID         NOT NULL REFERENCES claims(id),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ----- outbox_events -----
CREATE TABLE outbox_events (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_id  UUID         NOT NULL,
    type          VARCHAR(128) NOT NULL,
    payload       JSONB        NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    sent_at       TIMESTAMPTZ
);

-- =============================================================
-- Indexes
-- =============================================================

-- Queue query: filter by status, then by assigned officer
CREATE INDEX idx_claims_status_officer ON claims (status, assigned_officer_id);

-- Claimant lookup
CREATE INDEX idx_claims_claimant_id ON claims (claimant_id);

-- Outbox polling: unsent events ordered by creation time
CREATE INDEX idx_outbox_unsent ON outbox_events (created_at) WHERE sent_at IS NULL;
