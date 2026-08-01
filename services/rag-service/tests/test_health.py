"""
Purpose: Verifies the RAG service health contract.
Why it exists: Protects the service health, indexing, and retrieval contracts.
Architecture fit: Supports CI and Docker Compose readiness checks for the RAG boundary.
"""

from fastapi.testclient import TestClient
from rag_service.main import app


def test_health_endpoint() -> None:
    response = TestClient(app).get("/health")

    assert response.status_code == 200
    assert response.json()["service"] == "rag-service"


def test_rag_query_returns_citations() -> None:
    client = TestClient(app)
    index_response = client.post(
        "/v1/documents",
        json={
            "source_id": "test-water-source",
            "title": "Village water access evidence",
            "text": "Village water failures are connected to bore well downtime and maintenance gaps.",
            "metadata": {"domain": "water"},
        },
    )
    assert index_response.status_code == 200

    response = client.post("/v1/query", json={"query": "water maintenance village", "top_k": 3})

    assert response.status_code == 200
    body = response.json()
    assert body["citations"]
    assert body["citations"][0]["source_id"] == "test-water-source"
