# Multimodal Grounding Remediation Report

Date: 2026-08-14

## Scope

This remediation addressed semantic grounding after the existing multimodal path produced valid citation IDs but returned evidence and recommendations that were not reliably tied to the image observations. No datasets, evaluation sets, model weights, prompts used for training, or training configuration were changed.

## Confirmed Defect

The prior live agriculture trace returned valid source IDs, but retrieval included unrelated food-safety material and a generic controlled-pilot record alongside crop-disease material. The recommendation stage then emitted generic options, including market-access support, without demonstrating that the option addressed the validated crop-disease finding. Citation identity validation therefore passed while semantic support was incomplete.

The defect was in application-level grounding and recommendation selection, not in citation ID syntax. The prior trace also showed that the vision model reported observations and uncertainty; it did not establish a diagnosis. That distinction is preserved.

## Remediation

### Observation to evidence

Added `SemanticGroundingValidator` to retain only non-empty, sufficiently scored citations that share meaningful terms with the actual observation context. Root-cause analysis now treats an empty relevant-citation set as insufficient evidence and does not validate a causal candidate from unrelated retrieval results.

### Evidence to root cause

Root-cause validation now requires:

- relevant retrieved evidence when retrieval was requested;
- supporting facts or citation terms that align with the proposed finding;
- explicit uncertainty and evidence references from the existing response contract;
- no downstream validated root cause when the retrieved evidence is semantically unrelated.

### Root cause to recommendation

Recommendation generation now filters permitted source IDs through the same relevance validator and requires intervention alignment with the validated root cause or its relevant evidence. Generic options are excluded when they do not address the grounded cause. When no supported option remains, the service fails closed with `RECOMMENDATION_GROUNDING_INSUFFICIENT` instead of returning a generic intervention.

### Frontend behavior

The inspection workspace preserves the partial trace when recommendation generation is blocked. It labels the failure as `Grounding link blocked` and explains that the root cause or intervention did not pass semantic evidence grounding. It does not invent evidence, scores, explanations, or recommendations.

## Before and After Evidence

| Check | Before remediation | After remediation |
|---|---|---|
| Citation IDs | IDs were syntactically valid | Existing IDs remain preserved and are filtered for relevance |
| Irrelevant retrieval | Food-safety and generic pilot evidence reached the decision path | Irrelevant citations are removed; no validated root cause is produced when none remains |
| Crop-disease recommendation | Generic market-access option was emitted | Market option is excluded; only evidence-aligned crop-disease options remain |
| Unsupported root cause | Generic token overlap could validate a weak causal claim | Unrelated retrieved evidence produces zero validated root causes |
| Empty grounding | Could continue toward generic recommendations | Recommendation service fails closed with an explicit grounding error |

The focused service tests demonstrate the after-state: unrelated retrieval produces no validated root cause, unrelated permitted evidence is rejected, and generic market recommendations are excluded from crop-disease evidence. These are contract tests, not fabricated live metrics.

## Files Changed for This Remediation

- `services/core-backend/src/main/java/com/airural/platform/core/decision/application/SemanticGroundingValidator.java`
- `services/core-backend/src/main/java/com/airural/platform/core/decision/application/RootCauseIntelligenceService.java`
- `services/core-backend/src/main/java/com/airural/platform/core/decision/application/RecommendationIntelligenceService.java`
- `services/core-backend/src/test/java/com/airural/platform/core/decision/RootCauseIntelligenceServiceTests.java`
- `services/core-backend/src/test/java/com/airural/platform/core/decision/RecommendationIntelligenceServiceTests.java`
- `apps/web-portal/src/app/page.tsx`
- `apps/web-portal/src/app/components/decision-support.tsx`

## Verification

Passed:

- Backend focused grounding tests: 14/14
- AI inference tests: 13/13
- RAG tests: 8/8
- Web portal tests: 2/2
- Web portal typecheck
- Core backend Docker build
- Web portal Docker build
- Core backend health check
- RAG health check
- Web portal health check

The local source `npm run build` was attempted after deployment and hit a Windows `EPERM` while Next.js tried to unlink the running `.next` directory. The production Docker build completed successfully; no source build failure was observed.

## Live Multimodal Verification

The real agriculture image used was:

- File: `C:\Users\saira\AppData\Local\Temp\csp-agriculture.jpg`
- Type: JPEG
- Size: approximately 457 KB
- SHA-256: `0373B8AF5638EF20877BEA75ECDDAB2DB3C55D79946FD43E553D5EC4F4CE6BFE`
- Question: `What visible crop condition is present in the leaves?`
- Configured vision model: `moondream:1.8b`

The browser request failed closed at the vision stage and did not produce a new observation, retrieval query, evidence set, root cause, or recommendation. A direct call to `http://localhost:8101/v1/vision/analyze` also timed out after approximately 182.6 seconds. A direct text-only Ollama request to `moondream:1.8b` timed out after approximately 45.2 seconds.

Ollama logs identify the provider/runtime blocker:

- `CUDA driver version is insufficient for CUDA runtime version`
- `ggml_backend_cuda_init: invalid device 0`
- repeated runner/model loading before the request timed out

The AI inference service, backend, RAG service, and web portal health endpoints remained healthy because their process/readiness checks do not prove a completed model generation. No successful live post-remediation multimodal result is claimed.

## Domain Coverage

No additional healthcare, energy, education, livelihood, or water/sanitation live runs were claimed. The provider timeout prevents honest end-to-end domain verification. The semantic gates are domain-neutral and covered by focused unit tests, but each domain still requires a real governed evidence run after Ollama is operational.

## Result

**SEMANTIC GROUNDING FIX: VALIDATED BY CONTRACT TESTS**

**LIVE MULTIMODAL RUN: BLOCKED BY OLLAMA/CUDA RUNTIME**

The application now fails closed when semantic links are missing. The remaining blocker is to restore a working Ollama runtime, resolve the host/container CUDA driver compatibility or use the already-supported CPU execution path, then rerun one real agriculture image through the complete pipeline and verify the returned source evidence semantically supports the root cause and recommendation.

