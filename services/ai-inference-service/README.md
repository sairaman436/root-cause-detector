# AI Inference Service

## Purpose

Hosts the Python FastAPI local LLM inference boundary for provider-neutral model calls, structured root-cause analysis, prompt registry access, health checks, and streaming.

## Why It Exists

The approved architecture separates AI runtime concerns from the Spring Boot transactional backend. The backend calls this service; this service owns provider adapters such as Ollama.

## Architecture Fit

This service implements the local Ollama/Qwen adapter behind the `AIProvider` contract. It validates strict rural analysis output before the backend persists successful analysis results.

## Local Ollama Configuration

Required runtime variables:

| Variable                      | Description                                   |
| ----------------------------- | --------------------------------------------- |
| `LLM_PROVIDER`                | Provider selector. Use `ollama`.              |
| `LLM_MODEL`                   | Ollama model tag, for example `qwen2.5:0.5b`. |
| `OLLAMA_BASE_URL`             | Ollama base URL.                              |
| `LLM_CONNECT_TIMEOUT_SECONDS` | Timeout for model availability checks.        |
| `LLM_REQUEST_TIMEOUT_SECONDS` | Timeout for generation requests.              |
| `LLM_MAX_RETRIES`             | Retry count for transient provider failures.  |

## Provider Contract

The provider interface supports:

- `generate`
- `chat`
- `structured_generate`
- `stream`
- `health`

## Endpoints

- `GET /health`
- `GET /health/live`
- `GET /health/ready`
- `GET /v1/provider/health`
- `GET /v1/prompts`
- `POST /v1/inference`
- `POST /v1/analysis/root-cause`
- `POST /v1/stream`

## Local Setup

With Docker Compose running:

```powershell
docker compose exec ollama ollama pull qwen2.5:0.5b
Invoke-RestMethod http://localhost:8101/v1/provider/health
```

See `docs/operations/LOCAL_LLM_SETUP.md` and `LOCAL_LLM_INTEGRATION_REPORT.md` for the full operational flow.
