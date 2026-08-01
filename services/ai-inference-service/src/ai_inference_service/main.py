"""
Purpose: Provides the Sprint 1 AI inference service.
Why it exists: The platform needs a testable model-serving boundary that can use Ollama when available and deterministic local inference when it is not.
Architecture fit: Preserves prediction as a separate AI service boundary behind the backend AI gateway.
"""

import json
import os
import time
import urllib.error
import urllib.request
from collections.abc import AsyncIterator
from contextlib import asynccontextmanager
from typing import Any

import structlog
from fastapi import FastAPI
from pydantic import BaseModel, Field

SERVICE_NAME = "ai-inference-service"
STARTED_AT = time.time()
DEFAULT_MODEL = os.getenv("AI_DEFAULT_MODEL", "qwen2.5-local")
OLLAMA_URL = os.getenv("OLLAMA_URL", "http://localhost:11434")

structlog.configure(
    processors=[
        structlog.processors.add_log_level,
        structlog.processors.TimeStamper(fmt="iso"),
        structlog.processors.JSONRenderer(),
    ]
)
logger = structlog.get_logger(service=SERVICE_NAME)


class InferenceRequest(BaseModel):
    """Inference request accepted by the local serving boundary."""

    prompt: str = Field(min_length=1, max_length=12000)
    task_type: str = Field(default="root_cause_analysis", max_length=80)
    model: str | None = None
    context: dict[str, Any] = Field(default_factory=dict)
    require_json: bool = True


class InferenceResponse(BaseModel):
    """Structured inference response returned to platform callers."""

    model: str
    provider: str
    task_type: str
    output: str
    structured_output: dict[str, Any]
    latency_ms: int
    tokens_estimate: int
    fallback_used: bool


@asynccontextmanager
async def lifespan(_: FastAPI) -> AsyncIterator[None]:
    logger.info("service_started", environment=os.getenv("APP_ENV", "local"))
    yield


app = FastAPI(
    title="AI Inference Service",
    description="Sprint 1 inference service with Ollama integration and deterministic local fallback.",
    version="0.2.0",
    lifespan=lifespan,
)


@app.get("/health")
def health() -> dict[str, Any]:
    return {
        "service": SERVICE_NAME,
        "status": "ok",
        "uptime_seconds": round(time.time() - STARTED_AT, 3),
        "default_model": DEFAULT_MODEL,
    }


@app.get("/health/live")
def live() -> dict[str, str]:
    return {"service": SERVICE_NAME, "status": "live"}


@app.get("/health/ready")
def ready() -> dict[str, str]:
    return {"service": SERVICE_NAME, "status": "ready"}


@app.post("/v1/inference", response_model=InferenceResponse)
def inference(request: InferenceRequest) -> InferenceResponse:
    started = time.time()
    model = request.model or DEFAULT_MODEL
    provider = "ollama"
    output, fallback = _ollama_generate(model, request.prompt)
    if fallback:
        provider = "deterministic-local"
        output = _deterministic_analysis(request.prompt, request.context)
    structured = _structured_output(request.task_type, output, request.context)
    latency_ms = int((time.time() - started) * 1000)
    logger.info("inference_completed", model=model, provider=provider, latency_ms=latency_ms, fallback=fallback)
    return InferenceResponse(
        model=model,
        provider=provider,
        task_type=request.task_type,
        output=output,
        structured_output=structured,
        latency_ms=latency_ms,
        tokens_estimate=max(1, len((request.prompt + " " + output).split())),
        fallback_used=fallback,
    )


def _ollama_generate(model: str, prompt: str) -> tuple[str, bool]:
    payload = json.dumps({"model": model.replace("-local", ""), "prompt": prompt, "stream": False}).encode()
    req = urllib.request.Request(
        f"{OLLAMA_URL.rstrip('/')}/api/generate",
        data=payload,
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(req, timeout=3) as response:
            body = json.loads(response.read().decode())
            generated = str(body.get("response", "")).strip()
            return (generated or _deterministic_analysis(prompt, {}), not bool(generated))
    except (urllib.error.URLError, TimeoutError, json.JSONDecodeError, OSError):
        return "", True


def _deterministic_analysis(prompt: str, context: dict[str, Any]) -> str:
    evidence_count = len(context.get("evidence_ids", [])) if isinstance(context.get("evidence_ids"), list) else 0
    focus = prompt.strip().split(".")[0][:180]
    return (
        "Root cause analysis indicates the primary issue is constrained service delivery capacity, "
        f"based on {evidence_count} evidence item(s) and the stated problem: {focus}. "
        "Recommended action is to validate field evidence, prioritize accountable local interventions, "
        "and track measurable outcomes through follow-up surveys."
    )


def _structured_output(task_type: str, output: str, context: dict[str, Any]) -> dict[str, Any]:
    confidence = 0.78 if context else 0.66
    return {
        "taskType": task_type,
        "rootCauses": [
            {
                "title": "Constrained service delivery capacity",
                "rationale": output,
                "confidence": confidence,
            }
        ],
        "recommendations": [
            {
                "title": "Run targeted field validation and intervention tracking",
                "priority": 1,
                "expectedImpact": "Improves accountability and closes evidence gaps before policy action.",
                "confidence": confidence,
            }
        ],
        "citations": context.get("citations", []) if isinstance(context.get("citations"), list) else [],
    }
