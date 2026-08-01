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


def test_orchestrate_returns_agent_steps() -> None:
    response = TestClient(app).post(
        "/v1/orchestrate",
        json={"survey_id": "survey-1", "evidence_ids": ["evidence-1"], "objective": "Find water root causes"},
    )

    assert response.status_code == 200
    body = response.json()
    assert body["status"] == "orchestrated"
    assert len(body["steps"]) == 5
