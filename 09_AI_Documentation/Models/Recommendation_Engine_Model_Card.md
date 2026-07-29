# Recommendation Engine Model Card

> **Document Version:** 1.0  
> **Status:** Approved  
> **Owner:** AI Optimization & Policy Analytics Team  
> **Project:** AI Rural Root Cause Discovery System  
> **Document Type:** Production Model Specification  

---

# Model Card: Recommendation Engine (`REC-MCDA-v1`)

---

# Document Information

| Field | Value |
|---------|---------|
| Model Name | Public Works Recommendation Engine |
| Model Identifier | `REC-MCDA-v1` |
| Algorithm | Multi-Criteria Decision Analysis (TOPSIS / AHP Optimization) |
| Output Target | Ranked Mitigation Projects with Estimated Cost & Impact Score |
| Framework | Python 3.11 / SciPy / NumPy |
| MLflow Run ID | `run-rec-20260718-c21` |

---

# Model Purpose & Business Context

The Recommendation Engine converts predicted root cause diagnoses into concrete, prioritized engineering and infrastructure interventions for government decision makers (e.g., "Install solar-powered deep aquifer pump", "Deploy emergency chlorination unit").

---

# Optimization Constraints & Parameters

The model evaluates alternative public works projects against 4 weighted criteria:

1. **Urgency & Health Risk (Weight: 0.40)**: Based on root cause severity and population affected.
2. **Cost Efficiency (Weight: 0.25)**: Estimated project capital expenditure vs. budget constraint.
3. **Execution Timeline (Weight: 0.20)**: Days required for procurement and installation.
4. **Long-Term Sustainability (Weight: 0.15)**: Environmental and maintenance lifecycle score.

---

# Measured Optimization Metrics

| Evaluation Metric | Target SLA | Measured Score | Status |
|-------------------|------------|----------------|--------|
| **Rank Agreement (Kendall's Tau)** | ≥ 0.85 | **0.912** | ✅ PASS |
| **Budget Constraint Compliance** | 100% | **100%** | ✅ PASS |
| **Execution Latency (p95)** | ≤ 250 ms | **84 ms** | ✅ PASS |

---

# Approval

| Role | Name | Date |
|------|------|------|
| Lead Data Scientist | Dr. Elena Rostova | 2026-07-28 |
| Policy Analyst Lead | Gregory Vance | 2026-07-28 |
| Solution Architect | Marcus Vance | 2026-07-28 |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | 2026-07-28 | Initial Release of Recommendation Engine Model Card | AI Team |

---

# End of Document
