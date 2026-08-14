# RAG Evidence Restoration Report

## Root Cause Of 502

The RAG service, Qdrant, Ollama, PostgreSQL, and backend network were available. The HTTP 502 was caused by the governed pilot runner requesting RAG evidence before indexing the scenario's declared, provenance-bearing pilot evidence. Retrieval therefore returned no validated citations and the unchanged `RAG_EVIDENCE_REQUIRED` gate rejected the run.

The RAG task also incorrectly invoked recommendation generation. That caused a separate `VALIDATED_ROOT_CAUSE_REQUIRED` failure for the climate RAG scenario after retrieval had been restored.

## Fix

- Pilot scenarios now ingest their declared evidence through the existing `KnowledgeRagGatewayService` before retrieval.
- Ingestion preserves the controlled source classification, scenario key, domain, version, and approved-source metadata.
- RAG retrieval, score thresholds, citation validation, and `RAG_EVIDENCE_REQUIRED` were not weakened.
- Recommendation generation remains required for recommendation tasks. RAG-grounded-response tasks now record recommendation as `NOT_APPLICABLE` and evaluate only the relevant root-cause/evidence dimensions.

## Services And Configuration

- Rebuilt `core-backend` from the current source and redeployed the existing Compose service.
- Restored the local in-network Kafka endpoint as `kafka:9092` for the backend container; this was required for backend readiness after Docker Desktop restarted.
- PostgreSQL, Qdrant, Ollama, RAG, backend, and web portal are running on the existing Compose network.
- No database schema, dataset, evaluation-set, review decision, or model configuration was changed.

## Retrieval Validation

- RAG health: passed.
- RAG readiness: passed; Qdrant reported ready.
- RAG service state: 11 documents and 11 chunks.
- Direct hybrid retrieval: passed with retrieval mode `hybrid_vector_keyword_metadata`.
- Direct retrieval latency: 12 ms.
- Citation result: 2 validated citations returned with source ID `CONTROLLED_PROJECT_PILOT`.

## Replacement Evaluation Results

| Scenario | Result ID | Status | Score | Citations | Candidate status |
|---|---|---:|---:|---:|---|
| Disaster-warning root cause | `f41e18e2-b1a9-4d7f-ac20-30de851f8dfa` | COMPLETED/PASSED | 0.8991 | 1 | Existing pending candidate |
| Livelihood-storage RAG | `4d734ea8-edd6-47cc-904a-72ccb23c138a` | COMPLETED/PASSED structural gate | 0.7778 | 1 | Blocked by `SCORE_BELOW_THRESHOLD` |
| Mobile-clinic recommendation | `0b8cad3a-ae54-45b4-9a64-19cc59cf2495` | COMPLETED/PASSED | 0.8917 | 1 | Existing pending candidate |
| Climate RAG | `16a38a33-aac5-4501-a7c7-8e28a13eb10c` | COMPLETED/PASSED | 0.9322 | 2 | `6f6f2f80-37b3-4003-b9a0-9d2dc7f61b02`, pending |

The root-cause and mobile-clinic candidate IDs already existed because those evaluations had completed before the restoration rerun. Candidate generation correctly reported them as duplicates rather than creating a second governance record. The climate RAG candidate was generated after the restored run. All three candidates remain `PENDING_APPROVAL`; no review decision was made.

The storage RAG result remains excluded from the review queue because its score is below the existing `0.80` threshold. The threshold was not changed.

## Tests

- RAG service tests: **5 passed**.
- Backend focused human-evaluation integration tests: **4 passed, 0 failed**.
- Maven backend package build: **passed**.
- Core-backend Docker image build: **passed**.
- Backend health after redeployment: **UP**.
- Web portal health after stack recovery: **healthy**.

## Remaining Blockers

- Three candidates require authenticated human review before any promotion workflow can consider them.
- The livelihood-storage RAG result requires a genuine quality improvement or explicit exclusion; it must not be promoted under the current threshold.
- `evaluation-set-v1.1.0` has not been materialized, as required.
- No training or fine-tuning was performed.
