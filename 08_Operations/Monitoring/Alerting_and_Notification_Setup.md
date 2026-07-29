# Alerting and Notification Setup

> **Document Version:** 1.0  
> **Status:** Approved  
> **Owner:** SRE & Alert Governance Team  
> **Project:** AI Rural Root Cause Discovery System  
> **Document Type:** Prometheus Alertmanager & PagerDuty Specification  

---

# Alerting and Notification Setup

---

# Document Information

| Field | Value |
|---------|---------|
| Document Name | Alerting and Notification Setup |
| Domain | Site Reliability Engineering & Alerting |
| Version | 1.0 |
| Status | Approved |
| Owner | SRE Team |
| Created Date | 2026-07-28 |
| Last Updated | 2026-07-28 |

---

# Purpose

This document specifies Prometheus Alertmanager rule definitions, escalation policies, PagerDuty routing integrations, and notification channels for operational alerts within the AI Rural Root Cause Discovery System.

---

# Core Prometheus Alert Rules

```yaml
groups:
  - name: csp-critical-alerts
    rules:
      - alert: HighApiErrorRate
        expr: (sum(rate(http_requests_total{status=~"5.."}[5m])) / sum(rate(http_requests_total[5m]))) * 100 > 1.0
        for: 3m
        labels:
          severity: critical
        annotations:
          summary: "API Gateway Error Rate Exceeds 1.0%"
          description: "Production API 5xx HTTP error rate is {{ $value }}% over the last 3 minutes."

      - alert: DatabaseReplicationLag
        expr: pg_stat_replication_bytes_lag > 104857600
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "PostgreSQL Replica Replication Lag High"
          description: "Database replica replication lag exceeded 100 MB."

      - alert: AiInferenceQueueBacklog
        expr: rabbitmq_queue_messages{queue="ai-inference-queue"} > 1000
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "AI Inference Queue Backlog High"
          description: "AI queue depth has exceeded 1,000 un-processed survey records."
```

---

# Alert Escalation & Notification Routing Matrix

| Alert Severity | Primary Channel | Secondary Channel | Escalation Target | SLA Acknowledgment |
|----------------|-----------------|-------------------|-------------------|--------------------|
| **Critical (Sev-1)** | PagerDuty Phone Call / SMS | Slack `#ops-critical-alerts` | On-Call Lead → SRE Manager | ≤ 15 Minutes |
| **Warning (Sev-2)** | PagerDuty Mobile App Push | Slack `#ops-warning-alerts` | On-Call Engineer | ≤ 30 Minutes |
| **Info (Sev-3)** | Slack `#ops-info-logs` | Email Summary Report | Duty Operations Engineer | Next Business Day |

---

# Approval

| Role | Name | Date |
|------|------|------|
| SRE Lead | Jonathan Vance | 2026-07-28 |
| Operations Director | Helen Brody | 2026-07-28 |
| Solution Architect | Marcus Vance | 2026-07-28 |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | 2026-07-28 | Initial Release of Alerting & Notification Setup | SRE Team |

---

# End of Document
