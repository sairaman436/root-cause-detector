"""
Purpose: Materializes dataset-v0.1 from governed human-reviewed JSONL exports.
Why it exists: The existing dataset module registers and validates database datasets, but it does not produce leakage-safe JSONL training artifacts.
Architecture fit: This deterministic boundary prepares data for future training pipelines and never trains, fine-tunes, or changes the production model.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import re
from collections import Counter, defaultdict
from pathlib import Path
from typing import Any, Iterable


DATASET_VERSION = "dataset-v0.1"
TASKS = (
    "root-cause-analysis",
    "recommendation-generation",
    "rag-grounded-responses",
)
ACCEPTED_DECISIONS = {"ACCEPT", "APPROVE", "CORRECTED", "EDIT", "VALIDATED"}
CORRECTED_DECISIONS = {"CORRECTED", "EDIT"}
ACCEPTED_STATUSES = {"APPROVED", "APPROVED_FOR_FUTURE_DATASET", "VALIDATED", "PROMOTED"}
REJECTED_VALUES = {"REJECT", "REJECTED", "INVALID", "PENDING", "PENDING_APPROVAL", "PENDING_HUMAN_REVIEW", "UNRESOLVED"}
SENSITIVE_VALUES = {"PII", "SENSITIVE", "RESTRICTED", "CONFIDENTIAL", "SECRET"}
EMAIL = re.compile(r"[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}")
PHONE = re.compile(r"(?<!\d)(?:\+?\d{10,12})(?!\d)")
AADHAAR = re.compile(r"(?<!\d)\d{4}[ -]?\d{4}[ -]?\d{4}(?!\d)")
UUID = re.compile(r"(?i)\b[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\b")


def _text(value: Any) -> str:
    return value.strip() if isinstance(value, str) else ""


def _upper(value: Any) -> str:
    return _text(value).upper()


def _bool(value: Any) -> bool:
    return value is True or (isinstance(value, str) and value.strip().lower() == "true")


def _contains_pii(value: Any) -> bool:
    if isinstance(value, str):
        # UUIDs are provenance identifiers, not phone numbers. Remove them
        # before numeric PII checks so a UUID suffix cannot trigger PHONE.
        pii_text = UUID.sub("", value)
        return bool(EMAIL.search(pii_text) or PHONE.search(pii_text) or AADHAAR.search(pii_text))
    if isinstance(value, dict):
        return any(_contains_pii(item) for item in value.values())
    if isinstance(value, list):
        return any(_contains_pii(item) for item in value)
    return False


def _normalise(value: str) -> str:
    return " ".join(value.lower().split())


def _fingerprint(task: str, input_text: str, output_text: str) -> str:
    source = f"{task}\n{_normalise(input_text)}\n{_normalise(output_text)}"
    return hashlib.sha256(source.encode("utf-8")).hexdigest()


def _citations(record: dict[str, Any]) -> list[Any]:
    value = record.get("citations", record.get("evidence", []))
    return value if isinstance(value, list) else []


def _citations_are_valid(record: dict[str, Any], citations: list[Any]) -> bool:
    if record.get("citation_valid") is False:
        return False
    for citation in citations:
        if isinstance(citation, dict) and any(
            citation.get(key) is False
            for key in ("valid", "resolves", "supports_claim", "citation_resolves", "citation_supports_claim", "citation_correct")
        ):
            return False
    return bool(citations)


def _split_for(group: str, version: str) -> str:
    bucket = int(hashlib.sha256(f"{version}:{group}".encode("utf-8")).hexdigest()[:8], 16) % 10000
    if bucket < 8000:
        return "train"
    if bucket < 9000:
        return "validation"
    return "test"


def _provenance(record: dict[str, Any], decision: str) -> dict[str, Any]:
    source = record.get("provenance") if isinstance(record.get("provenance"), dict) else {}
    return {
        "source_id": _text(source.get("source_id", record.get("source_id"))),
        "source_type": _text(source.get("source_type", record.get("source_type"))),
        "review_id": _text(source.get("review_id", record.get("review_id"))),
        "review_decision": decision,
        "approved_at": _text(source.get("approved_at", record.get("approved_at"))),
        "model_version": _text(source.get("model_version", record.get("model_version"))),
        "prompt_version": _text(source.get("prompt_version", record.get("prompt_version"))),
    }


def _validate(record: dict[str, Any]) -> tuple[str | None, dict[str, Any] | None]:
    task = _text(record.get("task")).lower()
    if task not in TASKS:
        return "unsupported_task", None
    example_id = _text(record.get("example_id", record.get("id")))
    input_text = _text(record.get("input", record.get("input_text")))
    ai_output = _text(record.get("ai_output", record.get("output", record.get("output_text"))))
    human_output = _text(record.get("human_edited_output", record.get("human_edited_output_text")))
    accepted_output = _text(record.get("accepted_output", record.get("accepted_output_text")))
    decision = _upper(record.get("review_decision", record.get("decision")))
    status = _upper(record.get("approval_status", record.get("status")))
    group = _text(record.get("scenario_group", record.get("scenario_id")))
    citations = _citations(record)
    provenance = _provenance(record, decision)

    required = (
        ("example_id", example_id),
        ("input", input_text),
        ("ai_output", ai_output),
        ("scenario_group", group),
        ("provenance.source_id", provenance["source_id"]),
        ("provenance.source_type", provenance["source_type"]),
        ("provenance.review_id", provenance["review_id"]),
        ("provenance.approved_at", provenance["approved_at"]),
    )
    missing = next((name for name, value in required if not value), None)
    if missing:
        return f"missing_{missing.replace('.', '_')}", None
    if decision not in ACCEPTED_DECISIONS:
        return "not_human_approved", None
    if status not in ACCEPTED_STATUSES:
        return "approval_status_not_released", None
    if status in REJECTED_VALUES or decision in REJECTED_VALUES:
        return "rejected_or_unresolved", None
    synthetic = _bool(record.get("synthetic"))
    if synthetic and "synthetic" not in DATASET_VERSION.lower() and "dev" not in DATASET_VERSION.lower():
        return "synthetic_not_allowed_in_production_dataset", None
    if record.get("training_eligible") is False:
        return "training_not_eligible", None
    if _upper(record.get("privacy_classification")) in SENSITIVE_VALUES or _bool(record.get("sensitive")):
        return "sensitive_example", None
    if _bool(record.get("hallucination_detected")) or int(record.get("hallucination_count", 0) or 0) > 0:
        return "hallucination_detected", None
    if _bool(record.get("unsupported_claims")) or int(record.get("unsupported_claim_count", 0) or 0) > 0:
        return "unsupported_claims", None
    if record.get("resolved") is False or _upper(record.get("resolution_status")) == "UNRESOLVED":
        return "unresolved_example", None
    if not _citations_are_valid(record, citations):
        return "invalid_or_missing_citations", None
    if _contains_pii(record):
        return "pii_detected", None

    corrected = decision in CORRECTED_DECISIONS
    output = accepted_output or human_output if corrected else accepted_output or human_output
    if not output:
        return "missing_validated_output", None
    if corrected and not _bool(record.get("correction_validated", record.get("validated"))):
        return "correction_not_validated", None

    fingerprint = _fingerprint(task, input_text, output)
    return None, {
        "dataset_version": DATASET_VERSION,
        "example_id": example_id,
        "task": task,
        "split": "",
        "scenario_group": group,
        "input": input_text,
        "output": output,
        "citations": citations,
        "provenance": provenance,
        "quality": {
            "human_validated": True,
            "corrected": corrected,
            "synthetic": synthetic,
            "citation_valid": True,
            "fingerprint": fingerprint,
        },
    }


def _read_records(path: Path | None) -> tuple[list[dict[str, Any]], Counter[str], bool]:
    if path is None or not path.exists():
        return [], Counter(), False
    records: list[dict[str, Any]] = []
    failures: Counter[str] = Counter()
    for line_number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        if not line.strip():
            continue
        try:
            value = json.loads(line)
        except json.JSONDecodeError:
            failures["invalid_json"] += 1
            continue
        if not isinstance(value, dict):
            failures["record_not_object"] += 1
            continue
        value["_line_number"] = line_number
        records.append(value)
    return records, failures, True


def build_dataset(input_path: Path | None, output_dir: Path, report_path: Path, version: str = DATASET_VERSION) -> dict[str, Any]:
    global DATASET_VERSION
    DATASET_VERSION = version
    records, failures, source_available = _read_records(input_path)
    accepted: list[dict[str, Any]] = []
    seen_fingerprints: set[str] = set()
    seen_ids: set[str] = set()
    rejected = 0
    for record in records:
        reason, materialized = _validate(record)
        if reason:
            failures[reason] += 1
            rejected += 1
            continue
        assert materialized is not None
        fingerprint = materialized["quality"]["fingerprint"]
        if materialized["example_id"] in seen_ids:
            failures["duplicate_example_id"] += 1
            rejected += 1
            continue
        if fingerprint in seen_fingerprints:
            failures["duplicate_content"] += 1
            rejected += 1
            continue
        seen_ids.add(materialized["example_id"])
        seen_fingerprints.add(fingerprint)
        materialized["split"] = _split_for(materialized["scenario_group"], version)
        accepted.append(materialized)

    output_dir.mkdir(parents=True, exist_ok=True)
    for task in TASKS:
        target = output_dir / f"{task}.jsonl"
        lines = [json.dumps(item, ensure_ascii=True, sort_keys=True, separators=(",", ":")) for item in accepted if item["task"] == task]
        target.write_text("\n".join(lines) + ("\n" if lines else ""), encoding="utf-8")

    split_groups: dict[str, set[str]] = defaultdict(set)
    for item in accepted:
        split_groups[item["split"]].add(item["scenario_group"])
    split_fingerprints = [
        set(item["quality"]["fingerprint"] for item in accepted if item["split"] == split)
        for split in ("train", "validation", "test")
        if any(item["split"] == split for item in accepted)
    ]
    leakage = len(set.intersection(*split_fingerprints)) if len(split_fingerprints) > 1 else 0
    counts = Counter(item["split"] for item in accepted)
    task_counts = {task: sum(1 for item in accepted if item["task"] == task) for task in TASKS}
    synthetic_count = sum(1 for item in accepted if item["quality"]["synthetic"])
    manifest = {
        "dataset_version": version,
        "source_available": source_available,
        "source_path": str(input_path) if input_path else None,
        "accepted_examples": len(accepted),
        "rejected_examples": rejected,
        "split_counts": {"train": counts["train"], "validation": counts["validation"], "test": counts["test"]},
        "task_counts": task_counts,
        "synthetic_examples": synthetic_count,
        "development_only": "synthetic" in version.lower() or "dev" in version.lower(),
        "scenario_groups_by_split": {key: sorted(value) for key, value in sorted(split_groups.items())},
        "validation_failures": dict(sorted(failures.items())),
        "split_leakage_count": leakage,
        "training_performed": False,
    }
    (output_dir / "manifest.json").write_text(json.dumps(manifest, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    _write_report(report_path, manifest)
    return manifest


def _write_report(path: Path, manifest: dict[str, Any]) -> None:
    failures = manifest["validation_failures"]
    failure_lines = "\n".join(f"- `{key}`: {value}" for key, value in failures.items()) or "- None"
    split = manifest["split_counts"]
    source_note = (
        "This is a synthetic development-only fixture. It must not be treated as real field data or promoted to a production training dataset."
        if manifest["development_only"] and manifest["synthetic_examples"]
        else "Approved or validated corrected governed records were materialized from the authenticated candidate export. No training or fine-tuning was performed."
        if manifest["accepted_examples"]
        else "The current repository database has no learning records, training candidates, or approval decisions; therefore this release contains no examples until a governed source export is supplied."
    )
    path.parent.mkdir(parents=True, exist_ok=True)
    content = (
        f"# DATASET QUALITY REPORT\n\n"
        f"Dataset version: `{manifest['dataset_version']}`\n\n"
        "## Scope\n\n"
        "This report covers deterministic materialization of approved or validated human-corrected records. No training or fine-tuning is performed.\n\n"
        "## Counts\n\n"
        f"- Source export available: `{manifest['source_available']}`\n"
        f"- Accepted examples: `{manifest['accepted_examples']}`\n"
        f"- Rejected examples: `{manifest['rejected_examples']}`\n"
        f"- Train: `{split['train']}`\n"
        f"- Validation: `{split['validation']}`\n"
        f"- Test: `{split['test']}`\n"
        f"- Split leakage count: `{manifest['split_leakage_count']}`\n\n"
        f"- Synthetic examples: `{manifest['synthetic_examples']}`\n"
        f"- Development only: `{manifest['development_only']}`\n\n"
        "## Task Counts\n\n"
        + "\n".join(f"- `{task}`: `{count}`" for task, count in manifest["task_counts"].items())
        + "\n\n## Validation Failures\n\n"
        + failure_lines
        + "\n\n## Artifacts\n\n"
        "- `root-cause-analysis.jsonl`\n"
        "- `recommendation-generation.jsonl`\n"
        "- `rag-grounded-responses.jsonl`\n"
        "- `manifest.json`\n\n"
        + source_note
        + "\n"
    )
    path.write_text(content, encoding="utf-8")


def main() -> None:
    parser = argparse.ArgumentParser(description="Build the governed dataset-v0.1 JSONL artifacts.")
    parser.add_argument("--input", type=Path, help="Approved/corrected JSONL source export.")
    parser.add_argument("--output-dir", type=Path, default=Path("ml-platform/training-pipelines/dataset-v0.1"))
    parser.add_argument("--report", type=Path, default=Path("DATASET_QUALITY_REPORT.md"))
    parser.add_argument("--version", default=DATASET_VERSION)
    args = parser.parse_args()
    manifest = build_dataset(args.input, args.output_dir, args.report, args.version)
    print(json.dumps(manifest, sort_keys=True))


if __name__ == "__main__":
    main()
