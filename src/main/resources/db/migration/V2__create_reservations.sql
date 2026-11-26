CREATE TABLE reservation (
    id BIGSERIAL PRIMARY KEY,
    charger_id BIGINT NOT NULL REFERENCES charger (id),
    user_name VARCHAR(100) NOT NULL,
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT reservation_ends_after_start CHECK (ends_at > starts_at)
);

CREATE INDEX reservation_charger_id_idx ON reservation (charger_id);
