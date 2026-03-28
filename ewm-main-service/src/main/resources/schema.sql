DROP TABLE IF EXISTS compilation_events CASCADE;
DROP TABLE IF EXISTS participation_requests CASCADE;
DROP TABLE IF EXISTS events CASCADE;
DROP TABLE IF EXISTS compilations CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
	id BIGSERIAL PRIMARY KEY,
	email VARCHAR(254) NOT NULL UNIQUE,
	name VARCHAR(250) NOT NULL
);

CREATE TABLE categories (
	id BIGSERIAL PRIMARY KEY,
	name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE events (
	id BIGSERIAL PRIMARY KEY,
	annotation VARCHAR(2000) NOT NULL,
	description VARCHAR(7000) NOT NULL,
	event_date TIMESTAMP NOT NULL,
	created_on TIMESTAMP NOT NULL,
	published_on TIMESTAMP,
	lat DOUBLE PRECISION NOT NULL,
	lon DOUBLE PRECISION NOT NULL,
	paid BOOLEAN NOT NULL DEFAULT FALSE,
	participant_limit INTEGER NOT NULL DEFAULT 0,
	request_moderation BOOLEAN NOT NULL DEFAULT TRUE,
	state VARCHAR(20) NOT NULL,
	title VARCHAR(120) NOT NULL,
	category_id BIGINT NOT NULL REFERENCES categories (id),
	initiator_id BIGINT NOT NULL REFERENCES users (id)
);

CREATE INDEX idx_events_initiator ON events (initiator_id);
CREATE INDEX idx_events_category ON events (category_id);
CREATE INDEX idx_events_state ON events (state);
CREATE INDEX idx_events_event_date ON events (event_date);

CREATE TABLE compilations (
	id BIGSERIAL PRIMARY KEY,
	title VARCHAR(50) NOT NULL UNIQUE,
	pinned BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE compilation_events (
	compilation_id BIGINT NOT NULL REFERENCES compilations (id) ON DELETE CASCADE,
	event_id BIGINT NOT NULL REFERENCES events (id) ON DELETE CASCADE,
	position INTEGER NOT NULL,
	PRIMARY KEY (compilation_id, event_id)
);

CREATE TABLE participation_requests (
	id BIGSERIAL PRIMARY KEY,
	created TIMESTAMP NOT NULL,
	status VARCHAR(20) NOT NULL,
	event_id BIGINT NOT NULL REFERENCES events (id),
	requester_id BIGINT NOT NULL REFERENCES users (id),
	UNIQUE (event_id, requester_id)
);

CREATE INDEX idx_participation_event ON participation_requests (event_id);
CREATE INDEX idx_participation_requester ON participation_requests (requester_id);
