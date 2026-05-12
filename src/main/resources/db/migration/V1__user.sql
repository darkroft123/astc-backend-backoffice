-- =========================
-- TABLES
-- =========================

CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS projects (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    full_name VARCHAR(200),
    email VARCHAR(150) NOT NULL,
    password_hash TEXT NOT NULL,
    phone VARCHAR(20),
    role_id UUID REFERENCES roles(id),
    project_id UUID REFERENCES projects(id),
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT NOW()
);

-- =========================
-- OUTBOX
-- =========================

CREATE TABLE IF NOT EXISTS outbox_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    processed_at TIMESTAMP WITH TIME ZONE,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    published_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_outbox_unprocessed
ON outbox_events(processed_at)
WHERE processed_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_outbox_unpublished
ON outbox_events(published)
WHERE published = FALSE;

-- =========================
-- SEED DATA
-- =========================

INSERT INTO roles (id, code, name)
VALUES
('018f6b7a-0001-7000-8000-000000000001', 'ADMIN', 'Administrator'),
('018f6b7a-0001-7000-8000-000000000002', 'PROJECT_MANAGER', 'Project Manager'),
('018f6b7a-0001-7000-8000-000000000003', 'TEAM_MEMBER', 'Team Member');

INSERT INTO projects (id, code, name, status)
VALUES
('018f6b7a-0002-7000-8000-000000000001', 'PROJ-001', 'Main Platform', 'ACTIVE'),
('018f6b7a-0002-7000-8000-000000000002', 'PROJ-002', 'Mobile App', 'ACTIVE');

INSERT INTO users (
    id,
    username,
    first_name,
    last_name,
    full_name,
    email,
    password_hash,
    phone,
    role_id,
    project_id,
    status,
    created_at
)
VALUES (
    '018f6b7a-0003-7000-8000-000000000001',
    'admin',
    'System',
    'Admin',
    'System Admin',
    'admin@backoffice.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    '+1234567890',
    '018f6b7a-0001-7000-8000-000000000001',
    '018f6b7a-0002-7000-8000-000000000001',
    'ACTIVE',
    NOW()
);