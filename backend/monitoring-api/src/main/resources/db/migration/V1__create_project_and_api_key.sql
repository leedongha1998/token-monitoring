CREATE TABLE project (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE api_key (
    id          BIGSERIAL   PRIMARY KEY,
    project_id  BIGINT      NOT NULL REFERENCES project (id),
    key_hash    VARCHAR(64) NOT NULL UNIQUE,
    prefix      VARCHAR(8)  NOT NULL,
    is_active   BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_api_key_key_hash ON api_key (key_hash);
CREATE INDEX idx_api_key_project_id ON api_key (project_id);
