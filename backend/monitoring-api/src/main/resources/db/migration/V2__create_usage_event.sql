CREATE TABLE usage_event (
    id              BIGSERIAL    NOT NULL,
    project_id      BIGINT       NOT NULL REFERENCES project (id),
    idempotency_key VARCHAR(255) NOT NULL,
    model           VARCHAR(100) NOT NULL,
    input_tokens    INT          NOT NULL DEFAULT 0,
    output_tokens   INT          NOT NULL DEFAULT 0,
    occurred_at     TIMESTAMPTZ  NOT NULL,
    raw_payload     JSONB,
    PRIMARY KEY (id, occurred_at)
) PARTITION BY RANGE (occurred_at);

CREATE TABLE usage_event_2026_06 PARTITION OF usage_event
    FOR VALUES FROM ('2026-06-01') TO ('2026-07-01');

CREATE TABLE usage_event_2026_07 PARTITION OF usage_event
    FOR VALUES FROM ('2026-07-01') TO ('2026-08-01');

CREATE TABLE usage_event_2026_08 PARTITION OF usage_event
    FOR VALUES FROM ('2026-08-01') TO ('2026-09-01');

CREATE INDEX idx_usage_event_idempotency_key ON usage_event (idempotency_key);
CREATE INDEX idx_usage_event_project_id ON usage_event (project_id);
CREATE INDEX idx_usage_event_occurred_at ON usage_event (occurred_at);
