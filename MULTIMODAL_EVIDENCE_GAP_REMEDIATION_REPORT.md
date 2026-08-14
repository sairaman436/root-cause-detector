# Multimodal Evidence Gap Remediation Report

## Scope

This remediation hardens image input validation and performs only controlled live checks. No dataset, evaluation-set version, historical evidence record, model, review decision, or training artifact was modified. No evidence candidate was created because no new governed evidence was available for approval.

## Image Validation

The previous vision boundary trusted the multipart MIME header and accepted the payload without inspecting its bytes. `VisionImageUploadValidator` now runs before any provider request and:

- enforces a 10 MiB default vision-upload limit;
- enforces 4096 x 4096 maximum dimensions, configurable through `AI_VISION_MAX_IMAGE_BYTES`, `AI_VISION_MAX_IMAGE_WIDTH`, and `AI_VISION_MAX_IMAGE_HEIGHT`;
- verifies JPEG and PNG magic bytes;
- requires the declared content type to match the detected type;
- decodes the image through the JVM image reader;
- rejects empty, corrupt, unsupported, mismatched, and non-image content;
- passes a validated byte snapshot to the provider rather than rereading an unvalidated upload.

The exact invalid-content response is:

`Invalid image file. The uploaded content is not a supported image.`

Vision uploads are intentionally limited to JPEG and PNG because those formats have a verified decoder in the current backend runtime. WebP remains supported by the general evidence MIME configuration, but is rejected at the vision boundary until an explicitly supported decoder is added.

## Validation Results

| Case | Result |
| --- | --- |
| Valid JPEG | Accepted |
| Valid PNG | Accepted |
| HTML renamed to `.jpg` | Rejected as `VISION_INVALID_IMAGE` |
| Corrupt JPEG | Rejected as `VISION_INVALID_IMAGE` |
| Unsupported GIF | Rejected as `VISION_INVALID_IMAGE` |
| File-size overflow | Rejected as `VISION_IMAGE_TOO_LARGE` |
| Dimension overflow | Rejected as `VISION_IMAGE_DIMENSIONS_EXCEEDED` |

The live endpoint also rejected the original `csp-water-sanitation.jpg` before provider access. Its bytes begin with `<!DOCTYPE html>`, not a JPEG signature.

## Agriculture Evidence Gap

The live corpus contains seven active agriculture source IDs, including pest/disease, food-safety, and seed-storage material. The nearest sources describe governed pilot conditions and evidence gaps; they do not verify the specific yellow/brown crop condition in the inspected image.

The missing category is image-grounded agricultural condition evidence: a governed source that defines observable crop-stress or disease indicators, limits of visual identification, and supported follow-up actions with valid provenance and source IDs. No evidence was added automatically and the agriculture rerun remains correctly classified as `INSUFFICIENT GOVERNED EVIDENCE`.

## Healthcare Safety

The existing healthcare safeguards remain unchanged. Operational sources for appointments, facility hours, staffing, and referral access exist, but a building in an image does not establish that it is a clinic or that a person has a medical condition.

The pipeline continues to distinguish:

- visual observation;
- governed evidence;
- supported inference;
- recommendation.

Without governed evidence linking the observed image to a supported healthcare problem, recommendation generation remains blocked. No medical diagnosis was inferred.

## Livelihoods Root-Cause Analysis

The prior controlled run retrieved one livelihood source about supply-chain disruption, but the image observations did not establish a market or supply-chain problem. The root-cause service therefore returned no validated root cause and `VALIDATED_ROOT_CAUSE_REQUIRED` remained enforced.

This is an evidence/support gap, not a threshold failure. No replacement root cause was generated and no gate was weakened.

## Water/Sanitation Controlled Rerun

The corrupt HTML payload was not reused. A separate valid JPEG of a rural hand pump, with no identifiable person as the subject, was used only for this controlled evaluation. It was downloaded to a temporary path outside the repository from a public water-access image source and was not added to the knowledge corpus.

### Vision

- Model: `moondream:1.8b`
- Provider: Ollama
- Cold request: the client exceeded its 90-second diagnostic budget; the inference service later logged completion at 86,884 ms.
- Warm request: 699 ms
- Request ID: `81af9fab-f61d-4c2d-a0d7-f72343910d9b`
- Observation: “In this image, there is a water pump located in an open field with dry grass surrounding it. The area appears to be a dirt lot, which suggests that the ground might be rocky or uneven. There are no visible people or other objects nearby, and the sky above has some clouds scattered throughout.”
- Uncertainty: “The image does not establish a diagnosis or cause.”
- GPU telemetry: not returned by the API (`gpu_memory: null`)

### Input Rejection Regression

The original HTML-named-as-JPEG upload returned `VISION_INVALID_IMAGE` with the required message. Moondream was not called for that request.

### Retrieval

The first observation-derived query returned `INSUFFICIENT_EVIDENCE` with zero citations. A bounded water-domain query then retrieved:

- `approved-water-policy`, score `0.4643`, citation `7625635d-94af-4b8b-a068-a636e62c7988`: rural water reliability, bore-well maintenance, repair accountability, and village water committee monitoring;
- `PILOT_V05_WATER_WASTE_COLLECTION_RECOMMENDATION_GENERATION`, score `0.4245`, citation `77bfc51a-3e4b-494a-9e3f-695e459df724`: irregular waste collection and blocked drains near a market-side water point.

The second response reported structurally valid citations, but the retrieved material does not establish a specific fault in the photographed pump. The waste-collection record is a different governed pilot scenario and is not evidence for this image. No root-cause or recommendation was generated from these results.

### Final Water/Sanitation Result

`VALID IMAGE -> MOONDREAM OBSERVATION -> RAG RETRIEVAL -> EVIDENCE SPECIFICITY INSUFFICIENT -> NO RECOMMENDATION`

This is the expected fail-closed result. The image-validation defect is fixed; the remaining blocker is scenario-specific governed evidence for the observed water-point condition.

## Tests and Health Checks

- Backend focused AI tests: 14 passed.
- Python AI/RAG tests: 22 passed.
- Frontend tests: 7 passed.
- Frontend TypeScript typecheck: passed.
- Frontend production build: passed after clearing only the stale generated `.next` directory.
- Core backend local Maven package: passed.
- Backend health: HTTP 200.
- AI inference health: HTTP 200.
- RAG health: HTTP 200.
- Qdrant readiness: HTTP 200.
- Ollama tags endpoint: HTTP 200.
- Web portal health: HTTP 200.

The Docker rebuild was attempted but did not complete within the five-minute build window. It remained blocked inside the Dockerfile Maven dependency-resolution step while transferring `zstd-jni`; the existing backend container stayed healthy and was not replaced. This is an environment/build publication blocker, not an application test failure.

## Remaining Blockers

1. Add independently governed, scenario-relevant agriculture evidence through the approved evidence workflow before rerunning the agriculture image.
2. Keep healthcare recommendations blocked unless the image and governed evidence support a non-medical operational conclusion; never infer a diagnosis from appearance alone.
3. Keep the livelihoods validated-root-cause gate unchanged; obtain scenario-specific evidence before another run.
4. Add scenario-specific water-point evidence through governance before expecting a water recommendation.
5. Resolve the Docker Maven Central/`zstd-jni` build transfer stall before publishing the updated backend container image.
