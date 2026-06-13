CREATE TABLE model_pricing (
    id                       BIGSERIAL      NOT NULL PRIMARY KEY,
    model                    VARCHAR(100)   NOT NULL,
    input_price_per_m_token  NUMERIC(10, 6) NOT NULL,
    output_price_per_m_token NUMERIC(10, 6) NOT NULL,
    effective_from           TIMESTAMPTZ    NOT NULL
);

CREATE INDEX idx_model_pricing_model_effective ON model_pricing (model, effective_from DESC);
