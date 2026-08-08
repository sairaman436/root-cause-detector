# Local LLM Integration Report

## Purpose

This report documents the first real local LLM inference path for the Rural Intelligence Platform.

## Why It Exists

Sprint 1 previously had deterministic AI fallbacks that kept the platform executable when model services were unavailable. This milestone introduces a strict Ollama/Qwen path for root-cause analysis so the platform can prove real local model execution while still keeping providers replaceable.

## Architecture Fit

The canonical runtime path is:

```text
Survey workflow
-> Core Backend AI REST API
-> LocalLlmAnalysisService
-> AI inference service
-> AIProvider interface
-> OllamaProvider adapter
-> Local Qwen model
-> Strict structured output validator
-> ai.llm_analysis_results
-> Web dashboard
```

The backend does not call Ollama directly. Ollama details stay inside `services/ai-inference-service`, behind the provider interface.

## Implemented Components

- Provider-neutral `AIProvider` contract supporting `generate`, `chat`, `structured_generate`, `stream`, and `health`.
- `OllamaProvider` adapter with configurable base URL, model, timeout, retries, health check, model availability detection, JSON generation, and streaming.
- Versioned prompt registry with `ROOT_CAUSE_ANALYSIS` and `RECOMMENDATION_GENERATION`.
- Strict rural analysis schema:
  - `problem`
  - `summary`
  - `contributing_factors`
  - `root_causes`
  - `evidence`
  - `confidence`
  - `recommendations`
  - `limitations`
- Backend API: `POST /api/v1/ai/analysis/root-cause`.
- Durable metadata table: `ai.llm_analysis_results`.
- Dashboard display for local Qwen analysis.
- Tests for provider health, prompt versioning, unavailable model, malformed output, valid structured output, failure persistence, and backend service integration.

## Configuration

| Variable                       | Purpose                                                | Local Default                                |
| ------------------------------ | ------------------------------------------------------ | -------------------------------------------- |
| `LLM_PROVIDER`                 | Selects the provider adapter.                          | `ollama`                                     |
| `LLM_MODEL`                    | Selects the local Qwen model tag.                      | `qwen2.5:0.5b`                               |
| `OLLAMA_BASE_URL`              | Ollama HTTP endpoint used by the AI inference service. | `http://ollama:11434` in Docker              |
| `AI_INFERENCE_SERVICE_URL`     | Backend URL for the Python AI inference boundary.      | `http://ai-inference-service:8101` in Docker |
| `AI_INFERENCE_TIMEOUT_SECONDS` | Backend read timeout for long local LLM calls.         | `130`                                        |
| `LLM_CONNECT_TIMEOUT_SECONDS`  | AI service timeout for Ollama health/model checks.     | `3`                                          |
| `LLM_REQUEST_TIMEOUT_SECONDS`  | AI service timeout for generation calls.               | `120`                                        |
| `LLM_MAX_RETRIES`              | Ollama request retry attempts after the first call.    | `1`                                          |

## Failure Handling

The local LLM path does not convert provider failures into synthetic success.

| Failure               | Behavior                                                                  |
| --------------------- | ------------------------------------------------------------------------- |
| Ollama unavailable    | Returns `AI_PROVIDER_UNAVAILABLE` or provider detail from the AI service. |
| Model unavailable     | Returns `OLLAMA_MODEL_UNAVAILABLE` with the pull command.                 |
| Timeout               | Returns `OLLAMA_TIMEOUT` or `AI_PROVIDER_UNAVAILABLE`.                    |
| Empty response        | Returns `LLM_EMPTY_RESPONSE`.                                             |
| Malformed JSON        | Returns `LLM_INVALID_STRUCTURED_OUTPUT`.                                  |
| Schema violation      | Rejected before persistence as a successful result.                       |
| Server/provider error | Recorded as `FAILED` in `ai.llm_analysis_results`.                        |

## Security Review

- User survey and evidence text are treated as data inside the prompt.
- Prompt registry system instructions explicitly block treating user content as instructions.
- The backend stores validated analysis output and metadata, not raw prompts.
- Provider logs include provider, model, latency, prompt ID, and request ID, not full survey prompt text.
- Ollama remains internal to Docker networking and is not exposed through the public backend API.
- Output is validated in the AI service and again in the backend before a successful persistence record is created.

## Observability

Captured metadata per analysis:

- Provider
- Model
- Model version when available
- Prompt ID
- Prompt version
- Request ID
- Latency
- Token estimate
- Status
- Error code and sanitized error message for failures

The AI service emits structured logs for service start, inference success, structured analysis success, and provider failures.

## Validation Performed

- `python -m pytest services/ai-inference-service/tests`: passed, 6 tests.
- `.\mvnw.cmd -pl services/core-backend -am -Dtest=LocalLlmAnalysisIntegrationTests "-Dsurefire.failIfNoSpecifiedTests=false" test`: passed, 3 tests.

## Local Acceptance Procedure

1. Start the platform:

   ```powershell
   docker compose up -d --build
   ```

2. Pull the configured Qwen model:

   ```powershell
   docker compose exec ollama ollama pull qwen2.5:0.5b
   ```

3. Confirm the model is available:

   ```powershell
   docker compose exec ollama ollama list
   ```

4. Use the web dashboard at `http://localhost:3000`:

   - Login or register.
   - Create and publish a survey.
   - Submit a survey response.
   - Upload evidence.
   - Run AI Analysis.
   - Confirm the Local Qwen Analysis panel shows validated JSON, provider `ollama`, the configured model, prompt version, latency, and confidence.

## Performance Baseline Method

For each local machine, capture:

- Cold request latency: first request after starting Ollama and backend.
- Warm request latency: second and third requests using the same model.
- Token estimate: backend response field `tokensEstimate`.
- Model memory: `docker stats` for the `ollama` container during generation.
- CPU/GPU usage: local host tooling or Docker Desktop resource metrics.

This report does not fabricate hardware-specific numbers. The baseline must be recorded on the target workstation after the configured Qwen model is pulled.
