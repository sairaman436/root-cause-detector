# Live Multimodal RAG Inspection Report

## Scope

This milestone audited the live image-to-decision path and added the portal inspection surface without changing datasets, evaluation sets, training artifacts, governance records, or backend API contracts.

## Capability Audit

The current platform does **not** have a genuine image-understanding path.

| Stage | Current state |
| --- | --- |
| Evidence image storage | Supported by the core backend evidence module for image MIME types. |
| Vision model | Not configured. The only installed Ollama model is `qwen2.5:0.5b`. |
| Model capability | Ollama reports Qwen2 text generation (`qwen2`, `text-generation`, `Q4_K_M`); no image input capability is exposed. |
| AI input contract | `POST /v1/inference` accepts a text `prompt`, context and citations; it has no image or multipart field. |
| Image preprocessing | No image preprocessing or image-to-text adapter exists. |
| OCR | Not implemented in the active AI/RAG path. |
| Object/region detection | Not implemented; no bounding boxes or localization are available. |
| RAG input | Text queries and governed evidence only. It cannot receive an image directly. |

The real missing boundary is:

`image -> vision-capable provider / OCR -> structured observations -> text retrieval query`

Without that boundary, sending image bytes to the current Qwen endpoint would be invalid and generating observations in the portal would be misleading.

## Portal Implementation

Added an `AI Inspection Lab` screen to the existing web portal. It provides:

- drag-and-drop image selection;
- browser-local preview;
- image replacement and removal;
- optional question input;
- an `Analyze Image` capability check;
- explicit processing, blocked, empty, and invalid-file states;
- pipeline stages for Upload, Vision, Observations, Retrieval, Evidence Validation, Root Cause, Recommendation, and Source Trace;
- separate `MODEL OBSERVATIONS`, `EVIDENCE`, `INFERENCE`, and `RECOMMENDATION` sections;
- an expandable technical trace with model, endpoint, capability, query, source IDs, validation, and warning state.

The selected image is not persisted or sent to the backend. Clicking `Analyze Image` stops at the vision gate and reports the active text-only capability. No observations, evidence, citations, root cause, or recommendation are fabricated.

## Actual Runtime Verification

Verified local services:

- Web portal: `GET http://localhost:3000/api/health` returned HTTP 200 and `{"service":"web-portal","status":"ok"}`.
- AI inference: `GET http://localhost:8101/health/ready` returned HTTP 200 with provider/model ready.
- RAG: `GET http://localhost:8102/health/ready` returned HTTP 200 with Qdrant ready.
- Ollama model inspection: `qwen2.5:0.5b` is installed and reports a Qwen2 text-generation model.
- AI OpenAPI inspection: the active service exposes `/v1/inference`, but no image or multimodal endpoint.

The previously referenced temporary screenshot file was not available in the current attachment/workspace paths, so an actual image upload could not be reproduced from that artifact. The implemented UI path was verified by build and type checks; no image-to-model result is claimed.

Expected truthful failure behavior for an available image is:

1. The image is selected and previewed locally.
2. Upload is shown as selected locally, not permanently stored.
3. Vision fails with the text-only provider message.
4. Observations, retrieval, evidence validation, root cause, recommendation, and source trace remain blocked/pending.
5. No downstream request is made and no fallback output is shown.

## Validation

Passed:

- `npm run typecheck` in `apps/web-portal`.
- `npm run lint` in `apps/web-portal`.
- `npm test -- --run` in `apps/web-portal` (2 tests passed).
- `npm run build` in `apps/web-portal`.
- Docker build for `web-portal`.
- Recreated only the `web-portal` container and verified its health endpoint.
- AI and RAG readiness checks.

The production AI/RAG container images do not include `pytest`, so container-level pytest execution was not available without changing the production images. Existing host-side AI/RAG test evidence remains outside this milestone; no backend or RAG source was changed here.

## Remaining Blocker

Real multimodal inspection requires a governed vision-capable provider and an explicit image inference contract, including image transport, preprocessing/OCR policy, structured observation schema, provenance handling, and downstream observation-to-RAG mapping. That capability must be added and validated before the lab can honestly execute Vision -> Observations -> RAG -> Root Cause -> Recommendation.

