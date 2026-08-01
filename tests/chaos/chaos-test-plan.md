# Purpose: Defines chaos engineering tests for platform resilience.

# Why it exists: Validates graceful degradation, failover, and recovery behavior before production launch.

# Architecture fit: Supports Milestone 11 chaos tests, HA, and disaster recovery validation.

# Chaos Test Plan

## Experiments

- Terminate one core backend pod during load.
- Restart Redis primary during cache-heavy traffic.
- Introduce Kafka broker unavailability for event consumers.
- Add 500 ms latency between backend and PostgreSQL.
- Force Qdrant unavailability and verify RAG fallback behavior.

## Evidence

- Start and end time:
- Blast radius:
- User-visible impact:
- Alerts triggered:
- Recovery time:
- Follow-up actions:
