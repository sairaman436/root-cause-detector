"""
Purpose: Provides the Sprint 1 notification service.
Why it exists: The platform needs an executable notification boundary for report and workflow events even when external providers are not configured.
Architecture fit: Preserves notification delivery as a separate service with a local audit-safe provider.
"""

import os
import time
from collections.abc import AsyncIterator
from contextlib import asynccontextmanager
from typing import Any
from uuid import uuid4

import structlog
from fastapi import FastAPI
from pydantic import BaseModel, Field

SERVICE_NAME = "notification-service"
STARTED_AT = time.time()
DELIVERIES: list[dict[str, Any]] = []

structlog.configure(
    processors=[
        structlog.processors.add_log_level,
        structlog.processors.TimeStamper(fmt="iso"),
        structlog.processors.JSONRenderer(),
    ]
)
logger = structlog.get_logger(service=SERVICE_NAME)


class NotificationRequest(BaseModel):
    """Notification delivery request."""

    channel: str = Field(default="in_app", max_length=40)
    recipient: str = Field(min_length=1, max_length=254)
    subject: str = Field(min_length=1, max_length=220)
    body: str = Field(min_length=1, max_length=4000)


@asynccontextmanager
async def lifespan(_: FastAPI) -> AsyncIterator[None]:
    logger.info("service_started", environment=os.getenv("APP_ENV", "local"))
    yield


app = FastAPI(
    title="Notification Service",
    description="Sprint 1 notification service with local auditable delivery records.",
    version="0.2.0",
    lifespan=lifespan,
)


@app.get("/health")
def health() -> dict[str, Any]:
    return {
        "service": SERVICE_NAME,
        "status": "ok",
        "uptime_seconds": round(time.time() - STARTED_AT, 3),
        "deliveries": len(DELIVERIES),
    }


@app.get("/health/live")
def live() -> dict[str, str]:
    return {"service": SERVICE_NAME, "status": "live"}


@app.get("/health/ready")
def ready() -> dict[str, str]:
    return {"service": SERVICE_NAME, "status": "ready"}


@app.post("/v1/notifications")
def send(request: NotificationRequest) -> dict[str, Any]:
    delivery = {
        "id": str(uuid4()),
        "channel": request.channel,
        "recipient": request.recipient,
        "subject": request.subject,
        "status": "delivered_local",
        "createdAt": time.time(),
    }
    DELIVERIES.append(delivery)
    logger.info("notification_delivered_local", channel=request.channel, recipient=request.recipient)
    return delivery


@app.get("/v1/notifications")
def list_deliveries() -> dict[str, Any]:
    return {"deliveries": DELIVERIES}
