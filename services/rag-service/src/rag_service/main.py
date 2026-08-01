"""
Purpose: Provides the Sprint 1 RAG retrieval service.
Why it exists: The platform needs a testable knowledge retrieval boundary with citations for AI analysis.
Architecture fit: Preserves retrieval as a separate AI service boundary and keeps prediction outside RAG.
"""

import math
import os
import time
from collections import Counter
from collections.abc import AsyncIterator
from contextlib import asynccontextmanager
from typing import Any
from uuid import uuid4

import structlog
from fastapi import FastAPI
from pydantic import BaseModel, Field

SERVICE_NAME = "rag-service"
STARTED_AT = time.time()

structlog.configure(
    processors=[
        structlog.processors.add_log_level,
        structlog.processors.TimeStamper(fmt="iso"),
        structlog.processors.JSONRenderer(),
    ]
)
logger = structlog.get_logger(service=SERVICE_NAME)


class KnowledgeDocument(BaseModel):
    """Document indexed by the Sprint 1 retrieval service."""

    source_id: str = Field(default_factory=lambda: str(uuid4()))
    title: str = Field(min_length=1, max_length=240)
    text: str = Field(min_length=1)
    metadata: dict[str, Any] = Field(default_factory=dict)


class RagQuery(BaseModel):
    """Citation-preserving retrieval request."""

    query: str = Field(min_length=1, max_length=4000)
    top_k: int = Field(default=3, ge=1, le=10)


class Citation(BaseModel):
    """Retrieved citation returned to callers."""

    source_id: str
    title: str
    excerpt: str
    score: float
    metadata: dict[str, Any]


DOCUMENTS: list[KnowledgeDocument] = [
    KnowledgeDocument(
        source_id="policy-water-001",
        title="Rural water reliability policy note",
        text="Water access failures often combine infrastructure downtime, source depletion, and weak maintenance accountability.",
        metadata={"domain": "water", "sourceType": "policy"},
    ),
    KnowledgeDocument(
        source_id="health-service-001",
        title="Primary health service access note",
        text="Rural health access gaps frequently arise from staff availability, transport barriers, and low follow-up coverage.",
        metadata={"domain": "health", "sourceType": "policy"},
    ),
    KnowledgeDocument(
        source_id="livelihood-001",
        title="Livelihood intervention planning note",
        text="Livelihood outcomes improve when recommendations connect household evidence, local market access, and scheme eligibility.",
        metadata={"domain": "livelihood", "sourceType": "research"},
    ),
]


@asynccontextmanager
async def lifespan(_: FastAPI) -> AsyncIterator[None]:
    logger.info("service_started", environment=os.getenv("APP_ENV", "local"))
    yield


app = FastAPI(
    title="RAG Service",
    description="Sprint 1 retrieval service with in-memory indexing, hybrid keyword scoring, and citations.",
    version="0.2.0",
    lifespan=lifespan,
)


@app.get("/health")
def health() -> dict[str, Any]:
    return {
        "service": SERVICE_NAME,
        "status": "ok",
        "uptime_seconds": round(time.time() - STARTED_AT, 3),
        "documents": len(DOCUMENTS),
    }


@app.get("/health/live")
def live() -> dict[str, str]:
    return {"service": SERVICE_NAME, "status": "live"}


@app.get("/health/ready")
def ready() -> dict[str, str]:
    return {"service": SERVICE_NAME, "status": "ready"}


@app.post("/v1/documents", response_model=KnowledgeDocument)
def index_document(document: KnowledgeDocument) -> KnowledgeDocument:
    DOCUMENTS[:] = [existing for existing in DOCUMENTS if existing.source_id != document.source_id]
    DOCUMENTS.append(document)
    logger.info("document_indexed", source_id=document.source_id, title=document.title)
    return document


@app.post("/v1/query")
def query(request: RagQuery) -> dict[str, Any]:
    citations = [_citation(doc, request.query) for doc in DOCUMENTS]
    citations = sorted(citations, key=lambda citation: citation.score, reverse=True)[: request.top_k]
    citations = [citation for citation in citations if citation.score > 0]
    answer = (
        "Retrieved knowledge indicates that root causes should be validated against local evidence, "
        "service capacity, governance accountability, and policy eligibility."
        if citations
        else "No high-confidence citation matched the query; collect additional local evidence before acting."
    )
    logger.info("rag_query_completed", top_k=request.top_k, citations=len(citations))
    return {"answer": answer, "citations": [citation.model_dump() for citation in citations]}


def _citation(document: KnowledgeDocument, query_text: str) -> Citation:
    q = _tokens(query_text)
    d = _tokens(document.title + " " + document.text)
    overlap = sum((q & d).values())
    cosine = _cosine(q, d)
    score = round(min(1.0, (overlap / max(1, len(q))) * 0.6 + cosine * 0.4), 4)
    return Citation(
        source_id=document.source_id,
        title=document.title,
        excerpt=document.text[:320],
        score=score,
        metadata=document.metadata,
    )


def _tokens(text: str) -> Counter[str]:
    words = [token.strip(".,;:!?()[]{}\"'").lower() for token in text.split()]
    return Counter(word for word in words if len(word) > 2)


def _cosine(left: Counter[str], right: Counter[str]) -> float:
    common = set(left) & set(right)
    numerator = sum(left[token] * right[token] for token in common)
    left_norm = math.sqrt(sum(value * value for value in left.values()))
    right_norm = math.sqrt(sum(value * value for value in right.values()))
    if left_norm == 0 or right_norm == 0:
        return 0.0
    return numerator / (left_norm * right_norm)
