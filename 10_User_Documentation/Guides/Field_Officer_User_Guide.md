# Field Officer User Guide

> **Document Version:** 1.0  
> **Status:** Approved  
> **Owner:** User Education & Field Operations Team  
> **Project:** AI Rural Root Cause Discovery System  
> **Document Type:** End-User Operational Guide  

---

# Field Officer User Guide

---

# Document Information

| Field | Value |
|---------|---------|
| Document Name | Field Officer User Guide |
| Target Persona | Field Officer / Surveyor |
| Required Role | `ROLE_FIELD_OFFICER` |
| Version | 1.0 |
| Last Updated | 2026-07-28 |

---

# Purpose

This guide provides field survey officers with step-by-step instructions for operating the mobile and web survey collection client, capturing GPS-tagged ground evidence, conducting offline data entry, uploading photographs, and tracking survey resolution status.

---

# Operating Modes

```text
Offline Field Mode (Zero Connectivity)
    │
    ▼ Fill survey forms & capture geo-tagged photos
Local SQLite Storage (Encrypted)
    │
    ▼ Return to District Office / Cellular Area
Automated Background Sync -> Central Server
```

---

# Step-by-Step Task Procedures

### Task 1: Creating a New Rural Survey Record
1. Open the **AI Rural Survey App** on mobile device or web browser at `https://csp.gov.in`.
2. Tap **[+ New Survey]**.
3. Select target **District** and **Village** from dropdown menu.
4. Enter physical survey metrics:
   - **Water pH Level**: Input digital meter reading (e.g., `5.8`).
   - **Infrastructure Condition**: Select from `Intact`, `Minor Leak`, `Severe Corrosion`, `Pump Failed`.
5. Tap **[Capture Photo]** to attach geo-tagged site image.
6. Tap **[Submit Survey]**.

---

# Offline Data Sync Instructions

If operating in an offline rural area:
- The app displays a blue **[Offline Mode Active]** banner.
- All submitted forms are queued locally.
- Do **NOT** force close the app or clear cache.
- When network reconnects, tap **[Sync Pending Surveys]**. The status indicator will turn green **[Sync Complete]**.

---

# Approval

| Role | Name | Date |
|------|------|------|
| Field Operations Lead | Rajesh Kumar | 2026-07-28 |
| Documentation Lead | Sarah Connor | 2026-07-28 |
| Solution Architect | Marcus Vance | 2026-07-28 |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | 2026-07-28 | Initial Release of Field Officer User Guide | Tech Writing Team |

---

# End of Document
