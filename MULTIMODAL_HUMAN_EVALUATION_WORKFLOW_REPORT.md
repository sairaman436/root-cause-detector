# Multimodal Human Evaluation Workflow Report

## Storage Changes

- Added Flyway migration `V34__multimodal_human_evaluation.sql`.
- Added `evaluation.multimodal_human_reviews` with immutable trace linkage,
  artifact/evaluation/rubric versions, JWT-derived reviewer ID, six rubric
  dimensions, nullable recommendation score, failure classification,
  unsupported-claim flags, comments, timestamp, status, and duplicate
  protection per reviewer and trace.
- Added JPA entity and repository for the new review records.
- Existing multimodal artifacts remain file-backed and immutable. Review rows
  attach to them; they do not mutate the artifacts or any dataset.
- Runtime image packaging now includes the evaluation artifact directory in the
  final backend image.

## Endpoints

- `GET /api/v1/evaluation/multimodal/traces`
- `GET /api/v1/evaluation/multimodal/traces/{traceId}`
- `POST /api/v1/evaluation/multimodal/reviews`

The catalog accepts only known persisted artifacts with actual vision
observations. It excludes the prior `VISION_UNAVAILABLE` artifact and removes
reasoning/chain/thought fields before returning trace data to the portal.

## Frontend Changes

- Added a server-backed multimodal trace queue to the Multimodal Evaluation Lab.
- Added trace selection, immutable artifact inspection, rubric controls,
  failure classification, unsupported-claim flags, comments, saved state, and
  reviewer identity display.
- Human-review submission uses the authenticated API. Browser storage is only
  retained for optional local trace drafts and is not the governance source of
  truth.
- Added server queue progress and per-domain summary display. Averages remain
  absent until actual human scores exist.

## Authorization

- Multimodal routes require `AI_GOVERNANCE_REVIEW`, `AI_SAFETY_REVIEW`,
  `GOVERNMENT_POLICY_REVIEW`, or `AI_ADMIN`.
- Reviewer identity is taken from `AuthenticatedUser.userId`; no reviewer ID is
  accepted in the request body.
- Verified by integration tests: unauthenticated `401`, insufficient authority
  `403`, authorized submission `200`, duplicate submission `409`.
- Live local authorized-account verification succeeded. The live queue contains
  6 traces, 0 scored, and 6 remaining. No live review decision was submitted.

## Tests and Validation

- `MultimodalHumanEvaluationIntegrationTests`: 4/4 passed.
- Existing `HumanEvaluationIntegrationTests`: 4/4 passed.
- Web portal tests: 7/7 passed.
- Web portal typecheck: passed.
- Web portal lint: passed.
- Web portal production build: passed.
- Backend Maven compile/package: passed.
- Backend and web portal Docker builds: passed.
- Backend health: `UP`.
- Web portal health: HTTP `200`.
- Flyway migration `V34`: applied successfully.
- Live trace endpoint returned the six expected server-visible traces; trace
  payload inspection confirmed no private reasoning fields were exposed.

## Existing Traces Available

The server catalog currently exposes six reviewable traces: Agriculture,
Healthcare, Energy, Education, Livelihoods, and the real Water & sanitation
run. All remain `REMAINING`/unscored. The older failed Water artifact is not
reviewable, and no browser-only localStorage record is promoted implicitly.

## Remaining Blocker

There is no implementation blocker. Human scoring remains intentionally
pending: an authorized reviewer must open the Multimodal Evaluation Lab, load
the server traces, inspect each immutable trace, and submit scores explicitly.
No scores were fabricated or automatically created.
