"""
Purpose: Verifies the AI inference service health contract.
Why it exists: Protects the service health and inference contracts.
Architecture fit: Supports CI and Docker Compose readiness checks for the AI service boundary.
"""

from ai_inference_service.main import app
from fastapi.testclient import TestClient


def test_health_endpoint() -> None:
    response = TestClient(app).get("/health")

    assert response.status_code == 200
    assert response.json()["service"] == "ai-inference-service"


def test_inference_returns_structured_output() -> None:
    response = TestClient(app).post(
        "/v1/inference",
        json={
            "prompt": "Water supply is unreliable in the village.",
            "task_type": "root_cause_analysis",
            "context": {"evidence_ids": ["ev-1"]},
        },
    )

    assert response.status_code == 200
    body = response.json()
    assert body["structured_output"]["rootCauses"]
    assert body["structured_output"]["recommendations"]
    assert body["tokens_estimate"] > 0
