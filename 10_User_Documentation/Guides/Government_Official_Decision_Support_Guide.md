# Government Official Decision Support Guide

> **Document Version:** 1.0  
> **Status:** Approved  
> **Owner:** Policy Analytics & Decision Support Team  
> **Project:** AI Rural Root Cause Discovery System  
> **Document Type:** Government Executive User Guide  

---

# Government Official Decision Support Guide

---

# Document Information

| Field | Value |
|---------|---------|
| Document Name | Government Official Decision Support Guide |
| Target Persona | Government Official / Policy Analyst |
| Required Role | `ROLE_GOVT_OFFICIAL` |
| Version | 1.0 |
| Last Updated | 2026-07-28 |

---

# Purpose

This guide assists government policy makers, district engineers, and ministry officials in navigating the evidence-based decision support portal, evaluating AI-generated root cause analyses, reviewing SHAP feature attributions, and approving public works recommendations.

---

# Key Executive Workflows

### Workflow 1: Evaluating an AI Root Cause Diagnosis
1. Log in to `https://csp.gov.in/portal` using Government Single Sign-On (SSO).
2. Select target **District Overview Dashboard**.
3. Click on any flagged red **[High Risk Sector]** on the interactive GIS map.
4. Review the **AI Root Cause Breakdown**:
   - Primary AI Prediction: e.g., `AGRICULTURAL_NITRATE_RUNOFF` (94.2% Confidence).
   - Inspect **Evidence Panel**: Review field officer photos, water sample lab tests, and 30-day rainfall charts.
   - Inspect **SHAP Attribution Chart**: See key factors driving prediction.

```text
+-----------------------------------------------------------------------+
| GIS Map View  [ Sector 4 ]                                           |
+-----------------------------------------------------------------------+
| Predicted Root Cause: AGRICULTURAL_NITRATE_RUNOFF (Confidence: 94.2%) |
| Evidence: Lab Nitrate = 54.2 ppm (SLA limit 10 ppm)                   |
| Action Options:                                                       |
|   1. [Approve Bio-Filter Installation ($45,000)] -> Click to Authorize|
|   2. [Request Re-Sampling]                                            |
+-----------------------------------------------------------------------+
```

### Workflow 2: Approving Public Works Recommendations
1. Click **[Review Recommendations]**.
2. Compare candidate projects ranked by MCDA optimization score.
3. Select preferred intervention and click **[Approve Project & Allocate Budget]**.
4. Authenticate using Digital Signature Certificate (DSC).

---

# Approval

| Role | Name | Date |
|------|------|------|
| Policy Analytics Lead | Gregory Vance | 2026-07-28 |
| Documentation Lead | Sarah Connor | 2026-07-28 |
| Solution Architect | Marcus Vance | 2026-07-28 |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | 2026-07-28 | Initial Release of Decision Support Guide | Policy Team |

---

# End of Document
