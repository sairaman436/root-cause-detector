# Backup and Restore Operational Guide

> **Document Version:** 1.0  
> **Status:** Approved  
> **Owner:** Database Administration & Storage Engineering Team  
> **Project:** AI Rural Root Cause Discovery System  
> **Document Type:** Operational Procedure Guide  

---

# Backup and Restore Operational Guide

---

# Document Information

| Field | Value |
|---------|---------|
| Document Name | Backup and Restore Operational Guide |
| Domain | Database & Storage Operations |
| Version | 1.0 |
| Status | Approved |
| Owner | DB Operations Team |
| Created Date | 2026-07-28 |
| Last Updated | 2026-07-28 |

---

# Purpose

This operational guide provides step-by-step procedures for managing automated database backup jobs, manual snapshot generation, WAL archiving, object storage replication, and point-in-time restoration for the AI Rural Root Cause Discovery System.

---

# Backup Execution Architecture & Schedule

```text
Full Nightly DB Backup (02:00 UTC) -> Encrypted S3 Archive (`s3://csp-backups/pg_full/`)
Continuous WAL Archiving (Every 16 MB) -> Encrypted S3 Archive (`s3://csp-backups/pg_wal/`)
Hourly MinIO S3 Object Snapshot -> Multi-Region S3 Replication
```

---

# Manual Backup Generation Procedure

To generate an immediate manual backup prior to emergency hotfix deployments:

```bash
# 1. Execute Encrypted Database Backup via pg_dump
pg_dump -h db.csp.gov.in -U postgres -F c -b -v -f /tmp/csp_pre_hotfix.dump csp_prod

# 2. Encrypt Backup with KMS Key
gpg --symmetric --cipher-algo AES256 /tmp/csp_pre_hotfix.dump

# 3. Upload to Encrypted Production Backup Bucket
aws s3 cp /tmp/csp_pre_hotfix.dump.gpg s3://csp-production-backups/manual/20260728_pre_hotfix.dump.gpg
```

---

# Database Point-in-Time Restore (PITR) Execution Procedure

```bash
# 1. Stop active database cluster instances
pg_ctl -D /var/lib/postgresql/data stop

# 2. Restore base full backup archive
tar -xvf /backups/base_backup_20260727.tar.gz -C /var/lib/postgresql/data/

# 3. Configure recovery.conf / postgresql.conf for target recovery time
cat <EOF> /var/lib/postgresql/data/postgresql.conf
restore_command = 'cp /backups/wal_archive/%f %p'
recovery_target_time = '2026-07-28 14:23:14 UTC'
recovery_target_action = 'promote'
EOF

# 4. Restart database engine in recovery mode
pg_ctl -D /var/lib/postgresql/data start
```

---

# Approval

| Role | Name | Date |
|------|------|------|
| DB Administration Lead | Alan Turing | 2026-07-28 |
| SRE Lead | Jonathan Vance | 2026-07-28 |
| Solution Architect | Marcus Vance | 2026-07-28 |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | 2026-07-28 | Initial Release of Backup & Restore Ops Guide | DB Team |

---

# End of Document
