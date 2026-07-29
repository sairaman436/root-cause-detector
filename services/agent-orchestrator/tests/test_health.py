"""
Purpose: Verifies the agent orchestrator health contract.
Why it exists: Protects the service shell before agent workflows are introduced.
Architecture fit: Supports CI and Docker Compose readiness checks for the orchestration boundary.
"""

from agent_orchestrator.main import app
from fastapi.testclient import TestClient


def test_health_endpoint() -> None:
    response = TestClient(app).get("/health")

    assert response.status_code == 200
    assert response.json()["service"] == "agent-orchestrator"
