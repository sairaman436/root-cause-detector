"""
Purpose: Verifies the notification service health contract.
Why it exists: Protects the service shell before delivery logic is introduced.
Architecture fit: Supports CI and Docker Compose readiness checks for the notification boundary.
"""

from fastapi.testclient import TestClient
from notification_service.main import app


def test_health_endpoint() -> None:
    response = TestClient(app).get("/health")

    assert response.status_code == 200
    assert response.json()["service"] == "notification-service"
