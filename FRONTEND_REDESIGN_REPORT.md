# Frontend Redesign Report

<!--
Purpose: Records the implementation and validation evidence for the decision-support portal redesign.
Why it exists: Frontend changes need a durable handoff that separates verified UI work from backend-dependent runtime checks.
Architecture fit: Documents the Next.js presentation boundary without changing backend, data, AI, or governance contracts.
-->

## Scope

The web portal was redesigned as a decision-support workspace. Existing API calls, authentication, review actions, AI inference behavior, dataset artifacts, and evaluation governance were preserved.

## Pages And States

- Dashboard: real session workflow, protected platform snapshot, review counts, AI health/deployment status when available, and next-action guidance.
- Survey: existing create/submit workflow retained with clear disabled and empty states.
- Evidence / RAG: upload workspace plus evidence metadata, checksum, retrieval answer, source IDs, excerpts, and citation scores.
- AI Analysis: typed presentation of Qwen interpretation, observed signals, root-cause signals, evidence, uncertainty, and decision output.
- Root Cause Analysis: facts, validated causes, alternatives, confidence, severity, limitations, and follow-up context.
- Recommendations: multiple intervention cards, priorities, feasibility, effort, risks, evidence, confidence, comparisons, and implementation timeline.
- Training Review: existing authenticated approve/correct/reject workflow retained and visually separated from semantic evaluation.
- Human Evaluation: existing held-out BASE Qwen rubric workflow retained as a separate scored/remaining workspace.
- Model Evaluation: persisted evaluation records only; unavailable comparisons are explicitly labelled as unavailable.
- Governance / Dataset: persisted dataset registry, candidate queue, human-evaluation progress, and runtime status only.
- Reports, Profile, Settings, Login: existing routes/state branches retained.

The application uses one Next.js root route with state-driven workspace navigation. It does not claim separate URL routes that do not exist in the current application.

## Reusable Components

Added `apps/web-portal/src/app/components/decision-support.tsx` with typed presentation adapters for:

- Metrics and intentional empty states.
- Evidence and RAG citation views.
- Structured AI analysis and root-cause views.
- Recommendation cards, comparison rows, risk/details disclosure, and implementation timelines.

The existing page remains the workflow adapter and continues to own API calls and authenticated actions. The new components do not contain business rules or make model decisions.

## Session And Error Handling Follow-up

- The portal now refreshes an expired access token once through the existing `/api/v1/auth/refresh` contract before retrying a protected request.
- Refresh-token rotation is serialized in the browser so concurrent dashboard requests cannot reuse and invalidate the same rotated refresh token.
- A rejected or expired session is shown as `Session expired` instead of a misleading authenticated state.
- Duplicate survey-name database errors are translated into an actionable message asking the operator to choose a different name.
- Only the `web-portal` image was rebuilt and redeployed; backend, PostgreSQL, review data, and other services were not changed.

## Existing APIs Used

- `/api/v1/ai/health`
- `/api/v1/platform/deployment-status`
- `/api/v1/learning/candidates`
- `/api/v1/evaluation/human/examples`
- `/api/v1/datasets`
- `/api/v1/evaluation/results`
- Existing survey, evidence, RAG, analysis, recommendation, training-review, human-evaluation, and report endpoints

Each dashboard snapshot call is independently failure-tolerant. An unavailable endpoint renders `Not loaded`, `Not available`, or an intentional empty state instead of fabricated data.

## Files Changed

- `apps/web-portal/src/app/page.tsx`
- `apps/web-portal/src/app/globals.css`
- `apps/web-portal/src/app/components/decision-support.tsx`
- `FRONTEND_REDESIGN_REPORT.md`

## Validation

- `npm run typecheck`: passed.
- `npm run lint`: passed with `--deny-warnings`.
- `npm run test`: passed, 1 test file and 1 test.
- `npm run build`: passed with Next.js 16.3.0.
- `git diff --check`: passed.
- Local portal root check: `http://localhost:3000/` returned HTTP 200 and portal markup.
- Docker portal health check: passed after rebuilding the image; `/api/health` returned HTTP 200.
- Live container verification: current navigation includes `Evidence / RAG` and `Model Evaluation`; stale `Evidence Upload` navigation is absent.

## Runtime Limitations

- Authenticated API behavior, training-review actions, human-evaluation submission, and live AI rendering require the local backend, PostgreSQL, and an authorized account to be running. They were not altered by this frontend-only milestone.
- No new backend endpoint was introduced for model comparison or static dataset artifact inspection; those views intentionally show persisted API data or an unavailable state.
- No screenshot artifact was captured in this validation run.
