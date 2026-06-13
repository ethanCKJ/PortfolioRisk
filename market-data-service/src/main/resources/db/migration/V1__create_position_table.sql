CREATE TABLE position
(
    id            BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    portfolio_id  VARCHAR(64)    NOT NULL,
    quantity      NUMERIC(18, 8) NOT NULL,
    instrument_id VARCHAR(32)    NOT NULL,
    asset_class   VARCHAR(32)    NOT NULL,
    created_at    timestamptz    NOT NULL default now()
)
