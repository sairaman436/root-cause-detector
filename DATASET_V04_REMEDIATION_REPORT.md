# Dataset v0.4 Remediation Report

Status: VALIDATED

## Scope

Dataset `dataset-v0.3` remains immutable. Six correction proposals received
authenticated APPROVE decisions. Dataset-v0.4 was materialized read-only; no
training run was started.

## Affected Records

Six approved v0.3 records exceeded the validated 1,024-token formatted
training limit and the 512-token target-generation budget. The original
records remain unchanged:

| Record | Task | Split | Formatted tokens | Target tokens | Correction candidate |
|---|---|---:|---:|---:|---|
| `0e454d77-aed4-4ce9-a019-c1d92a97aab7` | recommendation | train | 1127 | 681 | `938ac69d-a96c-46dc-94b7-aa6a2e04a73d` |
| `437b4674-d344-40f5-bc47-84488f8792da` | recommendation | validation | 1120 | 679 | `9de3066a-8508-4b32-a02e-fe9c45b5d66c` |
| `43ea9cc6-45cf-43af-b746-e7bca8ff7128` | RAG | train | 1428 | 986 | `ea35379e-2bb6-4aaf-bb68-8ab13da6b906` |
| `6122d108-a6a8-44c7-8351-6d2ab5cef88e` | RAG | train | 1426 | 984 | `65e25c4b-eafb-4875-95d1-71eca7c5de88` |
| `92191669-e7f4-41db-87fc-bc57c7f710cf` | recommendation | train | 1105 | 662 | `2e16a825-c7b2-4cbb-a6dc-8e2e55f68f64` |
| `de3a0449-b8a8-43b1-b27b-5ffb5e22e3bd` | RAG | train | 1427 | 986 | `187d7168-9514-40c5-b182-bcbbc7a5e0cd` |

## Correction Proposals

Six governed proposals were created through the authenticated correction API.
They preserve the original record, provenance, evidence, source IDs, task and
review lineage. All six now have persisted authenticated `APPROVE` decisions;
corrected and rejected counts are zero.

All six were explicitly approved. Materialized target tokens are 449, 439,
96, 94, 435 and 95; all materialized formatted examples pass the exact Qwen
tokenizer gates.

## Current Dataset Baseline

| Version | Total | Root cause | Recommendation | RAG | Train | Validation | Test |
|---|---:|---:|---:|---:|---:|---:|---:|
| `dataset-v0.3` | 28 | 10 | 9 | 9 | 23 | 2 | 3 |
| `dataset-v0.4` | 25 | 10 | 6 | 9 | 20 | 2 | 3 |

Dataset v0.4: **materialized and validated**.

## Validation and Exclusions

Three unrelated v0.3 recommendation records were excluded because no governed
correction exists for their target overflow: `9ad7a088-7a46-453f-87bf-ccdd010dfffc`
(655 tokens), `9d50e469-76c3-46e5-a662-62df9ecd9a4a` (579 tokens), and
`fb1d6884-6656-4534-8c9b-630906cdafcf` (575 tokens). No record was silently
truncated or removed from v0.3.

## Required Next Gate

The next gate is independent evaluation-set replacement review. Dataset-v0.4
passed exact tokenizer, target-budget, schema, citation, evidence, PII,
provenance, duplicate, scenario and split-leakage validation.

## Verification

- PostgreSQL: reachable; six proposals have APPROVE decisions.
- Local backend artifact: Maven offline reactor build passed.
- Flyway: 33 migrations validated; schema at v32; no migration needed for
  the replacement run.
- Exact Qwen gates: max formatted 895/1024, max target 449/512.
- No database reset and no v0.3 mutation occurred.
