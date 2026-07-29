# DR Failover Test Plan

> **Document Version:** 1.0  
> **Status:** Approved  
> **Owner:** Infrastructure & Quality Assurance Team  
> **Project:** AI Rural Root Cause Discovery System  
> **Document Type:** Operational Test Plan  

---

# Disaster Recovery Failover Test Plan

---

# Document Information

| Field | Value |
|---------|---------|
| Document Name | DR Failover Test Plan |
| Domain | Infrastructure Quality Assurance |
| Version | 1.0 |
| Status | Approved |
| Owner | Infrastructure QA Team |
| Created Date | 2026-07-28 |
| Last Updated | 2026-07-28 |

---

# Purpose

This document provides the operational execution procedure for conducting controlled disaster recovery failover simulations for the AI Rural Root Cause Discovery System. It validates that upon a total outage of the primary datacenter, traffic automatically or manually redirects to the secondary disaster recovery site without violating RTO and RPO SLA targets.

---

# Business Context

Government officials and field survey officers rely on continuous platform availability to record regional emergencies, water contamination reports, and crop failure events. Outages during critical survey collection windows disrupt policy response timelines and cause data loss.

---

# Test Topology & Architecture

```text
               +----------------------------------+
               |     Global Traffic Route 53      |
               +----------------+-----------------+
                                |
          +---------------------+---------------------+
          | (Primary Route)                           | (Failover Route)
          ▼                                           ▼
+-----------------------+                   +-----------------------+
|  Primary Datacenter   |                   |   DR Datacenter       |
|  (Region US-East-1)   |                   |   (Region US-West-2)  |
|                       |  Async Replic.    |                       |
| - K8s Primary Cluster |==================>| - K8s Standby Cluster |
| - Primary PostgreSQL  |                   | - Read-Replica DB     |
| - Primary MinIO S3    |                   | - Replicated MinIO S3 |
+-----------------------+                   +-----------------------+
```

---

# Test Execution Phases & Test Cases

### Phase 1: Pre-Execution Baseline Check
1. Verify database streaming replication lag is < 500ms between primary and secondary DB.
2. Confirm secondary Kubernetes cluster node capacity and secret sync status via External Secrets Operator.
3. Record current baseline synthetic user transaction latency.

---

### Phase 2: Induced Primary Failover Scenarios

#### TC-DR-001: Automated Database Master Node Failure Simulation
- **Trigger**: Issue `ip link set dev eth0 down` on Primary PostgreSQL Master node.
- **Expected Action**: Patroni / Stolon cluster orchestrator detects node failure within 10 seconds, promotes secondary standby node to read-write Master, and updates Consul DNS records.
- **Pass Criteria**: Database write availability restored in ≤ 25 seconds; zero uncommitted transaction loss.

#### TC-DR-002: Total Region Infrastructure Blackout Simulation
- **Trigger**: Simulate regional network isolation by updating Global Traffic Manager / DNS route policy to mark US-East-1 endpoint unhealthy.
- **Expected Action**: DNS routes 100% of incoming REST API traffic to secondary ingress gateway in US-West-2. Kubernetes HPA auto-scales pods to handle active workload.
- **Pass Criteria**: Full end-to-end service availability restored in ≤ 8 minutes (RTO target < 15 min); HTTP error rate drops back to < 0.01%.

---

### Phase 3: Post-Failover Verification & Fallback

#### TC-DR-003: Data Consistency Verification
- **Execution**: Compare primary and secondary database transaction sequence numbers (`LSN`) and verify row counts across `surveys`, `complaints`, and `recommendations` tables.
- **Pass Criteria**: 100% row count match across all active tables; zero corrupted records.

#### TC-DR-004: Primary Site Failback Sequence
- **Execution**: Re-establish primary network connectivity, synchronize missing write logs (`pg_rewind`), verify primary node health, and revert DNS routes back to primary.
- **Pass Criteria**: System gracefully transitions back to primary site with zero downtime using dual-write DNS buffering.

---

# Approval

| Role | Name | Date |
|------|------|------|
| Infrastructure QA Lead | Robert Sterling | 2026-07-28 |
| Lead DevOps Architect | Samantha Chen | 2026-07-28 |
| Solution Architect | Marcus Vance | 2026-07-28 |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | 2026-07-28 | Initial Release of DR Failover Test Plan | Infrastructure QA Team |

---

# End of Document
