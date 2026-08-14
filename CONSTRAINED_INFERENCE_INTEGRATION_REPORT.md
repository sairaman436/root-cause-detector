# Constrained Inference Integration Report

Purpose: Records the production-path integration and validation of constrained v0.3 inference.

Why it exists: The local Qwen service must reject unconstrained v0.3 generation and preserve the canonical schema and source-ID citation contract across RAG and root-cause requests.

Architecture fit: Outlines constrains the existing Ollama/Qwen provider at the AI inference boundary. RAG remains responsible for retrieval and validates the constrained answer before returning its existing public response shape. No model, dataset, backend API, or database schema was changed.

## Files Changed

- `docker-compose.yml`
- `services/ai-inference-service/pyproject.toml`
- `services/ai-inference-service/src/ai_inference_service/main.py`
- `services/ai-inference-service/src/ai_inference_service/v03_contract.py`
- `services/ai-inference-service/tests/test_local_llm.py`
- `services/rag-service/src/rag_service/main.py`
- `CONSTRAINED_INFERENCE_INTEGRATION_REPORT.md`

## Integration Status

PASS after runtime upgrade and clean service restart.

- Outlines: `1.3.3`
- Ollama Python client: `0.6.2`
- Ollama server: `0.5.7`
- Model: `qwen2.5:0.5b`
- Schema compilation is cached by model, task, and source-ID-specific schema.
- Constrained generation is mandatory for v0.3 RAG and root-cause routes.
- Missing source IDs fail with `V03_SOURCE_IDS_REQUIRED`.
- No unconstrained model-generation fallback is used.
- RAG's existing deterministic evidence summary remains only as an explicitly logged provider-outage degradation path; it is not unconstrained model output.

The original local Ollama image was `0.3.4`. It rejected the JSON Schema object sent by Outlines with HTTP 400. The compose pin was updated to `0.5.7`, which supports structured JSON Schema format requests.

## Latency

Clean sequential root-cause API smoke after restarting Ollama and the AI service:

| Measurement | Result |
|---|---:|
| First call, HTTP elapsed | 5,971 ms |
| First call, service latency | 5,875 ms |
| Warm call, HTTP elapsed | 5,454 ms |
| Warm call, service latency | 5,449 ms |
| Two-call average, service latency | 5,662 ms |

The warm call reused the provider client and compiled schema cache. Earlier timeout/502 observations came from stale requests left by pre-fix smoke attempts; they were cleared by restarting only the local Ollama and AI service containers. The clean validation run completed both calls without errors.

RAG end-to-end query:

- HTTP elapsed: `6,147 ms`
- Constrained inference latency: `6,046 ms`
- Retrieved citations: `3`
- Citation validation: `VALIDATED`
- Support status: `SUPPORTED`

## Contract Results

- Root-cause structured output: `2/2` clean sequential API calls valid.
- Root-cause canonical contract version: `dataset-v0.3`.
- Root-cause source-ID citation compliance: `2/2`.
- Root-cause evidence source IDs matched the supplied source ID.
- RAG constrained output and parsing: `1/1`.
- RAG source IDs matched retrieved citation IDs: `1/1`.
- RAG public response preserved the existing `RagResponse` shape.
- Repair attempts: `0` in the clean smoke calls.
- No markdown or extra unconstrained text was accepted by the v0.3 boundary.

## Resource Usage

- Host GPU snapshot during validation: `7 MB / 6,144 MB` reported by `nvidia-smi`.
- Ollama logs show the local Qwen runner using CPU buffers; the compose service does not expose `nvidia-smi` telemetry.
- AI-service response GPU telemetry is therefore `null`, not fabricated.
- A GPU-enabled serving deployment remains a separate infrastructure task.

## Tests

- AI inference tests: `11 passed`.
- RAG service tests: `5 passed`.
- v0.3 contract tests: `11 passed`.
- Python compile check: passed.
- `git diff --check`: passed.
- Docker builds: AI inference and RAG images built successfully.
- Container health: AI inference and RAG healthy; Ollama running on `0.5.7`.
- Live RAG retrieval-to-Qwen smoke: passed with validated citations.

The API-level smoke used a non-persistent survey/evidence payload and existing indexed RAG material. No PostgreSQL, survey, evidence, dataset, or held-out test data was modified. The recommendation component remains the existing backend workflow; this change only replaces the model-generation boundary it consumes.

## Remaining Blockers

- GPU memory telemetry is unavailable inside the current CPU-oriented Ollama container.
- The existing RAG outage degradation is deterministic and logged, but it does not produce a canonical model response when Qwen is unavailable.
- Full authenticated browser-to-backend workflow validation was not required for this service-boundary change.
