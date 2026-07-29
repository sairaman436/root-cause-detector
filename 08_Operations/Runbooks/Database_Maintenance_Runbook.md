# Database Maintenance Runbook

> **Document Version:** 1.0  
> **Status:** Approved  
> **Owner:** Database Administration & SRE Team  
> **Project:** AI Rural Root Cause Discovery System  
> **Document Type:** Database Maintenance Operational Runbook  

---

# Database Maintenance Runbook

---

# Document Information

| Field | Value |
|---------|---------|
| Runbook Name | Database Maintenance Runbook |
| System Component | PostgreSQL Database Cluster |
| Estimated Execution Time | 45 Minutes |
| Execution Cadence | Weekly (Sundays 02:00 UTC) |
| Access Requirements | PostgreSQL `postgres` Superuser Access |

---

# Purpose

This runbook provides step-by-step procedures for conducting scheduled weekly database maintenance, vacuuming dead tuples, reindexing bloated tables, monitoring transaction ID wraparound, and optimizing query execution plans.

---

# Operational Maintenance Checklist

### Step 1: Pre-Maintenance Disk & Load Check
Verify that PostgreSQL database host has ≥ 30% available disk space before starting vacuum operations.
```sql
SELECT pg_size_pretty(pg_database_size('csp_prod')) AS database_size;
```

### Step 2: Non-Blocking Vacuum Analyze Execution
Perform `VACUUM ANALYZE` on core transaction tables to remove dead tuples and refresh planner statistics.
```sql
VACUUM VERBOSE ANALYZE surveys;
VACUUM VERBOSE ANALYZE complaints;
VACUUM VERBOSE ANALYZE ai_recommendations;
```

### Step 3: Concurrent Index Rebuilding
Reindex heavily updated indexes concurrently without taking table locks.
```sql
REINDEX TABLE CONCURRENTLY surveys;
REINDEX TABLE CONCURRENTLY complaints;
```

### Step 4: Transaction ID Wraparound Check
Verify maximum age of database transactions to prevent wraparound shutdown.
```sql
SELECT datname, age(datfrozenxid) FROM pg_database WHERE datname = 'csp_prod';
```
> **Alert Condition**: If `age > 150,000,000`, schedule an aggressive `VACUUM FREEZE`.

---

# Approval

| Role | Name | Date |
|------|------|------|
| Database Administrator Lead | Alan Turing | 2026-07-28 |
| SRE Lead | Jonathan Vance | 2026-07-28 |
| Solution Architect | Marcus Vance | 2026-07-28 |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | 2026-07-28 | Initial Release of DB Maintenance Runbook | DB QA Team |

---

# End of Document
