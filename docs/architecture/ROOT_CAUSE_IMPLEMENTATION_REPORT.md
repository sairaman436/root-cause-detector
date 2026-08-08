# Root Cause Implementation Report

## Architecture

The root-cause engine is implemented inside the Decision Intelligence bounded context. It exposes `/api/v1/analysis/root-cause` APIs, calls the existing RAG/Qwen pathway through a `RootCauseRagClient` boundary, persists structured JSON and normalized records in the `decision` schema, and renders results in the web portal.

## Reasoning Pipeline

The engine normalizes the problem, extracts observable facts, retrieves knowledge, scores evidence, identifies contributing factors, creates candidate causes, records alternatives, builds causal graph edges, calculates confidence, identifies uncertainty, and appends human review.

## Evidence Model

The engine preserves fact categories and distinguishes observed facts from retrieved evidence and model inference. Confidence uses source reliability, relevance, freshness, quantity, consistency, and contradiction penalties. It is not a scientific probability.

## Causal Graph Model

Graph edges connect candidate factors and candidate causes to outcomes. Relationship types use cautious language such as `FACTOR_ASSOCIATED_WITH_OUTCOME` and `CANDIDATE_CAUSE_MAY_CONTRIBUTE_TO_OUTCOME`.

## Evaluation Results

Unit tests passed for fact/evidence separation, contradiction detection, insufficient-evidence uncertainty, causal graph generation, alternatives, and confidence interpretation.

## Test Results

`./mvnw.cmd -pl services/core-backend -am -Dtest=RootCauseIntelligenceServiceTests -Dsurefire.failIfNoSpecifiedTests=false test` passed with 3 tests.

Live Docker acceptance passed with local Postgres, Qdrant, RAG service, AI inference service, and Ollama/Qwen:

- Trusted synthetic knowledge document ingested.
- Root-cause analysis generated through `/api/v1/analysis/root-cause`.
- RAG service retrieved 2 citations and called Qwen through `/v1/inference`.
- Generated analysis contained 9 facts, 5 contributing factors, 4 candidates, 2 validated root causes, 9 causal graph edges, and 1 uncertainty.
- Human review was recorded with action `ACCEPT`.
- PostgreSQL contained persisted analysis, review, and causal relationship records.

## Known Limitations

The engine provides transparent decision-support reasoning and does not claim expert-validated causal accuracy. Live acceptance still depends on local Docker services, Qdrant, RAG indexed knowledge, and Ollama/Qwen availability.

## Human Validation Results

The implementation records review actions immutably. Production human agreement metrics require real reviewer activity after deployment.
