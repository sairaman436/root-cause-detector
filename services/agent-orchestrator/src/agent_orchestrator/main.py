"""
Purpose: Provides the Sprint 1 agent orchestration service.
Why it exists: The MVP needs a testable orchestration boundary that sequences survey, evidence, RAG, prediction, recommendation, and reporting steps.
Architecture fit: Preserves the multi-agent boundary while keeping authoritative persistence in the backend.
"""

import os
import time
from collections.abc import AsyncIterator
from contextlib import asynccontextmanager
from typing import Any

import structlog
from fastapi import FastAPI
from pydantic import BaseModel, Field

SERVICE_NAME = "agent-orchestrator"
STARTED_AT = time.time()

structlog.configure(
    processors=[
        structlog.processors.add_log_level,
        structlog.processors.TimeStamper(fmt="iso"),
        structlog.processors.JSONRenderer(),
    ]
)
logger = structlog.get_logger(service=SERVICE_NAME)


class OrchestrationRequest(BaseModel):
    """Request describing an MVP decision workflow to coordinate."""

    survey_id: str = Field(min_length=1)
    evidence_ids: list[str] = Field(default_factory=list)
    objective: str = Field(min_length=1, max_length=1000)


@asynccontextmanager
async def lifespan(_: FastAPI) -> AsyncIterator[None]:
    logger.info("service_started", environment=os.getenv("APP_ENV", "local"))
    yield


app = FastAPI(
    title="Agent Orchestrator",
    description="Sprint 1 orchestration service for the integrated MVP workflow.",
    version="0.2.0",
    lifespan=lifespan,
)


@app.get("/health")
def health() -> dict[str, Any]:
    return {
        "service": SERVICE_NAME,
        "status": "ok",
        "uptime_seconds": round(time.time() - STARTED_AT, 3),
    }


@app.get("/health/live")
def live() -> dict[str, str]:
    return {"service": SERVICE_NAME, "status": "live"}


@app.get("/health/ready")
def ready() -> dict[str, str]:
    return {"service": SERVICE_NAME, "status": "ready"}


@app.post("/v1/orchestrate")
def orchestrate(request: OrchestrationRequest) -> dict[str, Any]:
    steps = [
        {"agent": "Survey Agent", "action": "load_survey", "status": "ready", "target": request.survey_id},
        {"agent": "Knowledge Agent", "action": "retrieve_citations", "status": "ready"},
        {"agent": "Prediction Agent", "action": "run_inference", "status": "ready"},
        {"agent": "Recommendation Agent", "action": "rank_actions", "status": "ready"},
        {"agent": "Report Agent", "action": "generate_exports", "status": "ready"},
    ]
    logger.info("workflow_orchestrated", survey_id=request.survey_id, evidence_count=len(request.evidence_ids))
    return {
        "surveyId": request.survey_id,
        "objective": request.objective,
        "evidenceCount": len(request.evidence_ids),
        "steps": steps,
        "status": "orchestrated",
    }
