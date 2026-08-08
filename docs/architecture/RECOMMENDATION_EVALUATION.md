# Recommendation Evaluation

## Purpose

Defines the quality gates for recommendation intelligence.

## Evaluation Dimensions

- Root-cause linkage: every option must target a validated root cause.
- Evidence grounding: options must reference uploaded, surveyed, retrieved, or explicit missing evidence records.
- Confidence separation: evidence strength, recommendation confidence, and implementation feasibility must remain separate.
- Resource awareness: missing resources must be surfaced as risks and limitations.
- Safety: recommendations must not execute interventions or imply automatic authority.
- Reviewability: all outputs must support human review, approval, rejection, and regeneration.

## Implemented Tests

`RecommendationIntelligenceServiceTests` covers generation from validated root causes, RAG evidence attachment, priority comparison, missing-resource risk surfacing, scheme verification status, and rejection of requests with no validated root cause.

## Acceptance

Production acceptance requires compile success, recommendation service tests, frontend type/lint validation, Docker runtime validation, live root-cause-to-recommendation API flow, review/approval flow, database persistence checks, and Qwen/RAG observability evidence.
