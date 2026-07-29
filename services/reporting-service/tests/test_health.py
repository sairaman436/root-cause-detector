"""
Purpose: Verifies the reporting service health contract.
Why it exists: Protects the service shell before report generation is introduced.
Architecture fit: Supports CI and Docker Compose readiness checks for the reporting boundary.
"""

from fastapi.testclient import TestClient
from reporting_service.main import app


def test_health_endpoint() -> None:
    response = TestClient(app).get("/health")

    assert response.status_code == 200
    assert response.json()["service"] == "reporting-service"
