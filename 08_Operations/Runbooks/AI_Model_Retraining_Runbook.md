# AI Model Retraining Runbook

> **Document Version:** 1.0  
> **Status:** Approved  
> **Owner:** MLOps & AI Data Engineering Team  
> **Project:** AI Rural Root Cause Discovery System  
> **Document Type:** MLOps Operational Runbook  

---

# AI Model Retraining Runbook

---

# Document Information

| Field | Value |
|---------|---------|
| Runbook Name | AI Model Retraining Runbook |
| System Component | MLOps Feature Store & AI Model Pipeline |
| Estimated Execution Time | 90 Minutes |
| Execution Trigger | Monthly Automated Schedule or Data Drift Alert (PSI > 0.20) |
| Access Requirements | MLflow Registry & GPU Worker Cluster Permissions |

---

# Purpose

This runbook defines the operational execution steps for triggering, evaluating, validating, and shadow-deploying updated machine learning model candidates for the Root Cause Discovery Engine (`RC-XGB-v2`) and Recommendation Module (`REC-MCDA-v1`).

---

# MLOps Retraining Lifecycle

```text
Data Drift Trigger (PSI > 0.20) or Monthly Cron Schedule
                        │
                        ▼
Step 1: Extract & Anonymize Latest 30-Day Survey Training Set
                        │
                        ▼
Step 2: Execute Automated Feature Engineering & Vectorization
                        │
                        ▼
Step 3: Execute Model Hyperparameter Tuning & Cross-Validation Run
                        │
                        ▼
Step 4: Run Automated Model Validation Suite & Fairness Check
                        │
                        ▼
Step 5: Register Candidate Model in MLflow Staging Registry
                        │
                        ▼
Step 6: Shadow Inference Deployment (24-Hour Dual Read Validation)
                        │
                        ▼
Step 7: Production Promotion Signoff
```

---

# Operational CLI Commands

```bash
# 1. Trigger Model Retraining Pipeline
python -m src.mlops.retrain_pipeline \
  --dataset-path "s3://csp-ml-data/training/2026_q2_dataset.parquet" \
  --model-name "RootCauseClassifier" \
  --target-metric "f1_score"

# 2. Evaluate Candidate Model against Baseline
python -m src.mlops.evaluate_candidate \
  --candidate-uri "runs:/b981f7a82b/model" \
  --baseline-uri "models:/RootCauseClassifier/Production"

# 3. Promote Candidate Model to Staging in MLflow
python -m src.mlops.register_model \
  --run-id "b981f7a82b" \
  --stage "Staging"
```

---

# Approval

| Role | Name | Date |
|------|------|------|
| Lead Data Scientist | Dr. Elena Rostova | 2026-07-28 |
| AI QA Lead | Dr. Aris Thorne | 2026-07-28 |
| Solution Architect | Marcus Vance | 2026-07-28 |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | 2026-07-28 | Initial Release of AI Model Retraining Runbook | MLOps Team |

---

# End of Document
