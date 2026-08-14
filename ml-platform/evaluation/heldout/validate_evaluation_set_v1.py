"""
Purpose: Validates the immutable held-out evaluation-set exports.
Why it exists: Evaluation inputs require the same strict v0.3 contract as
training inputs while additionally enforcing test-only task coverage and
methodology identity.
Architecture fit: Offline evaluation quality gate; it never changes review
state, dataset-v0.3, or model artifacts.
"""

from __future__ import annotations

import argparse
import json
import sys
from collections import Counter
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from build_evaluation_set_v1 import (
    EVALUATION_SET_VERSION,
    EVALUATION_VERSION,
    OUTPUT_CONTRACT_VERSION,
    RUBRIC_VERSION,
    TASKS,
    _validate_evaluation_record,
)


def validate(path: Path) -> dict[str, object]:
    failures: Counter[str] = Counter()
    records: list[dict[str, object]] = []
    for file_path in sorted(path.glob("*.jsonl")):
        for line_number, line in enumerate(file_path.read_text(encoding="utf-8").splitlines(), 1):
            if not line.strip():
                continue
            try:
                record = json.loads(line)
            except json.JSONDecodeError:
                failures[f"{file_path.name}:{line_number}:malformed_json"] += 1
                continue
            records.append(record)
            for failure in _validate_evaluation_record(record):
                failures[f"{file_path.name}:{line_number}:{failure}"] += 1
    ids = [str(record.get("example_id")) for record in records]
    scenarios = [str(record.get("scenario_group")) for record in records]
    if len(ids) != len(set(ids)):
        failures["duplicate_example_id"] += 1
    if len(scenarios) != len(set(scenarios)):
        failures["scenario_leakage"] += 1
    task_counts = Counter(record.get("task") for record in records)
    for task in TASKS:
        if task_counts[task] < 1:
            failures[f"task_coverage:{task}"] += 1
    return {
        "evaluation_set_version": EVALUATION_SET_VERSION,
        "output_contract_version": OUTPUT_CONTRACT_VERSION,
        "evaluation_version": EVALUATION_VERSION,
        "rubric_version": RUBRIC_VERSION,
        "records": len(records),
        "task_counts": {task: task_counts[task] for task in TASKS},
        "split_counts": {"train": 0, "validation": 0, "test": len(records)},
        "valid": not failures,
        "failures": dict(sorted(failures.items())),
    }


def main() -> None:
    parser = argparse.ArgumentParser(description="Validate the immutable held-out evaluation set.")
    parser.add_argument("--input", type=Path, default=Path("ml-platform/evaluation/heldout/evaluation-set-v1.0.0"))
    args = parser.parse_args()
    print(json.dumps(validate(args.input), sort_keys=True))


if __name__ == "__main__":
    main()
