"""
Purpose: Verifies the AI inference service health contract.
Why it exists: Protects the service shell before prediction logic is introduced.
Architecture fit: Supports CI and Docker Compose readiness checks for the AI service boundary.
"""

from ai_inference_service.main import app
from fastapi.testclient import TestClient


def test_health_endpoint() -> None:
    response = TestClient(app).get("/health")

    assert response.status_code == 200
    assert response.json()["service"] == "ai-inference-service"
