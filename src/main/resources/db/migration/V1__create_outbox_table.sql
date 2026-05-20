CREATE TABLE IF NOT EXISTS outbox_events (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type  VARCHAR(100)    NOT NULL,
    aggregate_id    UUID            NOT NULL,
    event_type      VARCHAR(100)    NOT NULL,
    payload         TEXT            NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT now(),
    processed_at    TIMESTAMP WITH TIME ZONE,           -- ← Esta columna faltaba
    published       BOOLEAN         NOT NULL DEFAULT FALSE,
    published_at    TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_outbox_unprocessed 
    ON outbox_events(processed_at) 
    WHERE processed_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_outbox_unpublished 
    ON outbox_events(published) 
    WHERE published = FALSE;