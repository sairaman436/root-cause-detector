# DATASET v0.4 QUALITY REPORT

Materialized only from immutable v0.3 rows that pass the exact Qwen token gates and explicitly approved governed correction proposals. v0.3 was not modified and no training was performed.

- Total examples: `25`
- Root-cause count: `10`
- Recommendation count: `6`
- RAG count: `9`
- Train: `20`
- Validation: `2`
- Test: `3`
- Corrected records materialized: `6`
- Overflow source records removed: `6`
- Excluded correction records: `{"9ad7a088-7a46-453f-87bf-ccdd010dfffc:target_generation_overflow:655": 1, "9d50e469-76c3-46e5-a662-62df9ecd9a4a:target_generation_overflow:579": 1, "correction_not_linked_to_overflow_source": 3, "fb1d6884-6656-4534-8c9b-630906cdafcf:target_generation_overflow:575": 1}`
- Exact formatted sequence gate: `passed` (`1024` max)
- Exact target budget gate: `passed` (`512` max)
- Schema/citations/evidence/PII/provenance/duplicates/leakage: `passed`
