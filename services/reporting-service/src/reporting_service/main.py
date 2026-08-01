"""
Purpose: Provides the Sprint 1 reporting worker service.
Why it exists: The platform needs a report rendering boundary for asynchronous and future batch report generation.
Architecture fit: Complements the backend Reports bounded context without owning authoritative report persistence.
"""

import base64
import os
import time
from collections.abc import AsyncIterator
from contextlib import asynccontextmanager
from typing import Any

import structlog
from fastapi import FastAPI
from pydantic import BaseModel, Field

SERVICE_NAME = "reporting-service"
STARTED_AT = time.time()

structlog.configure(
    processors=[
        structlog.processors.add_log_level,
        structlog.processors.TimeStamper(fmt="iso"),
        structlog.processors.JSONRenderer(),
    ]
)
logger = structlog.get_logger(service=SERVICE_NAME)


class RenderRequest(BaseModel):
    """Report rendering request."""

    title: str = Field(min_length=1, max_length=220)
    summary: str = Field(min_length=1)
    rows: list[dict[str, Any]] = Field(default_factory=list)


@asynccontextmanager
async def lifespan(_: FastAPI) -> AsyncIterator[None]:
    logger.info("service_started", environment=os.getenv("APP_ENV", "local"))
    yield


app = FastAPI(
    title="Reporting Service",
    description="Sprint 1 reporting worker with CSV and text-backed PDF rendering.",
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


@app.post("/v1/render")
def render(request: RenderRequest) -> dict[str, Any]:
    csv = _csv(request.rows)
    pdf_text = f"{request.title}\n\n{request.summary}\n\n{csv}"
    pdf_bytes = ("%PDF-1.4\n% Sprint 1 text report\n" + pdf_text + "\n%%EOF\n").encode()
    logger.info("report_rendered", row_count=len(request.rows), title=request.title)
    return {
        "title": request.title,
        "csv": csv,
        "pdfBase64": base64.b64encode(pdf_bytes).decode(),
        "rowCount": len(request.rows),
    }


def _csv(rows: list[dict[str, Any]]) -> str:
    if not rows:
        return "section,title,value\nsummary,No rows,0\n"
    headers = sorted({key for row in rows for key in row})
    output = ",".join(headers) + "\n"
    for row in rows:
        output += ",".join(_escape(str(row.get(header, ""))) for header in headers) + "\n"
    return output


def _escape(value: str) -> str:
    return '"' + value.replace('"', '""') + '"'
