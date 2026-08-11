"""
Purpose: Creates and materializes the human review queue for training examples.
Why it exists: Development examples must remain pending until a human explicitly approves, corrects, or rejects each example.
Architecture fit: Bridges the existing development JSONL source and build_dataset_v01.py without bypassing production validation or starting training.
"""

from __future__ import annotations

import argparse
import json
from datetime import datetime
from pathlib import Path
from typing import Any


DECISIONS = {"APPROVE", "CORRECT", "REJECT"}
REVIEW_STATES = {"PENDING_HUMAN_REVIEW", "HUMAN_APPROVED", "HUMAN_CORRECTED", "HUMAN_REJECTED"}


class ReviewQueueError(RuntimeError):
    """Raised when a review queue cannot be safely created or materialized."""


def _text(value: Any) -> str:
    return value.strip() if isinstance(value, str) else ""


def _read_jsonl(path: Path) -> list[dict[str, Any]]:
    if not path.exists():
        raise ReviewQueueError(f"SOURCE_NOT_FOUND:{path}")
    rows: list[dict[str, Any]] = []
    for line_number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        if not line.strip():
            continue
        try:
            value = json.loads(line)
        except json.JSONDecodeError as exc:
            raise ReviewQueueError(f"INVALID_JSON:{path}:{line_number}") from exc
        if not isinstance(value, dict):
            raise ReviewQueueError(f"RECORD_NOT_OBJECT:{path}:{line_number}")
        rows.append(value)
    return rows


def create_queue(input_path: Path, output_path: Path) -> dict[str, int]:
    """Creates a pending queue while preserving every source record verbatim."""
    rows = _read_jsonl(input_path)
    seen_ids: set[str] = set()
    queue: list[dict[str, Any]] = []
    for row in rows:
        example_id = _text(row.get("example_id", row.get("id")))
        if not example_id:
            raise ReviewQueueError("EXAMPLE_ID_REQUIRED")
        if example_id in seen_ids:
            raise ReviewQueueError(f"DUPLICATE_EXAMPLE_ID:{example_id}")
        seen_ids.add(example_id)
        queue.append(
            {
                "example_id": example_id,
                "source_dataset_version": _text(row.get("dataset_version", "dataset-v0.1-dev-synthetic")),
                "synthetic": bool(row.get("synthetic", False)),
                "scenario_group": _text(row.get("scenario_group", row.get("scenario_id"))),
                "review_item": {
                    "input": row.get("input", row.get("input_text", "")),
                    "context": row.get("retrieved_context", row.get("context", "")),
                    "ai_output": row.get("ai_output", row.get("output", "")),
                    "evidence": row.get("citations", row.get("evidence", [])),
                    "provenance": row.get("provenance", {}),
                },
                "original_record": row,
                "review": {
                    "decision": None,
                    "review_id": None,
                    "reviewer": None,
                    "reviewed_at": None,
                    "comments": None,
                    "corrected_output": None,
                    "correction_validated": False,
                },
                "review_state": "PENDING_HUMAN_REVIEW",
                "promotion_eligible": False,
            }
        )
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text("\n".join(json.dumps(item, ensure_ascii=True, sort_keys=True) for item in queue) + ("\n" if queue else ""), encoding="utf-8")
    return {"available": len(queue), "pending": len(queue), "approved": 0, "corrected": 0, "rejected": 0}


def _queue_counts(rows: list[dict[str, Any]]) -> dict[str, int]:
    counts = {"available": len(rows), "pending": 0, "approved": 0, "corrected": 0, "rejected": 0}
    for item in rows:
        decision = _text((item.get("review") or {}).get("decision")).upper()
        if decision == "APPROVE":
            counts["approved"] += 1
        elif decision == "CORRECT":
            counts["corrected"] += 1
        elif decision == "REJECT":
            counts["rejected"] += 1
        else:
            counts["pending"] += 1
    return counts


def record_decision(
    queue_path: Path,
    example_id: str,
    decision: str,
    review_id: str,
    reviewer: str,
    reviewed_at: str,
    comments: str,
    corrected_output: str = "",
    correction_validated: bool = False,
) -> dict[str, int]:
    """Records one explicit human decision without changing any dataset artifact."""
    rows = _read_jsonl(queue_path)
    target = next((item for item in rows if item.get("example_id") == example_id), None)
    if target is None:
        raise ReviewQueueError(f"EXAMPLE_NOT_FOUND:{example_id}")
    current_decision = _text((target.get("review") or {}).get("decision")).upper()
    if current_decision:
        raise ReviewQueueError(f"EXAMPLE_ALREADY_REVIEWED:{example_id}")
    decision = _text(decision).upper()
    if decision not in DECISIONS:
        raise ReviewQueueError("DECISION_MUST_BE_APPROVE_CORRECT_OR_REJECT")
    if not _text(review_id) or not _text(reviewer) or not _valid_timestamp(_text(reviewed_at)):
        raise ReviewQueueError("REVIEWER_ID_REVIEW_ID_AND_VALID_TIMESTAMP_REQUIRED")
    if decision == "REJECT" and not _text(comments):
        raise ReviewQueueError("REJECTION_REASON_REQUIRED")
    if decision == "CORRECT" and (not _text(corrected_output) or not correction_validated):
        raise ReviewQueueError("CORRECTED_OUTPUT_AND_VALIDATION_REQUIRED")
    target["review"] = {
        "decision": decision,
        "review_id": _text(review_id),
        "reviewer": _text(reviewer),
        "reviewed_at": _text(reviewed_at),
        "comments": _text(comments),
        "corrected_output": _text(corrected_output) or None,
        "correction_validated": correction_validated,
    }
    target["review_state"] = {"APPROVE": "HUMAN_APPROVED", "CORRECT": "HUMAN_CORRECTED", "REJECT": "HUMAN_REJECTED"}[decision]
    target["promotion_eligible"] = decision in {"APPROVE", "CORRECT"} and target.get("synthetic") is not True
    queue_path.write_text("\n".join(json.dumps(item, ensure_ascii=True, sort_keys=True) for item in rows) + "\n", encoding="utf-8")
    return _queue_counts(rows)


def _valid_timestamp(value: str) -> bool:
    try:
        datetime.fromisoformat(value.replace("Z", "+00:00"))
        return True
    except ValueError:
        return False


def _reviewed_record(item: dict[str, Any]) -> tuple[dict[str, Any] | None, str | None]:
    review = item.get("review") if isinstance(item.get("review"), dict) else {}
    decision = _text(review.get("decision")).upper()
    reviewer = _text(review.get("reviewer"))
    review_id = _text(review.get("review_id"))
    reviewed_at = _text(review.get("reviewed_at"))
    original = item.get("original_record") if isinstance(item.get("original_record"), dict) else {}
    if decision not in DECISIONS:
        return None, "pending_or_invalid_decision"
    if not reviewer or not review_id or not reviewed_at or not _valid_timestamp(reviewed_at):
        return None, "reviewer_metadata_missing_or_invalid"
    if decision == "CORRECT":
        corrected_output = _text(review.get("corrected_output"))
        if not corrected_output or review.get("correction_validated") is not True:
            return None, "correction_not_validated"
    if decision == "REJECT":
        return None, "rejected"
    promoted = dict(original)
    promoted["review_decision"] = "CORRECTED" if decision == "CORRECT" else "APPROVE"
    promoted["approval_status"] = "APPROVED_FOR_FUTURE_DATASET"
    promoted["training_eligible"] = True
    if decision == "CORRECT":
        promoted["human_edited_output"] = review["corrected_output"]
        promoted["accepted_output"] = review["corrected_output"]
        promoted["correction_validated"] = True
    provenance = dict(promoted.get("provenance")) if isinstance(promoted.get("provenance"), dict) else {}
    provenance.update({"review_id": review_id, "approved_at": reviewed_at, "reviewer": reviewer})
    promoted["provenance"] = provenance
    return promoted, None


def materialize_queue(queue_path: Path, output_dir: Path, report_path: Path) -> dict[str, Any]:
    """Materializes only explicitly reviewed, non-synthetic records via the existing builder."""
    rows = _read_jsonl(queue_path)
    approved_records: list[dict[str, Any]] = []
    counts = {"available": len(rows), "pending": 0, "approved": 0, "corrected": 0, "rejected": 0}
    failures: dict[str, int] = {}
    for item in rows:
        review = item.get("review") if isinstance(item.get("review"), dict) else {}
        decision = _text(review.get("decision")).upper()
        if decision == "REJECT":
            counts["rejected"] += 1
            continue
        if decision in {"APPROVE", "CORRECT"} and item.get("synthetic") is True:
            failures["synthetic_not_promotable"] = failures.get("synthetic_not_promotable", 0) + 1
            continue
        promoted, reason = _reviewed_record(item)
        if promoted is None:
            key = reason or "invalid_review"
            failures[key] = failures.get(key, 0) + 1
            if key == "pending_or_invalid_decision":
                counts["pending"] += 1
            continue
        approved_records.append(promoted)
        counts["corrected" if decision == "CORRECT" else "approved"] += 1
    if not approved_records:
        raise ReviewQueueError(json.dumps({"counts": counts, "validation_failures": failures}, sort_keys=True))
    from build_dataset_v01 import build_dataset

    staging = queue_path.with_suffix(".approved.jsonl")
    staging.write_text("\n".join(json.dumps(item, ensure_ascii=True, sort_keys=True) for item in approved_records) + "\n", encoding="utf-8")
    try:
        manifest = build_dataset(staging, output_dir, report_path)
    finally:
        staging.unlink(missing_ok=True)
    return {"counts": counts, "validation_failures": failures, "manifest": manifest}


def main() -> None:
    parser = argparse.ArgumentParser(description="Create or materialize the governed training review queue.")
    subparsers = parser.add_subparsers(dest="command", required=True)

    create = subparsers.add_parser("create-queue")
    create.add_argument("--input", type=Path, required=True)
    create.add_argument("--output", type=Path, required=True)

    promote = subparsers.add_parser("materialize")
    promote.add_argument("--queue", type=Path, required=True)
    promote.add_argument("--output-dir", type=Path, default=Path("ml-platform/training-pipelines/dataset-v0.1"))
    promote.add_argument("--report", type=Path, default=Path("DATASET_QUALITY_REPORT.md"))

    decide = subparsers.add_parser("decide")
    decide.add_argument("--queue", type=Path, required=True)
    decide.add_argument("--example-id", required=True)
    decide.add_argument("--decision", choices=sorted(DECISIONS), required=True)
    decide.add_argument("--review-id", required=True)
    decide.add_argument("--reviewer", required=True)
    decide.add_argument("--reviewed-at", required=True)
    decide.add_argument("--comments", default="")
    decide.add_argument("--corrected-output", default="")
    decide.add_argument("--correction-validated", action="store_true")

    args = parser.parse_args()
    if args.command == "create-queue":
        result = create_queue(args.input, args.output)
    elif args.command == "materialize":
        result = materialize_queue(args.queue, args.output_dir, args.report)
    else:
        result = record_decision(args.queue, args.example_id, args.decision, args.review_id, args.reviewer, args.reviewed_at, args.comments, args.corrected_output, args.correction_validated)
    print(json.dumps(result, sort_keys=True))


if __name__ == "__main__":
    main()
