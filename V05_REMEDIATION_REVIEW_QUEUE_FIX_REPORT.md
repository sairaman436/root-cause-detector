# V0.5 Remediation Review Queue Fix Report

## Scope

Read-only database/API verification and a visibility-only frontend fix were completed. No candidate review decision, dataset, historical candidate, or database row was modified.

## Root cause

The Training Review UI requested `/api/v1/learning/candidates?size=50`, which is page zero. The live API contained 88 total candidates and returned 50 on page zero across two pages. All 11 remediation candidates were on page one, so the UI never rendered them. The backend query and authorization path were already correct.

There was a second usability issue after the container refresh: the frontend kept the JWT only in React memory. A browser refresh or rebuilt frontend therefore lost the usable session, even when the previous tab had displayed an authenticated state. The queue also required a manual Load Candidates click after navigation.

## Affected remediation candidates

All 11 were verified in PostgreSQL as `PENDING_APPROVAL`, `review_decision IS NULL`, `synthetic=false`, `source_type=EVALUATION_RESULT`, and `training_eligible=false`.

| Candidate ID | Scenario | Task | Created at | Status |
|---|---|---|---|---|
| `1a25d517-0038-45a6-bee3-535837d9db72` | agri-pest-disease | root-cause-analysis | 2026-08-12 12:01:54.490978 | PENDING_APPROVAL |
| `5e77a843-2368-44ce-b5c9-dc487952ac5a` | education-teacher-attendance | root-cause-analysis | 2026-08-12 12:01:54.523697 | PENDING_APPROVAL |
| `129c6c54-cfae-46b6-9b39-aa5e6d9e1154` | education-student-transport | recommendation-generation | 2026-08-12 12:01:54.538247 | PENDING_APPROVAL |
| `3962c95d-9037-4fdc-b470-4b837cecd340` | education-dropout | rag-grounded-responses | 2026-08-12 12:01:54.551842 | PENDING_APPROVAL |
| `00c36462-3ee2-4828-a14b-41b21b24796d` | energy-transformer | root-cause-analysis | 2026-08-12 12:01:54.564445 | PENDING_APPROVAL |
| `39bf5eb4-dc8f-4762-a1c7-3251d1ac9fc9` | housing-market-shed | recommendation-generation | 2026-08-12 12:01:54.578341 | PENDING_APPROVAL |
| `f546da75-fd25-407d-b2f1-23f9345656ef` | energy-grid-outages | rag-grounded-responses | 2026-08-12 12:01:54.587744 | PENDING_APPROVAL |
| `7bdaf319-c6eb-46ec-bb4c-edb6c4b14cef` | health-staffing | root-cause-analysis | 2026-08-12 12:01:54.598745 | PENDING_APPROVAL |
| `61c56f49-542f-4ae2-a908-255c5e60c1cd` | housing-roof-leaks | root-cause-analysis | 2026-08-12 12:01:54.608481 | PENDING_APPROVAL |
| `ed77421c-10ca-4711-b5ea-a3af6146d480` | livelihood-seasonal-work | root-cause-analysis | 2026-08-12 12:01:54.620964 | PENDING_APPROVAL |
| `7efab948-96cb-4a8e-afe5-2ebf97d1dfd6` | water-school-handwashing | root-cause-analysis | 2026-08-12 12:01:54.635367 | PENDING_APPROVAL |

Each row is linked through its learning record to the corresponding `pilot-v05r-*` evaluation scenario, provenance, evidence, and evaluation result. None is marked historical; all are current remediation candidates.

## API behavior

Before the fix:

- `GET /api/v1/learning/candidates?size=50` returned page 0 only: `50` rows out of `88` total.
- Page 0 contained `0` remediation candidates.
- The backend route was authenticated and returned the normal paginated envelope.

After the fix:

- The same endpoint is requested with explicit `page` and `size` parameters.
- The frontend follows `totalPages` and collects all pages.
- Live API verification returned `88` total rows across `2` pages, with `38` rows on page 1.
- Page 1 contained all `11` remediation candidates, all still `PENDING_APPROVAL`.
- Unauthenticated API access returned `401`.
- No new backend endpoint or review path was introduced.

## Frontend change

Updated `apps/web-portal/src/app/page.tsx` to:

- collect all candidate pages for Training Review;
- use the same collector for the dashboard candidate counts;
- restore the current browser session from `sessionStorage` and refresh it through the existing refresh-token endpoint when needed;
- automatically load the queue when an authenticated reviewer opens Training Review;
- preserve existing authenticated API calls and refresh behavior;
- retain the existing candidate details and Approve, Select Correction, and Reject controls;
- avoid any client-side approval or status mutation.

## Regression coverage

Added:

- frontend pagination regression: 50 first-page rows plus 11 second-page remediation rows are all collected;
- backend authorized queue regression: 11 pending remediation candidates are returned to an authorized reviewer;
- backend unauthenticated queue/review checks: 401;
- backend unauthorized review check: 403 for a user without review permission.

Historical database verification after the fix:

- Historical v0.5 candidates: `22` total, `22` pending, `0` decided.
- Remediation candidates: `11` total, `11` pending, `0` decided.
- Candidate review decisions changed: **0**.

## Build and deployment verification

- Web portal Vitest: **2 passed**.
- Web portal typecheck: **passed**.
- Web portal production build: **passed**.
- Backend `TrainingReviewIntegrationTests`: **6 passed, 0 failures, 0 errors**.
- Web portal Docker image rebuilt successfully.
- Web portal container recreated with image digest `sha256:56f510053a6b208e6ccb711d38955df0f6b6a699c0de50ef0bfc0ea579f5305b`.
- Container image creation: `2026-08-12T14:04:49Z`.
- `http://localhost:3000/api/health`: `{"service":"web-portal","status":"ok"}`.
- Backend and PostgreSQL were not restarted or changed.

## Final status

The 11 remediation candidates are now visible to the existing authenticated Training Review workflow on the rebuilt web portal. After signing in, opening Training Review loads the full queue automatically; Load Candidates remains available as a retry. They remain pending and require explicit human Approve, Correct, or Reject decisions. No review was performed and dataset-v0.5 was not materialized.
