"""
Purpose: Verifies the notification service health contract.
Why it exists: Protects the service health and local notification delivery contracts.
Architecture fit: Supports CI and Docker Compose readiness checks for the notification boundary.
"""

from fastapi.testclient import TestClient
from notification_service.main import app


def test_health_endpoint() -> None:
    response = TestClient(app).get("/health")

    assert response.status_code == 200
    assert response.json()["service"] == "notification-service"


def test_send_notification_records_delivery() -> None:
    response = TestClient(app).post(
        "/v1/notifications",
        json={
            "channel": "in_app",
            "recipient": "analyst@example.org",
            "subject": "Report ready",
            "body": "The village report is ready.",
        },
    )

    assert response.status_code == 200
    assert response.json()["status"] == "delivered_local"
