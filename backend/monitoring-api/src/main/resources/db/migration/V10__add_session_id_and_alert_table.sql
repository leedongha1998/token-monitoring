ALTER TABLE usage_event ADD COLUMN session_id VARCHAR(255);

CREATE TABLE alert (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    triggered_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    metadata JSONB
);

CREATE INDEX idx_alert_project_id ON alert(project_id);
CREATE INDEX idx_alert_project_read ON alert(project_id, is_read);
