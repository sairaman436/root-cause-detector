"""
Purpose: Verifies the local LLM provider boundary.
Why it exists: The platform must fail clearly when Ollama/Qwen is unavailable and must validate structured analysis before persistence.
Architecture fit: Protects the provider abstraction, prompt registry, and strict rural analysis contract.
"""

import pytest
from ai_inference_service import main
from fastapi.testclient import TestClient


class FakeProvider(main.AIProvider):
    """Test provider used to avoid requiring Ollama in CI."""

    name = "fake-provider"
    default_model = "qwen-test"

    def __init__(self, output: str | None = None, error: main.ProviderError | None = None) -> None:
        self.output = output
        self.error = error

    def generate(self, prompt: str, model: str | None = None, *, require_json: bool = False) -> dict[str, object]:
        if self.error:
            raise self.error
        return {"response": self.output or "plain text response"}

    def chat(self, messages: list[main.ChatMessage], model: str | None = None) -> dict[str, object]:
        return {"message": {"content": "ok"}}

    def structured_generate(self, prompt: str, model: str | None = None) -> main.RuralAnalysisOutput:
        if self.error:
            raise self.error
        if self.output == "malformed":
            raise main.ProviderError("LLM_INVALID_STRUCTURED_OUTPUT", "invalid schema", 502)
        return main.RuralAnalysisOutput(
            problem="Water reliability",
            summary="Water access is constrained by maintenance delays.",
            contributing_factors=["Repair backlog"],
            root_causes=["Weak maintenance capacity"],
            evidence=["Survey answer water_source=well"],
            confidence=0.74,
            recommendations=["Prioritize maintenance crew routing"],
            limitations=["Requires field validation"],
        )

    def stream(self, prompt: str, model: str | None = None):
        yield "ok"

    def health(self, model: str | None = None) -> main.ProviderHealth:
        return main.ProviderHealth(
            provider=self.name,
            configured_model=model or self.default_model,
            status="ok",
            model_available=True,
            model_version=model or self.default_model,
        )


@pytest.fixture(autouse=True)
def reset_provider(monkeypatch: pytest.MonkeyPatch, request: pytest.FixtureRequest) -> None:
    if request.node.name in {"test_ollama_remains_the_default_provider", "test_sonar_provider_is_opt_in_without_loading_the_checkpoint"}:
        return
    monkeypatch.setattr(main, "provider", lambda: FakeProvider())


def test_health_endpoint_reports_provider() -> None:
    response = TestClient(main.app).get("/health")

    assert response.status_code == 200
    assert response.json()["service"] == "ai-inference-service"
    assert response.json()["provider"] == "fake-provider"


def test_prompt_registry_contains_required_prompts() -> None:
    response = TestClient(main.app).get("/v1/prompts")

    prompt_ids = {prompt["prompt_id"] for prompt in response.json()["prompts"]}
    assert {"ROOT_CAUSE_ANALYSIS", "RECOMMENDATION_GENERATION"}.issubset(prompt_ids)


def test_structured_root_cause_analysis_returns_strict_schema() -> None:
    response = TestClient(main.app).post(
        "/v1/analysis/root-cause",
        json={
            "problem": "Water supply is unreliable in the village.",
            "survey": {"name": "Water survey"},
            "submission": {"answers": [{"questionCode": "water_source", "value": "well"}]},
            "evidence": [{"fileName": "field-note.txt"}],
            "model": "qwen-test",
        },
    )

    assert response.status_code == 200
    body = response.json()
    assert body["success"] is True
    assert body["provider"] == "fake-provider"
    assert set(body["output"].keys()) == {
        "problem",
        "summary",
        "contributing_factors",
        "root_causes",
        "evidence",
        "confidence",
        "recommendations",
        "limitations",
    }
    assert body["output"]["root_causes"]


def test_structured_analysis_rejects_malformed_model_output(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr(main, "provider", lambda: FakeProvider(output="malformed"))

    response = TestClient(main.app).post(
        "/v1/analysis/root-cause",
        json={"problem": "Water supply is unreliable."},
    )

    assert response.status_code == 502
    assert response.json()["detail"]["code"] == "LLM_INVALID_STRUCTURED_OUTPUT"


def test_structured_analysis_fails_when_model_unavailable(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr(
        main,
        "provider",
        lambda: FakeProvider(error=main.ProviderError("OLLAMA_MODEL_UNAVAILABLE", "model missing", 503)),
    )

    response = TestClient(main.app).post(
        "/v1/analysis/root-cause",
        json={"problem": "Water supply is unreliable."},
    )

    assert response.status_code == 503
    assert response.json()["detail"]["code"] == "OLLAMA_MODEL_UNAVAILABLE"


def test_canonical_payload_converts_list_objects_to_strings() -> None:
    payload = main._canonical_rural_analysis_payload(
        """
        {
          "problem":"Water reliability",
          "summary":"Repairs are delayed.",
          "contributing_factors":["Repair backlog"],
          "root_causes":["Weak maintenance"],
          "evidence":[{"source":"survey","value":"well"}],
          "confidence":0.72,
          "recommendations":["Improve routing"],
          "limitations":["Needs field validation"]
        }
        """
    )

    validated = main.RuralAnalysisOutput.model_validate(payload)
    assert validated.evidence == ['{"source": "survey", "value": "well"}']


def test_ollama_remains_the_default_provider(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.delenv("LLM_MODEL", raising=False)
    monkeypatch.delenv("AI_DEFAULT_MODEL", raising=False)
    monkeypatch.delenv("LLM_PROVIDER", raising=False)

    selected = main.provider()

    assert isinstance(selected, main.OllamaProvider)
    assert selected.default_model == "qwen2.5:0.5b"


def test_sonar_provider_is_opt_in_without_loading_the_checkpoint(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setenv("LLM_PROVIDER", "sonar")
    monkeypatch.setenv("SONAR_MODEL_ID", "raxtemur/sonar-llm-100m")

    selected = main.provider()

    assert isinstance(selected, main.SONARProvider)
    assert selected.default_model == "raxtemur/sonar-llm-100m"
    with pytest.raises(main.ProviderError, match="configured"):
        selected.generate("test", "raxtemur/another-model")


def test_legacy_inference_no_longer_reports_fake_success_on_provider_failure(monkeypatch: pytest.MonkeyPatch) -> None:
    monkeypatch.setattr(
        main,
        "provider",
        lambda: FakeProvider(error=main.ProviderError("OLLAMA_UNAVAILABLE", "ollama down", 503)),
    )

    response = TestClient(main.app).post("/v1/inference", json={"prompt": "hello"})

    assert response.status_code == 503
    assert response.json()["detail"]["code"] == "OLLAMA_UNAVAILABLE"
