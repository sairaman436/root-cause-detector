# Retrieval Alignment Remediation Report

## Scope

This remediation addressed the multimodal path only:

`IMAGE -> VISION OBSERVATIONS -> RETRIEVAL CONTEXT -> GOVERNED RAG -> EVIDENCE VALIDATION -> ROOT CAUSE -> RECOMMENDATION`

No dataset, evaluation-set, training, model, or evidence record was changed.

## Confirmed Root Causes

| Area | Confirmed finding | Classification |
| --- | --- | --- |
| Vision to query | The web portal reduced the complete observation text to a small hard-coded keyword set. Healthcare became `road students`; education became `students`; livelihoods became `market livelihood road`; energy became `energy infrastructure damage`. | Poor query construction |
| Domain filtering | Evaluation labels are human-facing values. The indexed corpus uses canonical values such as `healthcare`, `education`, `livelihood`, and `sanitation`. `Water & sanitation` was not a valid retrieval domain. | Domain vocabulary mismatch |
| Healthcare | The model described the image as an old schoolhouse and explicitly retained uncertainty. The selected `Healthcare` value was user-provided evaluation context, not a confirmed image classification. | Classification uncertainty was not represented in retrieval context |
| Evidence corpus | The live index contains governed records for healthcare, energy, education, livelihood, agriculture, and sanitation. The failed runs therefore do not prove that the corpus is empty for those domains. | Corpus gap not established |
| Water/sanitation | The request stopped at the Spring Security boundary with `403 ACCESS_DENIED` before vision inference. The logged-in local account is `FIELD_SURVEYOR`; vision requires `SERVING_INFER`, `AI_OPERATOR`, or `AI_ADMIN`. | Authorization blocker |

## Before-State Failure Table

| Domain | Actual vision observation | Previous retrieval query | Retrieved evidence | Failure point |
| --- | --- | --- | --- | --- |
| Agriculture, first | Crop/leaf condition was observed. | Question plus a narrow keyword list. | Agriculture governed sources were returned. | Root-cause path completed; recommendation was correctly blocked by grounding. |
| Agriculture, second | Large field of yellow plants with brown leaves; diagnosis was uncertain. | `User question: What crop condition is visible in this field?` and `Observed terms: crop yellow plants brown leaves`. | Agriculture root-cause and food-safety sources were returned. | Root cause and recommendation completed; this is the regression baseline. |
| Healthcare | Building appeared to the model as an old schoolhouse; dirt road and nearby people; healthcare classification was not confirmed. | `User question: What visible facility characteristics should be noted? Do not identify people.` and `Observed terms: road students`. | Healthcare staffing and appointment-access sources were returned. | Root cause completed; recommendation was blocked by grounding. The query omitted the strongest visual concepts and uncertainty. |
| Energy | Aerial village scene, corrugated roofs, rust/wear, possible repairs; energy equipment was not identified. | `User question: What visible energy infrastructure is present?` and `Observed terms: energy infrastructure damage`. | No governed evidence was returned. | Retrieval failed before root cause. The query asserted a domain question but lacked specific observed energy features. |
| Education | Children at desks, classroom, worn chalkboard; uncertainty was retained. | `User question: What visible classroom conditions are present?` and `Observed terms: students`. | No governed evidence was returned. | Retrieval failed before root cause. Classroom and chalkboard observations were discarded. |
| Livelihoods | Group near a building on a dirt road, bags/colorful clothing, and two cars; market activity was not confirmed. | `User question: What visible market or livelihood activity is present?` and `Observed terms: market livelihood road`. | No governed evidence was returned. | Retrieval failed before root cause. The query was generic and did not preserve the actual scene description or uncertainty. |
| Water/sanitation | No vision result was produced. | No retrieval query was sent. | None. | `403 ACCESS_DENIED` at `/api/v1/ai/vision/analyze`. |

## Remediation Implemented

### Structured retrieval representation

`apps/web-portal/src/app/page.tsx` now creates a bounded representation containing:

- user-provided evaluation label and its canonical retrieval-domain mapping;
- `imageDomainAssumed: false` so a selected label is not presented as a visual fact;
- actual observation concepts extracted only from returned observation text;
- bounded observation summaries;
- the user question;
- model uncertainty.

The representation is retrieval context, not evidence and not a citation source.

### Query construction

The RAG query now contains:

```text
Evaluation context: <user label> (user-provided label; image domain not assumed)
Governed retrieval domain: <canonical corpus domain or not asserted>
User question: <question>
Observed concepts: <terms present in observations>
Observed summary:
- <bounded actual observation>
Model uncertainty: <actual uncertainty>
```

The full bounded observation summaries prevent the previous loss of terms such as `schoolhouse`, `classroom`, `chalkboard`, `corrugated roofs`, `water tank`, and `brown leaves`. No visual attribute is added when it was not returned by the vision model.

### Canonical domain mapping

The existing UI labels map as follows:

| UI label | RAG domain |
| --- | --- |
| Agriculture | `agriculture` |
| Healthcare | `healthcare` |
| Energy | `energy` |
| Education | `education` |
| Livelihoods | `livelihood` |
| Water & sanitation | `sanitation` |

The canonical value is used only as a governed corpus filter. The original UI label remains visible as user-provided context.

### Downstream preservation

The structured representation is sent in the existing RAG `context` map and is also included in root-cause `structuredData`. Existing evidence, citation, source-ID, root-cause, recommendation, and fail-closed API contracts are unchanged.

## Regression Tests

Added frontend contract tests for:

- observation-to-retrieval representation;
- canonical domain mapping;
- preservation of healthcare classification uncertainty;
- prevention of invented visual concepts;
- explicit `ACCESS_DENIED` and expired-session messages.

Existing RAG tests continue to cover:

- insufficient-evidence refusal;
- source-ID isolation;
- development/synthetic evidence rejection;
- citation validation for trusted documents.

## Verification Results

| Check | Result |
| --- | --- |
| Web portal tests | PASS: 7/7 |
| Web portal typecheck | PASS |
| RAG tests | PASS: 8/8 |
| RAG Python compilation | PASS |
| Backend AI integration test | PASS: 1/1 |
| Backend reactor build | PASS |
| Web portal Docker build | PASS |
| Portal health | PASS: HTTP 200 |
| Backend health | PASS: HTTP 200 |
| AI inference health | PASS: HTTP 200 |
| RAG health | PASS: HTTP 200 |

## Historical Live Rerun Status Before Authorization

The requested post-fix reruns for Healthcare, Energy, Education, Livelihoods, Water/sanitation, and one Agriculture regression were not executed because no currently usable authenticated AI account is available locally.

The only verified local login account is `admin@platform.local`, whose role is `FIELD_SURVEYOR`. The backend correctly rejects its vision request with `403 ACCESS_DENIED`; the account does not have `SERVING_INFER`, `AI_OPERATOR`, or `AI_ADMIN`. No role, account, password, or permission was changed to bypass this gate.

Therefore:

- post-fix live retrieval evidence: **not collected**;
- post-fix root-cause/recommendation results: **not collected**;
- post-fix latency comparison: **not available**;
- water/sanitation status: **blocked by authorization before vision**;
- no fabricated after-results are reported.

## Historical Remaining Gaps Before Authorization

1. Provide an authorized local AI test account through the existing identity/governance process, or have an authorized reviewer sign in. Do not broaden `FIELD_SURVEYOR` permissions.
2. Rerun only the requested failed domains plus one Agriculture regression using the same images/questions.
3. Use the live reruns to distinguish remaining ranking/corpus gaps from query-alignment improvements.
4. Human scoring remains pending and was not generated by this remediation.

## Historical Status Before Authorization

The retrieval-alignment implementation and automated regression gates pass. The milestone’s live post-fix comparison remains **BLOCKED** by the existing authorization boundary, not by a failed code or service health check.
## Controlled Live Rerun

The authorized controlled rerun was executed after normal login with the existing pilot operator account. Detailed results follow below.
| Check | Result |
| --- | --- |
| Account | `pilot-operator-20260810` / `pilot.operator.20260810@example.gov` |
| JWT | PASS: issued by `/api/v1/auth/login` |
| Verified roles | `ADMINISTRATOR`, `FIELD_SURVEYOR` |
| Verified permissions | `AI_ADMIN`, `AI_OPERATOR`, `SERVING_INFER` |
| `/api/v1/users/me` | PASS: HTTP 200 |
| `/api/v1/ai/health` | PASS: HTTP 200 |
| `/api/v1/evaluation/results?size=1` | PASS: HTTP 200; no result rows returned by this endpoint |
| Review decisions | Unchanged; no review action was submitted |

The six original image files were reused from the previous run. The raw controlled-rerun artifact is recorded at `artifacts/evaluation/retrieval-alignment-controlled-reruns-20260814.json`. All artifacts remain **UNSCORED**; no human score was generated.

### Before/after results

| Domain | Before query/result | After query/result | Grounding and recommendation | Latency |
| --- | --- | --- | --- | --- |
| Agriculture | `crop yellow plants brown leaves`; root cause and recommendation had previously run with incomplete observation context | New query included canonical `agriculture`, `brown leaves`, `yellow plants`, full bounded observation, and uncertainty; no qualifying governed citation returned | RAG **FAIL** (`INSUFFICIENT_GOVERNED_EVIDENCE`); no RCA/recommendation generated | 13,031 ms |
| Healthcare | `road students`; RCA completed and recommendation was blocked | New query included canonical `healthcare`, `dirt road`, `old schoolhouse`, `building`, `people`, full observation, and uncertainty; no qualifying governed citation returned | RAG **FAIL** (`INSUFFICIENT_GOVERNED_EVIDENCE`); no RCA/recommendation generated | 1,919 ms |
| Energy | `energy infrastructure damage`; no governed evidence and no RCA | New query included canonical `energy`, actual roof/building observations, and uncertainty; one energy maintenance source was retrieved | Observation-to-evidence **PASS** for the returned governed source; RCA completed with one validated root cause; recommendation **FAIL-CLOSED** with `RECOMMENDATION_GROUNDING_INSUFFICIENT` because the permitted source was not semantically relevant to the generated root cause | 16,061 ms |
| Education | `students`; no governed evidence | New query included canonical `education`, `classroom`, `children`, `chalkboard`, `students`, `teacher`, full observation, and uncertainty; three education sources were retrieved | Evidence **PASS**; RCA completed; recommendation generated with one source ID. It remains human-reviewable and **UNSCORED**, not automatically accepted | 5,805 ms |
| Livelihoods | `market livelihood road`; no governed evidence and no RCA | New query included canonical `livelihood`, `dirt road`, `people`, `building`, `cars`, full observation, and uncertainty; one livelihood source was retrieved | Evidence retrieval returned one source, but RCA produced no validated root cause (`NO_VALIDATED_ROOT_CAUSE`); recommendation was correctly not attempted | 3,219 ms |
| Water/sanitation | No query; prior request stopped with `403 ACCESS_DENIED` before vision under `FIELD_SURVEYOR` | With the authorized operator, the current request reached the vision path but returned `VISION_UNAVAILABLE` before validated observations; no RAG query was sent | Vision **BLOCKED** by the current vision availability/runtime path, not by the account permission boundary; no RCA/recommendation generated | 96 ms |

### Actual post-fix retrieval evidence

- Energy: `PILOT_V05_ENERGY_SOLAR_MAINTENANCE_RECOMMENDATION_GENERATION`, score `0.2111`.
- Education: `PILOT_V05R_EDUCATION_TEACHER_ATTENDANCE_ROOT_CAUSE_ANALYSIS`, score `0.2233`; `PILOT_V05_EDUCATION_TEACHER_ATTENDANCE_ROOT_CAUSE_ANALYSIS`, score `0.1837`; and the remediation source, score `0.1825`.
- Agriculture and Healthcare: zero citations passed the existing relevance/evidence gate.
- Livelihoods: `PILOT_V05_LIVELIHOOD_SUPPLY_CHAIN_RECOMMENDATION_GENERATION`, score `0.1893`; RCA grounding did not validate a cause.
- Water/sanitation: no retrieval request was made because the authorized request returned `VISION_UNAVAILABLE` before observations.

### Conclusion

The authorized rerun proves the new query representation is being sent to RAG and that scenario/domain context is preserved. It does **not** prove universal retrieval success: two domains still have no qualifying citations, one has a retrieved but semantically insufficient source, one completes end-to-end, and one reaches root cause without validation. The Water/sanitation request is no longer blocked by the original account authorization issue; it is blocked by `VISION_UNAVAILABLE` and needs a separate vision runtime/provider investigation.

No post-fix run was auto-scored, no training candidate was created, and no dataset or evaluation-set version was modified.
