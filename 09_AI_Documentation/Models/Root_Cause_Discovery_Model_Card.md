# Root Cause Discovery Model Card

> **Document Version:** 1.0  
> **Status:** Approved  
> **Owner:** AI Science & Data Engineering Team  
> **Project:** AI Rural Root Cause Discovery System  
> **Document Type:** Production Model Specification  

---

# Model Card: Root Cause Discovery Engine (`RC-XGB-v2`)

---

# Document Information

| Field | Value |
|---------|---------|
| Model Name | Root Cause Classification Engine |
| Model Identifier | `RC-XGB-v2` |
| Algorithm | Gradient Boosted Decision Trees (XGBoost 2.0) |
| Target Output | Multi-class Root Cause Category (14 Distinct Classes) |
| Framework | Python 3.11 / XGBoost / TreeSHAP |
| MLflow Run ID | `run-rc-20260715-a9f` |

---

# Model Purpose & Business Context

The Root Cause Classification Engine analyzes 42 numerical, categorical, and spatial survey features to discover the underlying structural failure causing rural distress (e.g., pipe corrosion, seasonal aquifer depletion, pump motor burnout, chemical runoff).

---

# Training & Evaluation Data

- **Training Dataset**: `AI Training Dataset.md` (25,000 historical rural survey records across 50 districts).
- **Validation Dataset**: `AI Validation Dataset.md` (5,000 holdout records).
- **Feature Count**: 42 Normalized Features (pH, rainfall, soil salinity, pipe material, population density).

---

# Measured Performance Benchmarks

| Evaluation Metric | Target SLA | Measured Score | Quality Gate Status |
|-------------------|------------|----------------|---------------------|
| **Classification Accuracy** | ≥ 90.0% | **94.2%** | ✅ PASS |
| **Micro F1 Score** | ≥ 0.88 | **0.925** | ✅ PASS |
| **Macro Recall** | ≥ 0.85 | **0.892** | ✅ PASS |
| **High Severity Precision** | ≥ 0.92 | **0.958** | ✅ PASS |
| **Inference Latency (p95)** | ≤ 300 ms | **118 ms** | ✅ PASS |

---

# Explainability & Attribution Matrix

Every prediction outputs top 3 TreeSHAP feature attribution weights.

```text
Sample Output:
- Primary Cause: "CHEMICAL_RUNOFF_CONTAMINATION" (Confidence: 0.948)
- Key Evidence:
  1. `nitrate_concentration_ppm` = 54.2 (+0.52 SHAP value)
  2. `distance_to_agricultural_zone_m` = 120 (+0.31 SHAP value)
```

---

# Approval

| Role | Name | Date |
|------|------|------|
| Lead Data Scientist | Dr. Elena Rostova | 2026-07-28 |
| AI Ethics Officer | Dr. Maya Lin | 2026-07-28 |
| Solution Architect | Marcus Vance | 2026-07-28 |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | 2026-07-28 | Initial Production Model Card | AI Team |

---

# End of Document
