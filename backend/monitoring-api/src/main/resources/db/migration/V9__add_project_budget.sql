CREATE TABLE project_budget (
    id                  BIGSERIAL       NOT NULL PRIMARY KEY,
    project_id          BIGINT          NOT NULL REFERENCES project (id),
    year_month          VARCHAR(7)      NOT NULL,
    monthly_budget_usd  NUMERIC(12, 4)  NOT NULL,
    created_at          TIMESTAMPTZ     NOT NULL,
    updated_at          TIMESTAMPTZ     NOT NULL,
    CONSTRAINT uq_project_budget_project_yearmonth UNIQUE (project_id, year_month)
);

CREATE INDEX idx_project_budget_project_id ON project_budget (project_id);
