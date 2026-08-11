"""
Purpose: Materializes immutable dataset-v0.2 JSONL exports from an authenticated
approved-candidate snapshot.
Why it exists: v0.2 adds structured-target, sequence-bound, citation-context,
and explicit split checks that the v0.1 compatibility builder does not enforce.
Architecture fit: Governed MLOps dataset boundary; it never reviews, approves,
trains, or changes live database records.
"""

from __future__ import annotations

import argparse
import hashlib
import importlib.util
import json
from collections import Counter
from pathlib import Path
from typing import Any


TASKS = (
    "root-cause-analysis",
    "recommendation-generation",
    "rag-grounded-responses",
)
SPLITS = ("train", "validation", "test")
SEQUENCE_LENGTH = 1024


def _load_v01_builder():
    path = Path(__file__).with_name("build_dataset_v01.py")
    spec = importlib.util.spec_from_file_location("dataset_v01_builder", path)
    if spec is None or spec.loader is None:
        raise RuntimeError("Unable to load the existing governed dataset builder")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    module.DATASET_VERSION = "dataset-v0.2"
    return module


def _structured_json(value: str) -> bool:
    try:
        parsed = json.loads(value)
    except json.JSONDecodeError:
        return False
    return isinstance(parsed, dict)


def _token_count(value: str) -> int:
    return len(value.split())


def _has_citation_context(record: dict[str, Any]) -> bool:
    if record["task"] != "rag-grounded-responses":
        return True
    input_text = record["input"].lower()
    if "retrieved evidence and citation context" not in input_text:
        return False
    return any(str(citation.get("source_id", "")).lower() in input_text for citation in record["citations"] if isinstance(citation, dict))


def _fingerprint(record: dict[str, Any]) -> str:
    normalized = " ".join(record["output"].lower().split())
    source = f"{record['task']}\n{record['input'].lower()}\n{normalized}"
    return hashlib.sha256(source.encode("utf-8")).hexdigest()


def materialize(source_path: Path, output_dir: Path, report_path: Path) -> dict[str, Any]:
    if output_dir.exists() and any(output_dir.iterdir()):
        raise RuntimeError(f"Immutable dataset directory already exists: {output_dir}")

    builder = _load_v01_builder()
    records = [json.loads(line) for line in source_path.read_text(encoding="utf-8").splitlines() if line.strip()]
    accepted: list[dict[str, Any]] = []
    failures: Counter[str] = Counter()
    seen_ids: set[str] = set()
    seen_fingerprints: set[str] = set()
    seen_scenarios: dict[str, str] = {}

    for record in records:
        task = str(record.get("task", ""))
        output = str(record.get("accepted_output", ""))
        if not _structured_json(output):
            failures["malformed_structured_target"] += 1
            continue
        if _token_count(output) > SEQUENCE_LENGTH:
            failures["target_exceeds_sequence_length"] += 1
            continue
        if not _has_citation_context(record):
            failures["missing_rag_citation_context"] += 1
            continue
        if task not in TASKS:
            failures["unsupported_task"] += 1
            continue
        split = record.get("split")
        if split not in SPLITS:
            failures["invalid_split"] += 1
            continue
        scenario = str(record.get("scenario_group", ""))
        if scenario in seen_scenarios:
            failures["scenario_duplicate"] += 1
            continue
        reason, item = builder._validate(record)
        if reason or item is None:
            failures[reason or "record_validation_failed"] += 1
            continue
        example_id = item["example_id"]
        fingerprint = _fingerprint({"task": task, "input": item["input"], "output": item["output"]})
        if example_id in seen_ids:
            failures["duplicate_example_id"] += 1
            continue
        if fingerprint in seen_fingerprints:
            failures["duplicate_content"] += 1
            continue
        item["dataset_version"] = "dataset-v0.2"
        item["split"] = split
        item["quality"]["fingerprint"] = fingerprint
        seen_ids.add(example_id)
        seen_fingerprints.add(fingerprint)
        seen_scenarios[scenario] = split
        accepted.append(item)

    split_counts = Counter(item["split"] for item in accepted)
    task_counts = Counter(item["task"] for item in accepted)
    for split in SPLITS:
        if split_counts[split] == 0:
            failures[f"empty_{split}_split"] += 1
    for task in TASKS:
        if task_counts[task] == 0:
            failures[f"missing_task_{task}"] += 1

    if failures:
        raise ValueError(json.dumps(dict(sorted(failures.items())), sort_keys=True))

    output_dir.mkdir(parents=True, exist_ok=False)
    for task in TASKS:
        path = output_dir / f"{task}.jsonl"
        rows = [json.dumps(item, ensure_ascii=True, sort_keys=True, separators=(",", ":")) for item in accepted if item["task"] == task]
        path.write_text("\n".join(rows) + "\n", encoding="utf-8")

    manifest = {
        "dataset_version": "dataset-v0.2",
        "immutable": True,
        "accepted_examples": len(accepted),
        "rejected_examples": sum(failures.values()),
        "split_counts": {split: split_counts[split] for split in SPLITS},
        "task_counts": {task: task_counts[task] for task in TASKS},
        "scenario_leakage_count": 0,
        "split_leakage_count": 0,
        "structured_targets_validated": True,
        "max_output_tokens_observed": max(_token_count(item["output"]) for item in accepted),
        "citation_context_validated": True,
        "synthetic_examples": 0,
        "training_performed": False,
        "validation_failures": {},
    }
    (output_dir / "manifest.json").write_text(json.dumps(manifest, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    report_path.write_text(
        "# DATASET v0.2 QUALITY REPORT\n\n"
        "Materialized only from explicitly approved or validated corrected governed records.\n"
        "No training or fine-tuning was performed.\n\n"
        f"- Total examples: `{manifest['accepted_examples']}`\n"
        f"- Train: `{manifest['split_counts']['train']}`\n"
        f"- Validation: `{manifest['split_counts']['validation']}`\n"
        f"- Test: `{manifest['split_counts']['test']}`\n"
        f"- Structured targets valid: `{manifest['structured_targets_validated']}`\n"
        f"- Maximum output tokens: `{manifest['max_output_tokens_observed']}` / `{SEQUENCE_LENGTH}`\n"
        f"- Citation context valid: `{manifest['citation_context_validated']}`\n"
        f"- Scenario leakage: `{manifest['scenario_leakage_count']}`\n"
        f"- Split leakage: `{manifest['split_leakage_count']}`\n"
        f"- Synthetic examples: `{manifest['synthetic_examples']}`\n\n"
        "## Task Distribution\n\n"
        + "\n".join(f"- `{task}`: `{manifest['task_counts'][task]}`" for task in TASKS)
        + "\n",
        encoding="utf-8",
    )
    return manifest


def main() -> None:
    parser = argparse.ArgumentParser(description="Materialize immutable dataset-v0.2 exports.")
    parser.add_argument("--input", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path, default=Path("ml-platform/training-pipelines/dataset-v0.2"))
    parser.add_argument("--report", type=Path, default=Path("DATASET_V02_QUALITY_REPORT.md"))
    args = parser.parse_args()
    print(json.dumps(materialize(args.input, args.output_dir, args.report), sort_keys=True))


if __name__ == "__main__":
    main()
