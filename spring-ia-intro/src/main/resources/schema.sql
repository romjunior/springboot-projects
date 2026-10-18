CREATE TABLE IF NOT EXISTS helpdesk_tickets (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    issue TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    eta TIMESTAMP NOT NULL
);
