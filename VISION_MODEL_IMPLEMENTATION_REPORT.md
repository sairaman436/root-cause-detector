# Vision Model Implementation Report

## Status

The real local vision capability is implemented and deployed locally. The
vision endpoint, observation contract, authenticated access, RAG handoff,
root-cause persistence, recommendation persistence, and fail-closed behavior
are present. The final post-threshold live rerun was blocked by a RAG request
timeout after the Docker CLI became unresponsive during the long backend test
container run; no successful result is claimed for that latest rerun.

## Selected Model

- Model: `moondream:1.8b`
- Runtime: Ollama `0.5.7`
- Architecture reported by Ollama: `phi2` with a CLIP projector
- Quantization reported by Ollama: `Q4_0`
- Model size reported by Ollama: approximately `1.7 GB`
- License reported by Ollama: Apache 2.0
- Image support: Ollama multimodal image input
- Deployment hardware: NVIDIA RTX 3050 Laptop GPU, 6 GB VRAM
- Docker GPU allocation: enabled with Compose `gpus: all`

This model was selected because it is materially smaller than general
multimodal models and was observed running on the available GPU. No second
vision model was downloaded.

## Implemented Path

```text
Authenticated browser
  -> POST /api/v1/ai/vision/analyze (multipart image)
  -> Spring validation and AI-service client
  -> POST /v1/vision/analyze
  -> Ollama moondream:1.8b
  -> validated visual observations
  -> governed RAG query
  -> citation/evidence validation
  -> root-cause analysis persistence
  -> recommendation generation with human approval required
```

The browser never calls Ollama directly. Image bytes are sent through the
authenticated backend boundary and are not persisted by the vision service.

## API Contract

`POST /api/v1/ai/vision/analyze`

- Authentication: required
- Authorization: `SERVING_INFER`, `AI_OPERATOR`, or `AI_ADMIN`
- Request: multipart `image`, optional `question`
- Accepted MIME types: JPEG, PNG, WEBP
- Image size limit: configured 50 MiB maximum

Validated response minimum:

```json
{
  "observations": [
    {
      "description": "...",
      "type": "visual_observation"
    }
  ],
  "question": "...",
  "uncertainty": "..."
}
```

Bounding boxes and confidence values are not emitted because this model did
not provide those fields in the tested response.

## Real Agriculture Image Test

Image: real agriculture photograph of crop leaves with visible spotting and
plant stress, stored locally at test time as `csp-agriculture.jpg`.

Observed response from `moondream:1.8b` included a visual observation of
green crop vegetation and leaf discoloration/spotting. The model explicitly
returned uncertainty that the image did not establish a diagnosis or cause.

The successful authenticated trace persisted:

- Root-cause analysis ID: `31d8080e-cdf6-4948-a812-f6c0402c6d9e`
- Recommendation set ID: `291e5dcd-29b7-4dfa-bed5-ab9e76540ddf`
- Vision observations: 1
- Governed RAG citations: 5 returned in the successful trace
- Validated root causes: 2 in the successful trace
- Recommendation options: 6 in the successful trace
- Root-cause and recommendation records: human review required

The successful trace was executed before the final portal-side evidence
threshold change. It proves the real model and backend integration path, but
its recommendation allowlist was broader than the final thresholded portal
path. The stricter source filtering is now implemented and covered by tests.

## Telemetry

Observed during successful calls:

- Vision service latency: approximately `876 ms` to `1,507 ms`
- Vision HTTP round trip: approximately `1,487 ms` in one trace
- RAG retrieval latency: approximately `1,930 ms` to `2,074 ms` in successful traces
- GPU process state: `moondream:1.8b` at `100% GPU`
- GPU snapshot during inspection: `5,415 MiB / 6,144 MiB`, `23%` utilization at the sampled instant
- AI-service `gpu_memory` field: `null`, because `nvidia-smi` is not installed in that container

The telemetry values are point observations, not benchmarks.

## Safety and Failure Behavior

Verified or implemented behavior:

- Unauthenticated backend vision access returns `401`.
- Unsupported image MIME type returns `415` with `VISION_UNSUPPORTED_IMAGE`.
- Vision provider unavailability maps to `Vision analysis unavailable.`.
- Invalid model output maps to `Vision analysis could not be validated.`.
- Empty or invalid governed retrieval stops the workflow with `Insufficient governed evidence.`.
- Recommendation generation requires a validated root cause and human approval.
- Vision observations are passed as observations/context, not as governed evidence.
- Recommendations receive only the final governed citation allowlist; unrelated retrieved results are filtered out.

## Frontend

The AI Inspection Lab now includes:

- Image picker and preview
- Replace/remove image controls
- Actual model observations
- Actual retrieval query
- Retrieved evidence and citations
- Root cause and uncertainty
- Recommendations and source trace
- Stage-by-stage failure states

The portal container was rebuilt and reported healthy. TypeScript, production
build, and frontend tests passed.

## Tests and Builds

- AI inference service tests: `13 passed`
- RAG service tests: `8 passed`
- Frontend tests: `2 passed`
- Frontend typecheck: passed
- Frontend production build: passed
- AI-service Python compilation: passed
- Core backend Docker build: passed
- Focused backend recommendation tests: `7 passed`
- AI service health: passed
- RAG service health: passed
- Web portal health: passed
- Full backend Maven test suite: did not complete within the 15-minute timeout; no pass is claimed

## Remaining Limitations

1. The final thresholded portal-side rerun timed out at the live RAG request
   after the Docker CLI became unresponsive. Re-run the complete flow after
   restarting the local Docker/Compose stack before treating the milestone as
   operationally closed.
2. Container-level GPU memory telemetry is unavailable inside the AI-service
   container; host/Ollama telemetry remains available.
3. Vision output is descriptive only. It is not a diagnosis and does not
   produce coordinates or calibrated confidence.
4. The local governed knowledge index contains controlled evaluation records.
   This report does not treat those records as real village measurements.
5. No datasets, evaluation sets, base models, or fine-tuning artifacts were
   modified by this milestone.
