"""
Purpose: Runs the existing structured root-cause scenario against Qwen and SONAR.
Why it exists: Model comparison must use identical prompts, context, and validation without changing production routing.
Architecture fit: Thin benchmark runner over the existing AIProvider and PromptRegistry contracts; it is not a second evaluation framework.
"""

from __future__ import annotations

import json
import os
import sys
import time
from typing import Any

from ai_inference_service.main import (
    OllamaProvider,
    ProviderError,
    RuralAnalysisRequest,
    SONARProvider,
    prompt_registry,
)

try:
    import psutil
except ImportError:  # pragma: no cover - optional benchmark dependency
    psutil = None


SCENARIOS = [
    RuralAnalysisRequest(
        request_id="benchmark-water-reliability-001",
        problem="Water supply is unreliable in the village.",
        survey={"name": "Water survey", "primary_water_source": "well", "repair_delay_days": 45},
        submission={"answers": [{"questionCode": "water_source", "value": "well"}]},
        evidence=[{"source_id": "field-note-1", "file_name": "water-field-note.txt", "statement": "Bore well repairs are delayed."}],
        citations=[{"source_id": "approved-water-policy", "excerpt": "Maintenance accountability should be verified.", "score": 0.86}],
    )
]


def rss_mb() -> float | None:
    """Returns resident process memory when psutil is installed."""

    if psutil is None:
        return None
    return round(psutil.Process().memory_info().rss / (1024 * 1024), 2)


def run_provider(name: str, provider: Any, model: str, runs: int) -> dict[str, Any]:
    results: list[dict[str, Any]] = []
    for scenario in SCENARIOS:
        prompt = prompt_registry.get("ROOT_CAUSE_ANALYSIS", "1.0.0").render(scenario)
        for iteration in range(1, runs + 1):
            before_memory = rss_mb()
            started = time.perf_counter()
            try:
                output = provider.structured_generate(prompt, model)
                elapsed_ms = round((time.perf_counter() - started) * 1000, 2)
                after_memory = rss_mb()
                results.append(
                    {
                        "scenario": scenario.request_id,
                        "iteration": iteration,
                        "success": True,
                        "latency_ms": elapsed_ms,
                        "memory_before_mb": before_memory,
                        "memory_after_mb": after_memory,
                        "memory_delta_mb": None if before_memory is None or after_memory is None else round(after_memory - before_memory, 2),
                        "structured_output": True,
                        "root_cause_count": len(output.root_causes),
                        "evidence_count": len(output.evidence),
                        "recommendation_count": len(output.recommendations),
                    }
                )
            except ProviderError as exc:
                results.append(
                    {
                        "scenario": scenario.request_id,
                        "iteration": iteration,
                        "success": False,
                        "latency_ms": round((time.perf_counter() - started) * 1000, 2),
                        "memory_before_mb": before_memory,
                        "memory_after_mb": rss_mb(),
                        "structured_output": False,
                        "error_code": exc.code,
                        "error": str(exc),
                    }
                )
    successful = [result for result in results if result["success"]]
    return {
        "provider": name,
        "model": model,
        "runs": len(results),
        "successful_runs": len(successful),
        "successful_generation_rate": round(len(successful) / len(results), 4) if results else 0.0,
        "mean_latency_ms": round(sum(result["latency_ms"] for result in successful) / len(successful), 2) if successful else None,
        "structured_output_success_rate": round(sum(result["structured_output"] for result in results) / len(results), 4) if results else 0.0,
        "results": results,
        "quality_metrics": {
            "root_cause_quality": "NOT_SCORED_REQUIRES_HUMAN_RUBRIC",
            "evidence_grounding": "NOT_SCORED_REQUIRES_CITATION_LABELS",
            "citation_correctness": "NOT_SCORED_REQUIRES_SOURCE_VERIFICATION",
            "hallucination_unsupported_claim_rate": "NOT_SCORED_REQUIRES_HUMAN_OR_REFERENCE_LABELS",
            "recommendation_quality": "NOT_SCORED_REQUIRES_HUMAN_RUBRIC",
        },
    }


def main() -> None:
    runs = max(1, int(os.getenv("BENCHMARK_RUNS", "3")))
    qwen_model = os.getenv("QWEN_MODEL", "qwen2.5:0.5b")
    sonar_model = os.getenv("SONAR_MODEL_ID", "raxtemur/sonar-llm-100m")
    report = {
        "environment": {"python": sys.version, "runs_per_model": runs, "scenario_count": len(SCENARIOS)},
        "qwen": run_provider("ollama", OllamaProvider(), qwen_model, runs),
        "sonar": run_provider("sonar", SONARProvider(), sonar_model, runs),
    }
    print(json.dumps(report, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    main()
