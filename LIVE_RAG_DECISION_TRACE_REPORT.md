# Live RAG Decision Trace Report

## Scope

This milestone delivers and validates the live decision-support experience
without changing model weights, datasets, evaluation sets, review decisions,
governance gates, or training configuration. The portal renders only data
returned by the existing backend pipeline and does not expose private
chain-of-thought.

## APIs Used

- `POST /api/v1/ai/rag/query`
- `POST /api/v1/ai/analysis/root-cause`
- `POST /api/v1/decision/analyze`
- `POST /api/v1/analysis/root-cause`
- `POST /api/v1/recommendations/generate`

The portal uses the existing authenticated `runAiWorkflow` action. It sends
`context.governed_only=true` and the selected domain to RAG. Retrieved source
IDs are passed to the recommendation request as an explicit allowed-source
constraint. Downstream work stops when retrieval, citation, or validated-root-
cause requirements fail.

## Product Experience

`LiveDecisionTrace` in `apps/web-portal/src/app/components/decision-support.tsx`
now provides:

- A prominent problem input with a clearly labelled Agriculture example.
- Domain selection for Agriculture, Healthcare, Energy, Education, Livelihoods,
  and Water / sanitation.
- Pending, processing, completed, and blocked states for each pipeline stage.
- Actual RAG evidence rows showing `VERIFIED EVIDENCE`, source ID, title when
  returned, excerpt, source type, citation ID, publisher, page, and score.
- Visible citation/support metadata from the backend response.
- Structured root-cause cards showing observed facts, validated causes,
  confidence, and uncertainty without hidden reasoning.
- Expandable recommendation traces showing target root cause, evidence,
  feasibility, effort, timeframe, risks, dependencies, and confidence.
- Explicit failure states with no fallback evidence or fabricated output.

## Backend Contract Enrichment

The RAG response now preserves and exposes:

- `supportStatus`
- `citationValidationStatus`
- `reasoningSummary`
- `promptVersion`
- `modelId`
- Retrieval and inference latency
- Citation ID, document ID, title, publisher, page, and section when returned

The core service uses the RAG `chunk_id` as the displayed citation reference
when the RAG response does not provide a separate citation record ID. This is
the stable source reference actually available to the gateway; it is not
invented metadata.

## Agriculture End-to-End Trace

Problem:

> Producers report crop stress during an irrigation interruption and uncertain
> pump maintenance responsibility.

Live persisted trace:

- RAG status: `SUPPORTED`
- Citation validation: `VALIDATED`
- Citations returned: 5
- Representative source IDs: `CONTROLLED_PROJECT_PILOT`,
  `PILOT_V05R_AGRI_PEST_DISEASE_ROOT_CAUSE_ANALYSIS`,
  `PILOT_V05R_AGRI_SEED_STORAGE_RECOMMENDATION_GENERATION`
- Root-cause analysis: `40a516c1-2211-4dd6-b4a6-bf8723018541`
- Validated root causes: 2
- Recommendation set: `f02d9e41-70ab-4cc6-9c61-cb7e82389a59`
- Recommendation options: 6
- Recommendation evidence references after fix: nonblank, unique source IDs

The earlier broad Agriculture smoke test also completed with RAG request
`d4edda01-b0a2-4588-83cf-afef4a2e46e2`, root analysis
`b407283f-aaf3-4d38-8556-6e583e26b920`, and recommendation set
`fc072c53-6446-4ef0-a3d3-bd04fc83cc0f`. It exposed the relevance limitation
described below; it did not fabricate or substitute evidence.

## Six-Domain Live Validation

Each row was exercised through governed RAG, root-cause, and recommendation
endpoints after the mapping fix. These are local development traces, not claims
about real village conditions.

| Domain | RAG | Citation validation | Sources | Root causes | Recommendations | Result |
|---|---:|---:|---:|---:|---:|---|
| Agriculture | Supported | Validated | 5 | 2 | 6 | Passed |
| Healthcare | Supported | Validated | 5 | 2 | 6 | Passed |
| Energy | Supported | Validated | 5 | 4 | 12 | Passed after domain normalization fix |
| Education | Supported | Validated | 5 | 2 | 6 | Passed |
| Livelihoods | Supported | Validated | 5 | 4 | 12 | Passed after domain mapping fix |
| Water / sanitation | Supported | Validated | 5 | 1 | 3 | Passed |

Focused rerun trace IDs:

- Energy root cause: `28688d57-2dc2-4a11-a1e0-d6c7b4cf8aac`
- Energy recommendations: `8801bb72-3917-45e7-942f-b9d05973f0b4`
- Livelihoods root cause: `2f75d360-d242-4aae-ab65-4dea76e6bd4c`
- Livelihoods recommendations: `7afede61-6232-4c46-889f-0c6df5ee8ea4`

## Validation and Traceability

For the successful live runs:

- RAG returned actual excerpts and source IDs.
- RAG citation validation returned `VALIDATED`.
- Root-cause records persisted observed facts, candidate/validated causes,
  uncertainty, confidence, and knowledge snapshot metadata.
- Recommendation records persisted options, root-cause links, evidence links,
  feasibility, risks, dependencies, and human-approval requirements.
- No recommendation review or approval was performed by this milestone.

The existing RAG evidence gate remains active. A pre-fix Livelihoods query
returned `INSUFFICIENT_EVIDENCE` with zero citations and correctly stopped
before root cause and recommendations. After the domain-value mapping fix, the
same governed workflow returned supported evidence and completed.

## Failure Safety

- RAG unavailable or empty: retrieval is marked blocked and downstream stages
  do not run.
- Missing source ID or excerpt: evidence validation is blocked.
- No validated root cause: recommendations are blocked.
- Backend/API errors are shown as pipeline failures; no fabricated output is
  created.
- Unauthenticated or survey-less users cannot run the workflow.

## Tests and Operational Checks

- Web portal typecheck: passed.
- Web portal lint: passed.
- Web portal tests: passed, 2 tests.
- Web portal production build: passed.
- Web portal Docker build: passed.
- Core backend Docker Maven compile/package: passed with `BUILD SUCCESS`.
- RAG service tests: passed, 8 tests.
- AI inference service tests: passed, 11 tests.
- RAG health: passed, 64 documents and 64 chunks available.
- Backend health: passed, `UP`.
- Portal health: passed, `ok`.
- Direct RAG search smoke test: passed with hybrid vector/keyword/metadata
  retrieval and 5 Agriculture citations.
- Six-domain authenticated API validation: completed as recorded above.

The host Maven command could not run because the host environment does not
expose `JAVA_HOME`/Maven. A Java 21 container test attempt against the
OneDrive-mounted workspace exceeded the 15-minute command limit without
producing a result. The Docker build verifies backend compilation and
packaging, but does not execute the backend test suite because the production
Dockerfile uses `-DskipTests package`.

## Known Limitations

1. A domain filter is an exact metadata filter, not a semantic scenario
   boundary. Broad queries can return related governed records from the same
   domain. The UI displays all actual returned citations and does not claim
   equal relevance for each item.
2. The current recommendation generator can produce several intervention
   options for each validated root cause. It now removes blank/null evidence
   references and constrains secondary RAG to the retrieved source IDs, but
   recommendation quality still requires human review.
3. PII, synthetic-source, provenance, and relevance gates are authoritative in
   the backend/RAG service. The core RAG response exposes support and citation
   status, while detailed per-gate diagnostics are not currently exposed to the
   portal.
4. Concurrent-request validation and container-level GPU telemetry were not
   added by this milestone. GPU telemetry remains dependent on the host/runtime
   exposing it to the container.

## Files Changed

- `apps/web-portal/src/app/page.tsx`
- `apps/web-portal/src/app/components/decision-support.tsx`
- `apps/web-portal/src/app/globals.css`
- `services/core-backend/src/main/java/com/airural/platform/core/ai/application/AiFoundationService.java`
- `services/core-backend/src/main/java/com/airural/platform/core/ai/web/dto/AiDtos.java`
- `services/core-backend/src/main/java/com/airural/platform/core/decision/application/RootCauseIntelligenceService.java`
- `services/core-backend/src/main/java/com/airural/platform/core/decision/application/RecommendationIntelligenceService.java`
