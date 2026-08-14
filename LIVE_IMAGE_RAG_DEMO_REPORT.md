# Live Image-to-RAG Demonstration Report

## Result

**BLOCKED before image inference.** The current live deployment cannot execute `IMAGE -> VISION -> OBSERVATIONS -> RAG -> EVIDENCE -> ROOT CAUSE -> RECOMMENDATION` because the claimed integrated vision model is not present in the running stack.

No dataset, evaluation set, training artifact, or governance record was modified.

## Live Capability Evidence

| Check | Observed result |
| --- | --- |
| Installed Ollama models | `qwen2.5:0.5b` only |
| Model family | `qwen2` |
| Model tags | `chat`, `text-generation` |
| Quantization | `Q4_K_M` |
| Ollama image template support | Absent; model metadata template contains no image input handling |
| AI provider health | `ollama`, configured model `qwen2.5:0.5b`, status `ok` |
| AI inference API | `/v1/inference` accepts a text `prompt`; no image/multipart field |
| Vision endpoint | Not present in the AI service OpenAPI surface |
| OCR/observation adapter | Not present |
| RAG readiness | Healthy, but accepts text retrieval queries only |

The existing core backend can store image evidence, but storage is not image understanding. The existing portal inspection lab therefore correctly stops at the vision capability gate and does not claim observations.

## Demonstration Attempt

### Agriculture image

No valid agriculture image fixture was available in the current repository or attachment paths. The previously referenced temporary screenshot path no longer exists. More importantly, even with a valid uploaded agriculture image, the live provider cannot accept image content.

### Expected actual behavior in the current lab

1. Select an image through the AI Inspection Lab file picker or drag/drop area.
2. The browser shows a local preview only; the image is not permanently stored.
3. `Analyze Image` stops at `VISION` with the explicit text-only provider error.
4. `MODEL OBSERVATIONS` remains unavailable.
5. No retrieval query, evidence result, citation, root cause, or recommendation is generated.
6. No fallback or fabricated output is shown.

This is the only valid result for the current deployment.

## Failure Matrix

| Scenario | Result | Reason |
| --- | --- | --- |
| Valid image | Blocked before inference | No vision-capable model or image input API |
| Unsupported image | Blocked in the existing browser file validation path | Non-image content is not sent or stored |
| Vision unavailable | Confirmed | Provider is text-only; no image route exists |
| RAG unavailable | Not reached | RAG must not run without real observations |
| Insufficient evidence | Not reached | Evidence validation cannot begin without a real retrieval query |
| Recommendation grounding failure | Not reached | Recommendation generation is correctly blocked upstream |

## Latency

No image-processing, vision, retrieval, root-cause, or recommendation latency exists for this demonstration because no image inference request was made. Reporting a numeric latency would be fabricated.

## Exact Blocker

The platform needs the already-approved vision capability to be present in the deployed environment, including:

- a vision-capable model actually installed and selected by Ollama or the existing provider;
- an image-bearing inference contract, such as multipart or validated image references;
- provider adapter support for image input;
- a structured observation response schema;
- preservation of image/provenance metadata;
- an observation-to-RAG query mapping;
- contract tests for model observations, source IDs, evidence validation, and downstream stop behavior.

The user instruction prohibits adding or replacing a model, so this milestone cannot be completed until the missing existing model/provider deployment is restored or its correct runtime location/configuration is supplied.

## Validation Performed

- `http://localhost:3000/api/health` returned HTTP 200.
- `http://localhost:8101/health/ready` returned HTTP 200.
- `http://localhost:8102/health/ready` returned HTTP 200.
- Ollama `/api/tags` returned only `qwen2.5:0.5b`.
- Ollama `/api/show` identified a text-generation Qwen2 model with no image template support.
- AI OpenAPI inspection found `/v1/inference` but no image/multimodal endpoint.
- Existing portal build/typecheck/lint/tests remain green from the preceding lab implementation.

## Final Status

**NOT COMPLETE: real image-to-RAG demonstration unavailable in the current live deployment.** No fake observation, evidence, citation, root cause, recommendation, or timing was created.

