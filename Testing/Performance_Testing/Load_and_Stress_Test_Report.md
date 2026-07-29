# Load and Stress Test Report

> **Document Version:** 1.0  
> **Status:** Approved  
> **Owner:** Performance Engineering Team  
> **Project:** AI Rural Root Cause Discovery System  
> **Document Type:** Test Execution & Performance Benchmark Report  

---

# Load and Stress Test Report

---

# Document Information

| Field | Value |
|---------|---------|
| Document Name | Load and Stress Test Report |
| Domain | Performance Quality Assurance |
| Version | 1.0 |
| Status | Approved |
| Owner | Performance QA Team |
| Created Date | 2026-07-28 |
| Last Updated | 2026-07-28 |

---

# Purpose

This report documents the load, stress, spike, and endurance performance test execution conducted on the AI Rural Root Cause Discovery System using k6 and Apache JMeter. It measures response times, throughput, resource consumption, and breaking points under simulated multi-district survey submission loads.

---

# Test Environment Configuration

- **Target Environment**: Performance Staging Cluster (`k8s-perf-cluster-01`)
- **Node Configuration**: 8 Kubernetes Worker Nodes (16 vCPU, 64 GB RAM each)
- **Database Instance**: PostgreSQL 15 Primary + 2 Read Replicas (AWS db.r6g.4xlarge)
- **Load Generation**: 4 Distributed k6 Agents simulating up to 10,000 Concurrent Virtual Users (VUs)

---

# Load Test Execution Results (Target SLA vs. Actual)

### Scenario 1: Normal Peak Load (3,000 Virtual Users)
Simulates standard daytime field operations across 50 rural districts.

| Endpoint / API Operation | Target SLA (p95) | Actual Latency (p95) | Actual Throughput (RPS) | Error Rate | SLA Status |
|--------------------------|------------------|----------------------|-------------------------|------------|------------|
| `POST /api/v1/auth/login` | ≤ 150 ms | 68 ms | 850 RPS | 0.00% | ✅ PASS |
| `POST /api/v1/surveys` | ≤ 200 ms | 112 ms | 1,420 RPS | 0.00% | ✅ PASS |
| `POST /api/v1/ai/root-cause` | ≤ 350 ms | 215 ms | 620 RPS | 0.00% | ✅ PASS |
| `GET /api/v1/reports/district` | ≤ 250 ms | 145 ms | 310 RPS | 0.00% | ✅ PASS |

---

### Scenario 2: Stress Test & Breaking Point Profiling (Up to 10,000 Virtual Users)
Ramps load continuously until resource exhaustion or SLA breach to identify system breaking points.

```text
Virtual Users
10,000 |                                                 /------\
 8,000 |                                       /--------/        \
 6,000 |                             /--------/                   \
 4,000 |                   /--------/                              \
 2,000 |         /--------/                                         \
     0 +---------+--------+---------+---------+---------+-----------+
       0m        10m      20m       30m       40m       50m         60m
```

- **SLA Breach Point**: Occurred at **7,850 Concurrent Virtual Users** (API throughput reached 4,820 RPS).
- **Bottleneck Identified**: PostgreSQL DB connection pool exhaustion (Max pool size 200 connections reached).
- **Recovery Outcome**: Pod HPA successfully scaled API pods from 10 to 32 instances; connection pool auto-recovered within 45 seconds after load ramp-down.

---

### Scenario 3: 24-Hour Endurance Test
Maintained steady load of 2,000 Virtual Users for 24 continuous hours.
- **Memory Leak Check**: Pod memory usage stabilized at 4.2 GB / 8.0 GB per replica. Zero memory leaks detected.
- **Total Transactions Executed**: 142,500,000 HTTP requests.
- **Overall Error Rate**: 0.002% (All transient network socket timeouts).

---

# Recommendations & Tuning Actions

1. **Database Connection Pool Optimization**: Increase PgBouncer max client connections from 2,000 to 5,000 and configure statement timeout to 5,000ms.
2. **Redis Cache TTL Tuning**: Extend TTL for static district metadata lookup endpoints from 15 minutes to 2 hours.
3. **AI Inference Batching**: Enable dynamic batching (`batch_size = 32`) on Python inference pods to optimize GPU utilization.

---

# Approval

| Role | Name | Date |
|------|------|------|
| Performance QA Lead | Jonathan Vance | 2026-07-28 |
| Lead DevOps Architect | Samantha Chen | 2026-07-28 |
| Solution Architect | Marcus Vance | 2026-07-28 |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | 2026-07-28 | Initial Release of Load and Stress Test Report | Performance QA Team |

---

# End of Document
