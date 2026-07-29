# Prometheus Grafana Monitoring Guide

> **Document Version:** 1.0  
> **Status:** Approved  
> **Owner:** SRE & Monitoring Quality Engineering Team  
> **Project:** AI Rural Root Cause Discovery System  
> **Document Type:** Monitoring Architecture & Telemetry Guide  

---

# Prometheus Grafana Monitoring Guide

---

# Document Information

| Field | Value |
|---------|---------|
| Document Name | Prometheus Grafana Monitoring Guide |
| Domain | Site Reliability Engineering & Telemetry |
| Version | 1.0 |
| Status | Approved |
| Owner | SRE Team |
| Created Date | 2026-07-28 |
| Last Updated | 2026-07-28 |

---

# Purpose

This document details the telemetry metrics collection, Prometheus scrape target configurations, custom Exporters, and Grafana dashboard visualizations for monitoring the AI Rural Root Cause Discovery System.

---

# Core Metric Categories (Google SRE 4 Golden Signals)

1. **Latency**: API request duration tracked via `http_request_duration_seconds_bucket`.
2. **Traffic**: Request rate measured via `http_requests_total`.
3. **Errors**: Failed HTTP 5xx responses tracked via `http_requests_total{status=~"5.."}`.
4. **Saturation**: CPU, Memory, DB Connection Pool, and Queue Depth utilization.

---

# Key PromQL Monitoring Queries

### API Gateway 95th Percentile Latency (p95)
```promql
histogram_quantile(0.95, sum(rate(http_request_duration_seconds_bucket{job="csp-api-gateway"}[5m])) by (le))
```

### System Error Rate Percentage
```promql
(sum(rate(http_requests_total{status=~"5.."}[5m])) / sum(rate(http_requests_total[5m]))) * 100
```

### AI Model Inference Queue Saturation
```promql
rabbitmq_queue_messages{queue="ai-inference-queue"}
```

---

# Standard Grafana Dashboards

| Dashboard ID | Dashboard Name | Target Audience | Primary Visualizations |
|--------------|----------------|-----------------|------------------------|
| `dash-01` | Executive System Overview | CTO / Ministry Stakeholders | System Uptime, Total Surveys Today, Active Districts |
| `dash-02` | API Gateway Telemetry | SRE / DevOps Engineers | RPS, Latency Percentiles (p50/p90/p95/p99), HTTP Error Codes |
| `dash-03` | AI Model Performance | Data Scientists / MLOps | Inference Latency, Confidence Score Distribution, Queue Backlog |
| `dash-04` | PostgreSQL DB Cluster | DB Administrators | DB Connections, Replication Lag, Buffer Cache Hit Ratio |

---

# Approval

| Role | Name | Date |
|------|------|------|
| SRE Lead | Jonathan Vance | 2026-07-28 |
| Lead DevOps Architect | Samantha Chen | 2026-07-28 |
| Solution Architect | Marcus Vance | 2026-07-28 |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | 2026-07-28 | Initial Release of Prometheus Grafana Guide | SRE Team |

---

# End of Document
