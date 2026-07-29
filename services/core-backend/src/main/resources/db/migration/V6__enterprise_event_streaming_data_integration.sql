-- Purpose: Creates the Enterprise Event Streaming and Data Integration schema for Milestone 7.
-- Why it exists: The platform needs a reliable Kafka-backed event backbone, transactional outbox, replay, retry, dead-letter, subscription, and audit visibility.
-- Architecture fit: Adds event infrastructure without implementing AI, LLM, RAG, multi-agent logic, analytics products, or notification providers.

CREATE SCHEMA IF NOT EXISTS eventing;

CREATE TABLE eventing.outbox_events (
    id UUID PRIMARY KEY,
    event_type VARCHAR(120) NOT NULL,
    schema_version INTEGER NOT NULL,
    topic VARCHAR(180) NOT NULL,
    aggregate_type VARCHAR(80) NOT NULL,
    aggregate_id UUID NOT NULL,
    organization_id UUID,
    actor_user_id UUID,
    producer_service VARCHAR(120) NOT NULL DEFAULT 'core-backend',
    correlation_id VARCHAR(120) NOT NULL,
    trace_id VARCHAR(120),
    payload_json TEXT NOT NULL,
    metadata_json TEXT,
    status VARCHAR(40) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_error VARCHAR(1000),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uq_outbox_event_id UNIQUE (id)
);

CREATE TABLE eventing.event_log (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL UNIQUE,
    event_type VARCHAR(120) NOT NULL,
    topic VARCHAR(180) NOT NULL,
    aggregate_type VARCHAR(80) NOT NULL,
    aggregate_id UUID NOT NULL,
    organization_id UUID,
    actor_user_id UUID,
    producer_service VARCHAR(120) NOT NULL,
    correlation_id VARCHAR(120) NOT NULL,
    trace_id VARCHAR(120),
    schema_version INTEGER NOT NULL,
    payload_json TEXT NOT NULL,
    metadata_json TEXT,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    logged_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE eventing.dead_letter_events (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    event_type VARCHAR(120) NOT NULL,
    topic VARCHAR(180) NOT NULL,
    aggregate_type VARCHAR(80) NOT NULL,
    aggregate_id UUID NOT NULL,
    payload_json TEXT NOT NULL,
    error_message VARCHAR(1000) NOT NULL,
    attempts INTEGER NOT NULL,
    failed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    replayed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE eventing.event_retry (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    attempt_number INTEGER NOT NULL,
    status VARCHAR(40) NOT NULL,
    error_message VARCHAR(1000),
    scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE eventing.event_subscription (
    id UUID PRIMARY KEY,
    consumer_name VARCHAR(120) NOT NULL,
    topic VARCHAR(180) NOT NULL,
    status VARCHAR(40) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_event_subscription UNIQUE (consumer_name, topic)
);

CREATE TABLE eventing.event_consumer_offset (
    id UUID PRIMARY KEY,
    consumer_group VARCHAR(180) NOT NULL,
    topic VARCHAR(180) NOT NULL,
    partition_id INTEGER NOT NULL,
    offset_value BIGINT NOT NULL,
    lag BIGINT,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE eventing.event_processing_log (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    consumer_name VARCHAR(120) NOT NULL,
    topic VARCHAR(180) NOT NULL,
    partition_id INTEGER,
    offset_value BIGINT,
    status VARCHAR(40) NOT NULL,
    attempts INTEGER NOT NULL,
    latency_ms BIGINT,
    error_message VARCHAR(1000),
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_event_processing_consumer UNIQUE (event_id, consumer_name)
);

CREATE TABLE eventing.analytics_event_records (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL UNIQUE,
    event_type VARCHAR(120) NOT NULL,
    topic VARCHAR(180) NOT NULL,
    aggregate_type VARCHAR(80) NOT NULL,
    aggregate_id UUID NOT NULL,
    organization_id UUID,
    actor_user_id UUID,
    schema_version INTEGER NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ingested_at TIMESTAMP WITH TIME ZONE NOT NULL,
    payload_json TEXT NOT NULL
);

CREATE INDEX idx_outbox_status_due ON eventing.outbox_events (status, next_attempt_at, created_at);
CREATE INDEX idx_outbox_topic_created ON eventing.outbox_events (topic, created_at);
CREATE INDEX idx_event_log_topic_time ON eventing.event_log (topic, occurred_at);
CREATE INDEX idx_event_log_aggregate ON eventing.event_log (aggregate_type, aggregate_id);
CREATE INDEX idx_dead_letter_topic_time ON eventing.dead_letter_events (topic, failed_at);
CREATE INDEX idx_retry_event ON eventing.event_retry (event_id, attempt_number);
CREATE INDEX idx_subscription_topic ON eventing.event_subscription (topic);
CREATE INDEX idx_consumer_offset_group_topic ON eventing.event_consumer_offset (consumer_group, topic, partition_id);
CREATE INDEX idx_processing_consumer ON eventing.event_processing_log (consumer_name, processed_at);
CREATE INDEX idx_analytics_event_type_time ON eventing.analytics_event_records (event_type, occurred_at);

INSERT INTO identity.permissions (id, name, resource, action, description, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000000701', 'EVENT_ADMIN', 'EVENTS', 'ADMIN', 'Administer event streaming, replay, retry, and dead-letter operations', NOW(), NOW()),
('00000000-0000-0000-0000-000000000702', 'EVENT_VIEWER', 'EVENTS', 'VIEW', 'View event logs, outbox state, subscriptions, and dead-letter records', NOW(), NOW()),
('00000000-0000-0000-0000-000000000703', 'AUDIT_VIEWER', 'EVENT_AUDIT', 'VIEW', 'View event-driven audit records', NOW(), NOW());

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000201', id
FROM identity.permissions
WHERE name IN ('EVENT_ADMIN', 'EVENT_VIEWER', 'AUDIT_VIEWER');

INSERT INTO identity.role_permissions (role_id, permission_id)
VALUES
('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000702'),
('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000703');

INSERT INTO eventing.event_subscription (id, consumer_name, topic, status, description, created_at, updated_at)
VALUES
('00000000-0000-0000-0000-000000000711', 'audit-service', 'audit.created', 'ACTIVE', 'Current audit event consumer', NOW(), NOW()),
('00000000-0000-0000-0000-000000000712', 'notification-service', 'notification.created', 'PLACEHOLDER', 'Future notification dispatch consumer', NOW(), NOW()),
('00000000-0000-0000-0000-000000000713', 'analytics-service', 'survey.completed', 'PLACEHOLDER', 'Future analytics ingestion consumer', NOW(), NOW()),
('00000000-0000-0000-0000-000000000714', 'ai-service', 'evidence.validated', 'PLACEHOLDER', 'Future AI pipeline consumer', NOW(), NOW()),
('00000000-0000-0000-0000-000000000715', 'search-index-service', 'knowledge.document.updated', 'PLACEHOLDER', 'Future search indexing consumer', NOW(), NOW()),
('00000000-0000-0000-0000-000000000716', 'workflow-service', 'survey.completed', 'PLACEHOLDER', 'Future workflow orchestration consumer', NOW(), NOW());
