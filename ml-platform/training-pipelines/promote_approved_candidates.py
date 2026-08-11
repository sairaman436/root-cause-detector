"""
Purpose: Materializes the authenticated learning API export through the existing dataset validator.
Why it exists: Approved real candidates need a deterministic, auditable bridge into dataset-v0.1 without bypassing schema, provenance, PII, citation, or split checks.
Architecture fit: MLOps export adapter; it never reviews, approves, trains, or changes the synthetic development queue.
"""

from __future__ import annotations

import argparse
import importlib.util
import json
import tempfile
from pathlib import Path
from typing import Any
from urllib.request import Request, urlopen


class CandidateExportError(RuntimeError):
    """Raised when the authenticated candidate export is not safe to materialize."""


def _text(value: Any) -> str:
    return value.strip() if isinstance(value, str) else ""


def fetch_export(api_url: str, token: str) -> dict[str, Any]:
    if not _text(token):
        raise CandidateExportError("AUTH_TOKEN_REQUIRED")
    request = Request(api_url, headers={"Authorization": f"Bearer {token}", "Accept": "application/json"})
    with urlopen(request, timeout=30) as response:
        payload = json.loads(response.read().decode("utf-8"))
    return payload.get("data", payload)


def materialize_export(export: dict[str, Any], output_dir: Path, report_path: Path) -> dict[str, Any]:
    if export.get("datasetVersion") != "dataset-v0.1":
        raise CandidateExportError("DATASET_VERSION_MISMATCH")
    examples = export.get("examples")
    if not isinstance(examples, list) or not examples:
        raise CandidateExportError("NO_APPROVED_REAL_CANDIDATES")

    records: list[dict[str, Any]] = []
    for example in examples:
        if not isinstance(example, dict):
            raise CandidateExportError("EXPORT_RECORD_NOT_OBJECT")
        if example.get("synthetic") is True:
            raise CandidateExportError("SYNTHETIC_CANDIDATE_IN_EXPORT")
        decision = _text(example.get("reviewDecision")).upper()
        if decision not in {"APPROVE", "CORRECT"}:
            raise CandidateExportError("UNAPPROVED_CANDIDATE_IN_EXPORT")
        citations = example.get("citations")
        if not isinstance(citations, list) or not citations:
            raise CandidateExportError("CITATIONS_REQUIRED")
        provenance = example.get("provenance")
        if not isinstance(provenance, dict) or not all(_text(provenance.get(key)) for key in ("source_id", "source_type", "review_id", "approved_at", "model_version", "prompt_version")):
            raise CandidateExportError("PROVENANCE_REQUIRED")
        output = _text(example.get("output"))
        if not output or not _text(example.get("input")) or not _text(example.get("scenarioGroup")):
            raise CandidateExportError("DATASET_FIELDS_REQUIRED")
        record = {
            "id": example.get("exampleId"),
            "task": example.get("task"),
            "scenario_group": example.get("scenarioGroup"),
            "input": example.get("input"),
            "ai_output": output,
            "accepted_output": output,
            "review_decision": "CORRECTED" if decision == "CORRECT" else "APPROVE",
            "approval_status": "APPROVED_FOR_FUTURE_DATASET",
            "training_eligible": True,
            "privacy_classification": "INTERNAL",
            "citations": citations,
            "provenance": provenance,
            "synthetic": False,
            "correction_validated": decision == "CORRECT",
        }
        records.append(record)

    output_dir.mkdir(parents=True, exist_ok=True)
    with tempfile.NamedTemporaryFile("w", encoding="utf-8", suffix=".jsonl", delete=False) as source:
        source_path = Path(source.name)
        source.write("\n".join(json.dumps(record, ensure_ascii=True, sort_keys=True) for record in records) + "\n")
    try:
        try:
            from build_dataset_v01 import build_dataset
        except ModuleNotFoundError:
            builder_path = Path(__file__).with_name("build_dataset_v01.py")
            spec = importlib.util.spec_from_file_location("build_dataset_v01", builder_path)
            if spec is None or spec.loader is None:
                raise CandidateExportError("DATASET_BUILDER_UNAVAILABLE")
            module = importlib.util.module_from_spec(spec)
            spec.loader.exec_module(module)
            build_dataset = module.build_dataset

        manifest = build_dataset(source_path, output_dir, report_path, version="dataset-v0.1")
    finally:
        source_path.unlink(missing_ok=True)
    return manifest


def main() -> None:
    parser = argparse.ArgumentParser(description="Materialize authenticated approved candidates into dataset-v0.1.")
    parser.add_argument("--api-url", required=True)
    parser.add_argument("--token", required=True)
    parser.add_argument("--output-dir", type=Path, default=Path("ml-platform/training-pipelines/dataset-v0.1"))
    parser.add_argument("--report", type=Path, default=Path("DATASET_QUALITY_REPORT.md"))
    args = parser.parse_args()
    print(json.dumps(materialize_export(fetch_export(args.api_url, args.token), args.output_dir, args.report), sort_keys=True))


if __name__ == "__main__":
    main()
