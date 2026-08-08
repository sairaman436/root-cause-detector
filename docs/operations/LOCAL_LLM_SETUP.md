# Local LLM Setup

## Purpose

This runbook explains how to run local Qwen inference through Ollama for root-cause analysis.

## Why It Exists

The platform must support sovereign, offline-friendly, locally hosted AI execution. Local Ollama integration gives developers and operators a concrete path for validating real model inference without relying on cloud providers.

## Architecture Fit

The local model is reached only through the Python AI inference service:

```text
Core Backend -> AI Inference Service -> AIProvider -> OllamaProvider -> Qwen
```

Business modules never call Ollama directly.

## Environment Variables

| Variable                       | Required | Description                                                                          |
| ------------------------------ | -------- | ------------------------------------------------------------------------------------ |
| `LLM_PROVIDER`                 | Yes      | Use `ollama` for local inference.                                                    |
| `LLM_MODEL`                    | Yes      | Ollama model tag, for example `qwen2.5:0.5b`.                                        |
| `OLLAMA_BASE_URL`              | Yes      | Base URL for Ollama. In Docker Compose use `http://ollama:11434`.                    |
| `AI_INFERENCE_SERVICE_URL`     | Yes      | Backend-to-AI-service URL. In Docker Compose use `http://ai-inference-service:8101`. |
| `AI_INFERENCE_TIMEOUT_SECONDS` | Yes      | Backend timeout for local inference requests.                                        |
| `LLM_REQUEST_TIMEOUT_SECONDS`  | Yes      | AI service timeout for Ollama generation.                                            |

## Local Docker Workflow

Start the stack:

```powershell
docker compose up -d --build
```

Pull the configured Qwen model:

```powershell
docker compose exec ollama ollama pull qwen2.5:0.5b
```

Verify Ollama sees the model:

```powershell
docker compose exec ollama ollama list
```

Check AI service provider health:

```powershell
Invoke-RestMethod http://localhost:8101/v1/provider/health
```

The response should show:

- `provider: ollama`
- `configured_model: qwen2.5:0.5b`
- `model_available: true`

## Backend API

Run root-cause analysis through the backend:

```text
POST /api/v1/ai/analysis/root-cause
```

Request body:

```json
{
  "surveyId": "survey-uuid",
  "submissionId": "submission-uuid",
  "problem": "Village households report unreliable water access and delayed repairs.",
  "modelId": "qwen2.5:0.5b",
  "evidenceIds": ["evidence-uuid"],
  "citations": []
}
```

Successful responses include:

- Analysis ID
- Request ID
- Provider
- Model
- Prompt ID
- Prompt version
- Latency
- Token estimate
- Strict structured analysis output

## Troubleshooting

| Symptom                           | Likely Cause                                       | Resolution                                                                      |
| --------------------------------- | -------------------------------------------------- | ------------------------------------------------------------------------------- |
| `OLLAMA_MODEL_UNAVAILABLE`        | Qwen model has not been pulled.                    | Run `docker compose exec ollama ollama pull qwen2.5:0.5b`.                      |
| `OLLAMA_UNAVAILABLE`              | Ollama container is stopped or unreachable.        | Run `docker compose ps ollama` and restart the stack.                           |
| `OLLAMA_TIMEOUT`                  | Model generation exceeded timeout.                 | Increase `LLM_REQUEST_TIMEOUT_SECONDS` and verify host resources.               |
| `LLM_INVALID_STRUCTURED_OUTPUT`   | Model returned JSON that failed the strict schema. | Retry with clearer evidence/context or inspect AI service logs.                 |
| Backend `AI_PROVIDER_UNAVAILABLE` | Backend cannot reach the AI inference service.     | Verify `AI_INFERENCE_SERVICE_URL` and `docker compose ps ai-inference-service`. |

## Security Notes

- Do not expose Ollama directly to untrusted networks.
- Do not log raw prompts or full survey/evidence bodies.
- Treat uploaded evidence and survey responses as sensitive operational data.
- Keep model and provider configuration environment-driven.
- Use the backend API as the only supported caller-facing inference entry point.
