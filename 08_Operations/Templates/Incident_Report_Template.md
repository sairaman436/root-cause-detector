# Incident Report Template

> **Document Version:** <Version>  
> **Status:** <Status>  
> **Owner:** <Document Owner>  
> **Project:** AI Rural Root Cause Discovery System  
> **Document Type:** Post-Incident Post-Mortem Template  

---

# Incident Post-Mortem Report: <INC-ID>

---

# Document Information

| Field | Value |
|---------|---------|
| Incident ID | <INC-2026-XXXX> |
| Incident Severity | <Sev-1 / Sev-2 / Sev-3 / Sev-4> |
| Date & Time of Occurrence | <YYYY-MM-DD HH:MM UTC> |
| Duration of Outage | <XX Hours XX Minutes> |
| Lead Incident Commander | <Name> |
| Service(s) Affected | <Services Affected> |

---

# Executive Summary

High-level narrative explaining what happened, the root cause, business impact, and resolution timeline.

---

# Timeline of Events (UTC)

| Timestamp | Event Description | Action Taken | Operational Lead |
|-----------|-------------------|--------------|------------------|
| <HH:MM> | Prometheus trigger `Alert: HighApiLatency` | PagerDuty paged SRE Lead | System Monitor |
| <HH:MM> | Initial incident triage bridge established | Confirmed Sev-1 incident | Incident Commander |
| <HH:MM> | Executed DB connection pool failover | Traffic restored to normal | SRE Lead |
| <HH:MM> | Incident formally resolved & closed | Post-incident monitoring | Incident Commander |

---

# Root Cause Analysis (5 Whys)

1. **Why did the incident occur?**: <Reason 1>
2. **Why?**: <Reason 2>
3. **Why?**: <Reason 3>
4. **Why?**: <Reason 4>
5. **Why? (Root Cause)**: <Root Cause>

---

# Preventive Action Items

| Action Item | Description | Owner | Target Completion Date | Ticket Link |
|-------------|-------------|-------|------------------------|-------------|
| ACT-01 | Adjust PgBouncer connection pool limits | DevOps | <YYYY-MM-DD> | <JIRA-KEY> |
| ACT-02 | Add automated alert for Redis memory pressure | SRE Lead | <YYYY-MM-DD> | <JIRA-KEY> |

---

# Approval

| Role | Name | Date |
|------|------|------|
| Incident Commander | <Name> | <YYYY-MM-DD> |
| Operations Director | <Name> | <YYYY-MM-DD> |

---

# End of Document
