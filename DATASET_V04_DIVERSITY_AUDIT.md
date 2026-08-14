# Dataset v0.4 Diversity Audit

This report is read-only. Dataset v0.4 was not modified.

- Total records: `25`
- Near-duplicate threshold: `0.45` Jaccard similarity on scenario text
- Exact scenario-text fingerprints duplicated: `0`

## Coverage Matrix

| Domain | Root cause | Recommendation | RAG | Total |
|---|---:|---:|---:|---:|
| Water & sanitation | 3 | 3 | 2 | 8 |
| Agriculture & food production | 2 | 1 | 2 | 5 |
| Healthcare access | 2 | 1 | 2 | 5 |
| Energy/electricity | 0 | 0 | 0 | 0 |
| Education | 1 | 0 | 1 | 2 |
| Livelihoods/markets | 1 | 0 | 1 | 2 |
| Climate/disaster resilience | 0 | 0 | 0 | 0 |
| Housing/basic infrastructure | 1 | 1 | 1 | 3 |

## Task Distribution

- `root-cause-analysis`: `10`
- `recommendation-generation`: `6`
- `rag-grounded-responses`: `9`

## Overrepresentation And Gaps

- Overrepresented: Water & sanitation is the largest domain; agriculture and healthcare are the next largest.
- Missing: Energy/electricity and climate/disaster resilience have no v0.4 records.
- Weak: Education and livelihoods/markets have two records each and lack recommendation coverage.
- The required v0.5 target is 24 scenarios: eight domains multiplied by three tasks.

## Evidence Source Reuse

- `14` records use source set `CONTROLLED_PROJECT_PILOT`.
- `11` records use source set `approved-rural-development-manual, approved-synthetic-rural-policy, approved-water-policy, development-evaluation-fixture`.
- The four-source bundle containing `development-evaluation-fixture` is reused by 11 records and requires provenance review before any future materialization.

## Semantic Redundancy

- Candidate near-duplicate pairs at or above `0.45`: `10`.
- `0.615`: `pilot-v03-exp3-livelihood-market-rag-001` <-> `pilot-v03-exp3-infrastructure-road-rag-001`
- `0.600`: `pilot-v03-agriculture-guidance-rag-001` <-> `pilot-v03-expansion-agriculture-rag-001`
- `0.571`: `pilot-v03-expansion-health-rag-001` <-> `pilot-v03-health-guidance-rag-001`
- `0.538`: `pilot-v03-exp3-livelihood-market-rag-001` <-> `pilot-v03-exp3-sanitation-drainage-rag-001`
- `0.533`: `pilot-v03-agriculture-guidance-rag-001` <-> `pilot-v03-water-guidance-rag-001`
- `0.500`: `pilot-v03-expansion-health-rag-001` <-> `pilot-v03-exp3-sanitation-drainage-rag-001`
- `0.500`: `pilot-v03-exp3-sanitation-drainage-rag-001` <-> `pilot-v03-exp3-infrastructure-road-rag-001`
- `0.500`: `pilot-v03-exp3-livelihood-market-rag-001` <-> `pilot-v03-expansion-health-rag-001`
- `0.467`: `pilot-v03-expansion-health-rag-001` <-> `pilot-v03-exp3-infrastructure-road-rag-001`
- `0.462`: `pilot-v03-agriculture-water-recommendation-001` <-> `pilot-v03-agriculture-reliability-root-cause-001`

## v0.5 Diversity Gate

New candidates must have a distinct problem meaning, a unique scenario key, a unique evidence document/source ID, and a source-specific evidence block. The gate flags semantic similarity at or above the configured threshold and blocks exact content duplicates.

## Decision

Retain v0.4 unchanged. Generate new PILOT_EVALUATION candidates for all eight domains and all three task types through the authenticated governed pipeline. Do not materialize v0.5 until human decisions exist.
