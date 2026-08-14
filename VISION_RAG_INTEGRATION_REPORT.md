# Vision to RAG Integration Report

## Status

**BLOCKED: the existing vision capability is not present in the current source checkout or live deployment.** No image-to-RAG execution was claimed, and no datasets, evaluation sets, training artifacts, or governance data were changed.

## Trace Results

| Stage | Existing implementation | Result |
| --- | --- | --- |
| Image upload | Core backend `POST /api/v1/evidence` accepts multipart image evidence and stores it through the evidence storage abstraction. | Image bytes can be stored, but this is not image understanding. |
| Vision model | No vision-capable model is configured or installed. Live Ollama has only `qwen2.5:0.5b`. | Missing |
| Vision endpoint | AI service exposes `/v1/inference`, `/v1/analysis/root-cause`, `/v1/stream`, and health/prompt routes. No image or multipart inference endpoint exists. | Missing |
| Image preprocessing | No active image preprocessing, OCR, or observation adapter exists. | Missing |
| Observation schema | No image observation response is produced by the active AI service. | Missing |
| RAG query | RAG service exposes `POST /v1/query`; the core backend also exposes `/api/v1/ai/rag/query`. Both accept text query/context and governed retrieval options. | Present for text |
| Evidence validation | Existing RAG response validates retrieved citations/source IDs and withholds the answer when citation validation fails. | Present for text |
| Root cause | Existing constrained text path consumes problem/evidence/citation context. | Present for text |
| Recommendation | Existing backend recommendation workflow consumes validated root-cause/evidence context. | Present for text |

The exact break is:

```text
IMAGE
  -> core evidence storage (bytes only)
  -> no deployed vision provider / no image inference API
  -> no structured observations
  -> no observation-derived retrieval query
  -> text RAG only
```

## Live Verification

- Ollama `/api/tags` returned exactly one model: `qwen2.5:0.5b`.
- Ollama model metadata reported family `qwen2`, tags `chat,text-generation`, quantization `Q4_K_M`.
- Ollama model template contains no image handling.
- AI provider health returned `ollama`, configured model `qwen2.5:0.5b`, status `ok`.
- AI OpenAPI surface contains no image/multimodal route.
- RAG readiness returned healthy with Qdrant available.
- Web portal health returned HTTP 200.

The model and endpoint facts contradict the milestone premise that an existing vision-capable model is integrated. The prior inspection-lab UI therefore correctly stops at the vision capability gate instead of sending an invalid image request.

## Required Smallest Integration Once Capability Is Restored

The smallest legitimate change is not a parallel schema or fake adapter. It is to restore the already-approved vision provider at its intended deployment boundary, then connect its real response to the existing text RAG request:

```json
{
  "observations": [],
  "question": "",
  "context": []
}
```

The existing RAG request should receive the actual observation serialization and user question as retrieval input. Retrieved source IDs, citations, provenance, and evidence validation must remain owned by the governed RAG system. The vision model must not create citations.

## Failure Behavior

The current truthful behavior is:

- vision unavailable: show `Image analysis unavailable.`;
- no observations: do not call RAG;
- RAG failure: show `Image analyzed, but evidence retrieval failed.` and do not generate a recommendation;
- insufficient governed evidence: show `Image observations available, but there is insufficient governed evidence to make a recommendation.` and stop;
- citation failure: retain the existing RAG withholding behavior;
- no fallback to fabricated text, evidence, citations, root cause, or recommendation.

## End-to-End Test Result

The requested real agriculture image run could not be executed because no valid agriculture image fixture was available in the current repository or attachment paths, and the active model cannot accept images in any case. No image-to-vision request was made. Therefore there are no honest observation, retrieval-query, evidence, root-cause, recommendation, or latency results to report.

## Regression Coverage Status

Existing text RAG and constrained inference coverage remains in place. Image-specific regression tests cannot pass until an image inference contract and real vision provider are present. The required future tests are:

- image bytes reach the vision adapter;
- model observations reach RAG;
- question and observation payload are preserved;
- evidence source IDs survive through root cause and recommendation;
- invalid citations block downstream output;
- vision failure does not trigger fabricated output;
- RAG failure does not trigger fabricated output;
- insufficient evidence blocks recommendation generation.

## Validation Performed

- Web portal health: passed.
- AI inference readiness: passed for text-only Qwen/Ollama.
- RAG readiness: passed.
- Source/API/model capability audit: completed.
- Dataset/evaluation/training immutability: preserved.

The full backend/AI/RAG test suites were not rerun in this milestone because the required existing vision capability is absent and no safe implementation target exists. No backend or AI source changes were made.

## Remaining Blocker

Restore or expose the existing approved vision model and its image input endpoint/configuration in the current deployment. Until that exact capability is available, completing `IMAGE -> OBSERVATIONS -> RAG -> EVIDENCE -> ROOT CAUSE -> RECOMMENDATION` would require adding a new capability, which this milestone explicitly forbids.

