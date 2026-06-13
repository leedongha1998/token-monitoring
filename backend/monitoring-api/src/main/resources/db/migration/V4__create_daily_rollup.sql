CREATE TABLE daily_rollup (
    id                  BIGSERIAL      NOT NULL PRIMARY KEY,
    project_id          BIGINT         NOT NULL REFERENCES project (id),
    date                DATE           NOT NULL,
    model               VARCHAR(100)   NOT NULL,
    total_input_tokens  BIGINT         NOT NULL DEFAULT 0,
    total_output_tokens BIGINT         NOT NULL DEFAULT 0,
    total_cost          NUMERIC(20, 8) NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ    NOT NULL,
    CONSTRAINT uq_daily_rollup_project_date_model UNIQUE (project_id, date, model)
);

CREATE INDEX idx_daily_rollup_project_date ON daily_rollup (project_id, date);
CREATE INDEX idx_daily_rollup_date ON daily_rollup (date);
