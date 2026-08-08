# Recommendation Engine

## Purpose

Defines the implemented recommendation intelligence engine that turns validated root causes, village context, evidence, RAG citations, available resources, and constraints into prioritized intervention options for human decision makers.

## Boundary

The engine is decision support only. It does not execute interventions, make autonomous policy decisions, fine-tune models, or bypass human authority. Every generated option remains in `AI_GENERATED`, `UNDER_REVIEW`, `APPROVED`, `REJECTED`, `MORE_EVIDENCE_REQUESTED`, or `SUPERSEDED` status.

## Pipeline

1. Validate that at least one root cause is present from a stored root-cause analysis or explicit request input.
2. Retrieve trusted knowledge through the existing RAG service using the root cause, village context, and domain.
3. Generate intervention options from a domain taxonomy.
4. Attach evidence references from root-cause evidence and RAG citations.
5. Score evidence strength, recommendation confidence, and implementation feasibility independently.
6. Rank options through a weighted prioritization framework.
7. Produce scheme matches with eligibility-verification warnings.
8. Persist recommendation sets, options, evidence links, risks, resources, metrics, implementation plans, reviews, and versions.

## API

- `POST /api/v1/recommendations/generate`
- `GET /api/v1/recommendations/{id}`
- `GET /api/v1/recommendations/{id}/options`
- `GET /api/v1/recommendations/{id}/evidence`
- `GET /api/v1/recommendations/{id}/risks`
- `POST /api/v1/recommendations/{id}/review`
- `POST /api/v1/recommendations/{id}/approve`
- `POST /api/v1/recommendations/{id}/reject`
- `POST /api/v1/recommendations/{id}/regenerate`

## Data Model

The persistence model lives in Flyway migration `V27__recommendation_intelligence_engine.sql` and uses the `decision` schema. The canonical tables are `recommendation_sets`, `recommendation_options`, `recommendation_evidence_links`, `recommendation_risks`, `recommendation_resources`, `recommendation_metrics`, `recommendation_scheme_matches`, `implementation_plans`, `recommendation_reviews`, and `recommendation_versions`.

## Security

Generation and regeneration require `DECISION_ANALYZE` or `DECISION_ADMIN`. Review, approval, and rejection require `DECISION_REVIEW` or `DECISION_ADMIN`. Read access requires decision read/review/admin or AI audit/admin permissions.
