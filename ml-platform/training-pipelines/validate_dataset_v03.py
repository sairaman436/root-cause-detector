"""
Purpose: Validates governed dataset-v0.3 candidates before immutable materialization.
Why it exists: Human approval alone cannot release malformed JSON, unsupported
citation IDs, PII, or truncated prompt/target records.
Architecture fit: Final MLOps quality gate; it never approves candidates or trains models.
"""

from __future__ import annotations

import argparse
import json
from collections import Counter
from pathlib import Path
from typing import Any

from contracts.dataset_v03_contract import validate_record


def validate(path: Path) -> dict[str, Any]:
    failures: Counter[str] = Counter()
    records: list[dict[str, Any]] = []
    for line_number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        if not line.strip():
            continue
        try:
            record = json.loads(line)
        except json.JSONDecodeError:
            failures[f"line_{line_number}:malformed_json"] += 1
            continue
        records.append(record)
        for failure in validate_record(record):
            failures[f"{line_number}:{failure}"] += 1
    scenario_splits: dict[str, str] = {}
    for index, record in enumerate(records, 1):
        scenario = record.get("scenario_group", "")
        split = record.get("split", "")
        if scenario in scenario_splits and scenario_splits[scenario] != split:
            failures[f"{index}:scenario_leakage"] += 1
        scenario_splits[scenario] = split
    return {"records": len(records), "valid": not failures, "failures": dict(sorted(failures.items()))}


def main() -> None:
    parser = argparse.ArgumentParser(description="Validate dataset-v0.3 contract records.")
    parser.add_argument("--input", type=Path, required=True)
    args = parser.parse_args()
    print(json.dumps(validate(args.input), sort_keys=True))


if __name__ == "__main__":
    main()
