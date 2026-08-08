"""
Purpose: Verifies the trusted RAG ingestion, retrieval, citation, and refusal contracts.
Why it exists: The platform must prove that answers depend on indexed evidence and fail closed when evidence is insufficient.
Architecture fit: Exercises the RAG service boundary without requiring Qdrant or Ollama in CI.
"""

from fastapi.testclient import TestClient
import pytest

from rag_service.main import app, knowledge_store


@pytest.fixture(autouse=True)
def reset_store() -> None:
    knowledge_store.reset()


def test_health_endpoint_reports_index_state() -> None:
    response = TestClient(app).get("/health")

    assert response.status_code == 200
    body = response.json()
    assert body["service"] == "rag-service"
    assert body["documents"] == 0
    assert body["chunks"] == 0


def test_ingestion_rejects_untrusted_documents() -> None:
    response = TestClient(app).post(
        "/v1/documents",
        json={
            "title": "Unapproved source",
            "source": "unknown-blog",
            "publisher": "Unknown",
            "text": "This should not enter the trusted corpus.",
        },
    )

    assert response.status_code == 403


def test_rag_query_returns_valid_citations_for_trusted_document() -> None:
    client = TestClient(app)
    index_response = client.post(
        "/v1/documents",
        json={
            "document_id": "doc-water-001",
            "title": "Village Water Maintenance Guidelines",
            "source": "approved-rural-policy-manual",
            "publisher": "Rural Development Department",
            "publication_date": "2025-01-10",
            "document_version": "1.0.0",
            "language": "en",
            "domain": "water",
            "document_type": "policy",
            "approved_source": True,
            "text": "# Bore Well Maintenance\nVillage water reliability failures often involve bore well downtime, weak maintenance accountability, and delayed pump repair.",
        },
    )
    assert index_response.status_code == 200
    assert index_response.json()["chunk_count"] >= 1

    response = client.post(
        "/v1/query",
        json={
            "query": "What causes village water reliability failures?",
            "top_k": 3,
            "filters": {"domain": "water", "language": "en"},
        },
    )

    assert response.status_code == 200
    body = response.json()
    assert body["support_status"] == "SUPPORTED"
    assert body["citation_validation_status"] == "VALIDATED"
    assert body["citations"]
    assert body["citations"][0]["document_id"] == "doc-water-001"
    assert "bore well downtime" in body["citations"][0]["excerpt"]


def test_rag_query_refuses_when_evidence_is_insufficient() -> None:
    client = TestClient(app)
    client.post(
        "/v1/documents",
        json={
            "document_id": "doc-health-001",
            "title": "Primary Health Availability",
            "source": "approved-health-manual",
            "publisher": "Health Department",
            "language": "en",
            "domain": "health",
            "document_type": "policy",
            "approved_source": True,
            "text": "Primary health access depends on staff availability and transport coverage.",
        },
    )

    response = client.post("/v1/query", json={"query": "How should irrigation insurance be priced?", "top_k": 2})

    assert response.status_code == 200
    body = response.json()
    assert body["support_status"] == "INSUFFICIENT_EVIDENCE"
    assert body["citations"] == []


def test_search_supports_metadata_filtering() -> None:
    client = TestClient(app)
    client.post(
        "/v1/documents",
        json={
            "document_id": "doc-agri-001",
            "title": "Agriculture Extension Note",
            "source": "approved-agriculture-university",
            "publisher": "Agriculture University",
            "language": "en",
            "domain": "agriculture",
            "document_type": "research",
            "approved_source": True,
            "text": "Soil testing improves crop planning and fertilizer recommendations.",
        },
    )

    response = client.post(
        "/v1/search",
        json={"query": "soil testing crop planning", "top_k": 5, "filters": {"domain": "agriculture"}},
    )

    assert response.status_code == 200
    citations = response.json()["citations"]
    assert len(citations) == 1
    assert citations[0]["metadata"]["domain"] == "agriculture"
