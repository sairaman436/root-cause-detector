# Purpose: Defines incident runbooks for production operations.

# Why it exists: Reduces response ambiguity during outages, degradation, drift, and security events.

# Architecture fit: Supports Milestone 11 SRE, alerting, disaster recovery, and incident response.

# Runbooks

## Core Backend Down

1. Check Kubernetes Deployment rollout and pod readiness.
2. Inspect `/actuator/health/liveness` and container logs in Loki.
3. Validate database, Redis, Kafka, and Qdrant connectivity.
4. Roll back to the previous signed image if failure started after deployment.

## High Error Rate

1. Use Grafana platform overview to identify endpoint and status class.
2. Correlate traces in Tempo by request path and deployment version.
3. Check recent database migrations and Kafka lag.
4. Scale replicas or roll back if errors correlate with current release.

## Decision Confidence Drop

1. Inspect decision confidence distribution and rule violation metrics.
2. Compare prompt, model, embedding, and rule versions before and after the drop.
3. Route consequential recommendations to human review.
4. Freeze model or prompt promotion until evaluation passes.

## Restore Validation Failed

1. Preserve failed restore logs and backup identifiers.
2. Attempt restore from prior recovery point.
3. Escalate to database engineering and SRE.
4. Update `operations.backup_reports` with failure evidence.
