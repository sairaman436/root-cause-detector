# Database_Design.md

> **Document Version:** 1.0
> **Status:** Draft
> **Owner:** Database Engineering Team
> **Project:** AI Rural Root Cause Discovery System
> **Last Updated:** 2026-07-28

---

# Database Design

---

# Document Information

| Field | Value |
|---------|---------|
| Project | AI Rural Root Cause Discovery System |
| Module | Database |
| Database | PostgreSQL |
| Version | 1.0 |
| Status | Draft |
| Owner | Database Engineering Team |

---

# Purpose

This document defines the logical and physical database design for the AI Rural Root Cause Discovery System.

It provides a blueprint for data storage, integrity, security, scalability, and performance.

---

# Objectives

The database shall:

- Maintain ACID compliance
- Ensure data consistency
- Support high availability
- Enable horizontal growth where applicable
- Optimize analytical and transactional workloads
- Protect sensitive data
- Maintain complete auditability

---

# Database Technology

| Component | Technology |
|-----------|------------|
| Relational Database | PostgreSQL |
| Cache | Redis |
| Object Storage | MinIO / Amazon S3 |
| Migration Tool | Flyway |
| Connection Pool | HikariCP |

---

# High-Level Architecture

```text
Frontend

↓

REST APIs

↓

Business Services

↓

JPA/Hibernate

↓

PostgreSQL

↓

Backups
```

---

# Database Modules

The database consists of the following logical domains:

- Identity & Access Management
- Geographic Information
- Survey Management
- Root Cause Analysis
- AI Predictions
- Recommendations
- Analytics
- Notifications
- Audit Logs
- System Configuration

---

# Entity Overview

Core entities:

- User
- Role
- Permission
- Village
- District
- State
- Survey
- SurveyResponse
- RootCause
- Prediction
- Recommendation
- Report
- Notification
- AuditLog
- ModelVersion

---

# Entity Relationship Overview

```text
User
 │
 ├── Role
 │
 ├── Survey
 │      │
 │      ├── SurveyResponse
 │      │
 │      ├── Prediction
 │      │      │
 │      │      └── RootCause
 │      │
 │      └── Recommendation
 │
 └── AuditLog
```

---

# Schema Organization

Schemas:

```text
public

identity

survey

analytics

ai

audit

configuration
```

---

# Table Standards

Every table should include:

- Primary Key
- Created Timestamp
- Updated Timestamp
- Created By
- Updated By
- Version (Optimistic Locking)
- Active Flag (Soft Delete)

Example:

```sql
id UUID PRIMARY KEY

created_at TIMESTAMP

updated_at TIMESTAMP

created_by UUID

updated_by UUID

version INTEGER

is_active BOOLEAN
```

---

# Primary Key Strategy

Use UUID Version 7 (or UUID Version 4 if Version 7 is unavailable).

Advantages:

- Globally unique
- Distributed-friendly
- Reduced collision risk

---

# Foreign Key Strategy

All relationships shall enforce referential integrity.

Example:

```text
Survey

↓

Village

↓

District

↓

State
```

---

# Normalization

Target:

- Third Normal Form (3NF)

Exceptions:

- Controlled denormalization for reporting
- Materialized views for analytics

---

# Indexing Strategy

Indexes should be created for:

- Primary Keys
- Foreign Keys
- Frequently filtered columns
- Search fields
- Timestamp columns
- Composite query patterns

Example:

```sql
CREATE INDEX idx_survey_village
ON survey(village_id);

CREATE INDEX idx_prediction_status
ON prediction(status);
```

---

# Partitioning Strategy

Partition large tables by:

- Survey Date
- District
- State
- Year

Benefits:

- Improved query performance
- Easier archival
- Faster maintenance

---

# Constraints

Use:

- NOT NULL
- CHECK
- UNIQUE
- FOREIGN KEY
- DEFAULT

Example:

```sql
CHECK (confidence_score BETWEEN 0 AND 100)
```

---

# Transactions

Support:

- ACID transactions
- Rollback on failure
- Savepoints
- Optimistic locking

---

# Data Integrity

Ensure:

- Referential integrity
- Business rule validation
- Duplicate prevention
- Consistent timestamps

---

# Caching Strategy

Cache:

- Villages
- Districts
- Configuration
- Frequently accessed reports
- AI model metadata

Technology:

- Redis

---

# Database Security

Authentication:

- Strong credentials
- Managed secrets

Authorization:

- Least privilege
- Role-based access

Encryption:

- TLS in transit
- Encryption at rest

---

# Sensitive Data

Sensitive fields:

- Email
- Phone Number
- Government Identifiers
- Authentication Data

Controls:

- Encryption
- Masking
- Restricted access
- Audit logging

---

# Backup Strategy

Frequency:

- Daily full backup
- Hourly incremental backup

Retention:

- Daily: 30 days
- Weekly: 12 weeks
- Monthly: 12 months

Storage:

- Offsite encrypted storage

---

# Disaster Recovery

Recovery objectives:

| Metric | Target |
|---------|---------|
| RPO | ≤ 15 minutes |
| RTO | ≤ 1 hour |

Procedures:

- Point-in-time recovery
- Automated backup verification
- Failover testing

---

# Performance Optimization

Strategies:

- Query optimization
- Proper indexing
- Connection pooling
- Batch operations
- Read replicas
- Materialized views

---

# Data Lifecycle

Stages:

```text
Create

↓

Validate

↓

Store

↓

Analyze

↓

Archive

↓

Delete (Retention Policy)
```

---

# Archival Strategy

Archive:

- Old surveys
- Historical reports
- AI prediction history
- Audit logs

Archive storage:

- Object Storage
- Cold Storage

---

# Audit Logging

Track:

- INSERT
- UPDATE
- DELETE
- Login Events
- Configuration Changes
- Permission Changes

---

# Monitoring

Monitor:

- CPU usage
- Memory usage
- Slow queries
- Deadlocks
- Index usage
- Connection count
- Replication lag
- Disk utilization

---

# Database Maintenance

Scheduled tasks:

- VACUUM
- ANALYZE
- REINDEX
- Statistics update
- Partition management

---

# Migration Strategy

Tool:

Flyway

Rules:

- Version-controlled migrations
- Rollback scripts
- Peer review
- Automated validation

---

# Risks

| Risk | Mitigation |
|------|------------|
| Slow queries | Index optimization |
| Database growth | Partitioning |
| Data corruption | Automated backups |
| Connection exhaustion | Connection pooling |
| Replication failure | Monitoring and failover |

---

# Future Enhancements

- Multi-region replication
- Read/write splitting
- Time-series storage for analytics
- Graph database integration
- Vector database for AI embeddings
- Data lake integration

---

# Traceability

| Requirement | Database Component |
|-------------|--------------------|
| FR-001 | Survey Tables |
| FR-002 | Prediction Tables |
| FR-003 | Recommendation Tables |
| NFR-001 | Backup Strategy |
| NFR-002 | Replication |

---

# References

- System Overview
- Backend Design
- API Design
- Database Design Template
- Caching Strategy
- Performance Design
- ADRs

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | 2026-07-28 | Initial Version | |