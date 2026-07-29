# Drift and Bias Test Report

> **Document Version:** 1.0  
> **Status:** Approved  
> **Owner:** AI Quality Engineering & Ethics Team  
> **Project:** AI Rural Root Cause Discovery System  
> **Document Type:** Test Execution & Evaluation Report  

---

# Drift and Bias Test Report

---

# Document Information

| Field | Value |
|---------|---------|
| Document Name | Drift and Bias Test Report |
| Domain | AI Ethics & Model Monitoring |
| Version | 1.0 |
| Status | Approved |
| Owner | AI QA Team |
| Created Date | 2026-07-28 |
| Last Updated | 2026-07-28 |

---

# Purpose

This report documents the empirical evaluation of data drift, concept drift, demographic fairness, and algorithmic bias for the deployed AI models supporting the AI Rural Root Cause Discovery System across diverse geographic districts.

---

# Business Context

To maintain public trust and regulatory compliance, decision support models must not discriminate against underrepresented rural communities, remote agricultural zones, or specific socio-economic demographics. Furthermore, seasonal environmental shifts (e.g., monsoon vs. dry season) can induce data drift that degrades model accuracy over time.

---

# Test Environment & Target Models

- **Evaluation Window**: Q2 2026 Operational Survey Cycle
- **Evaluated Model**: `RC-XGB-v2` (Root Cause Classifier)
- **Baseline Dataset**: 2025 Annual Baseline Survey Data (25,000 records)
- **Current Production Dataset**: Q2 2026 District Survey Submissions (10,500 records)

---

# Data Drift & Concept Drift Analysis

### Population Stability Index (PSI) Analysis

Data drift was evaluated across key numerical and categorical features using Kolmogorov-Smirnov (KS) tests and Population Stability Index (PSI).

| Feature Name | Feature Type | PSI Value | Drift Status | Remediation Required |
|--------------|--------------|-----------|--------------|----------------------|
| `monthly_rainfall_mm` | Continuous | 0.245 | ⚠️ Significant Drift | Yes (Seasonal Recalibration) |
| `groundwater_depth_m` | Continuous | 0.082 | ✅ Stable | No |
| `complaint_category` | Categorical | 0.041 | ✅ Stable | No |
| `village_population_density` | Discrete | 0.015 | ✅ Stable | No |
| `crop_yield_loss_pct` | Continuous | 0.189 | ⚠️ Moderate Drift | Retraining Recommended |

---

# Algorithmic Bias & Fairness Metrics

Fairness was evaluated across 4 geographical sectors (North District, South District, East Hills, West Arid Zone) to ensure equitable root cause prioritization.

| Demographic Sector | Sample Size | Selection Rate | Disparate Impact Ratio | Equalized Odds Difference | Status |
|--------------------|-------------|----------------|------------------------|---------------------------|--------|
| North District | 3,200 | 28.4% | 1.00 (Reference) | 0.00 | Baseline |
| South District | 2,800 | 27.1% | 0.95 | 0.02 | ✅ Compliant |
| East Hills | 2,100 | 26.5% | 0.93 | 0.03 | ✅ Compliant |
| West Arid Zone | 2,400 | 25.8% | 0.91 | 0.04 | ✅ Compliant |

> **Note**: Disparate Impact Ratio is well within the acceptable threshold of [0.80, 1.25], satisfying the 80% Rule for demographic fairness.

---

# Summary Findings & Recommendations

1. **Seasonal Drift Detected**: `monthly_rainfall_mm` experienced significant drift (PSI = 0.245) due to early monsoon arrival in Q2.
2. **Fairness Verified**: Model output recommendations show no statistically significant bias across geographical sectors or population densities.
3. **Action Items**:
   - Trigger automated model retraining incorporating Q2 2026 weather data.
   - Update feature scaling normalization bounds for `monthly_rainfall_mm`.
   - Maintain continuous weekly PSI monitoring in Prometheus.

---

# Approval

| Role | Name | Date |
|------|------|------|
| AI Ethics Officer | Dr. Maya Lin | 2026-07-28 |
| AI QA Lead | Dr. Aris Thorne | 2026-07-28 |
| Lead Data Scientist | Dr. Elena Rostova | 2026-07-28 |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | 2026-07-28 | Initial Release of Drift and Bias Test Report | AI QA Team |

---

# End of Document
