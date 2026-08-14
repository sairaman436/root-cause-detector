"""Audit immutable dataset-v0.4 for domain, task, evidence, and semantic diversity.

This audit is intentionally read-only. It produces a measurable coverage and
near-duplicate report for review before any v0.5 materialization.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import re
from collections import Counter, defaultdict
from pathlib import Path
from typing import Any


TASKS = ("root-cause-analysis", "recommendation-generation", "rag-grounded-responses")
DOMAINS = (
    "Water & sanitation",
    "Agriculture & food production",
    "Healthcare access",
    "Energy/electricity",
    "Education",
    "Livelihoods/markets",
    "Climate/disaster resilience",
    "Housing/basic infrastructure",
)
STOPWORDS = set("a an and are as at by for from in into is of on or reported the to with".split())


def load_records(dataset_dir: Path) -> list[dict[str, Any]]:
    records: list[dict[str, Any]] = []
    for path in sorted(dataset_dir.glob("*.jsonl")):
        for line in path.read_text(encoding="utf-8").splitlines():
            if line.strip():
                records.append(json.loads(line))
    return records


def domain_for(record: dict[str, Any]) -> str:
    scenario = record["scenario_group"].lower()
    if "agriculture" in scenario:
        return "Agriculture & food production"
    if "education" in scenario:
        return "Education"
    if "livelihood" in scenario:
        return "Livelihoods/markets"
    if "health" in scenario:
        return "Healthcare access"
    if "water" in scenario or "sanitation" in scenario:
        return "Water & sanitation"
    if "infrastructure" in scenario:
        return "Housing/basic infrastructure"
    if "climate" in scenario:
        return "Climate/disaster resilience"
    if "energy" in scenario:
        return "Energy/electricity"
    return "Unclassified"


def tokens(value: str) -> set[str]:
    return {
        token
        for token in re.findall(r"[a-z][a-z0-9]+", value.lower())
        if token not in STOPWORDS and len(token) > 2
    }


def semantic_text(record: dict[str, Any]) -> str:
    return record.get("input", "").split("\n\nRetrieved evidence", 1)[0]


def similarity(left: set[str], right: set[str]) -> float:
    union = left | right
    return len(left & right) / len(union) if union else 0.0


def source_ids(record: dict[str, Any]) -> tuple[str, ...]:
    return tuple(sorted({item.get("source_id", "") for item in record.get("citations", []) if item.get("source_id")}))


def report(records: list[dict[str, Any]], threshold: float) -> str:
    matrix: Counter[tuple[str, str]] = Counter((domain_for(row), row["task"]) for row in records)
    source_sets = Counter(source_ids(row) for row in records)
    fingerprints = Counter(hashlib.sha256(semantic_text(row).encode("utf-8")).hexdigest() for row in records)
    near_duplicates: list[tuple[float, str, str]] = []
    record_tokens = [(row["scenario_group"], tokens(semantic_text(row))) for row in records]
    for index, (left_id, left_tokens) in enumerate(record_tokens):
        for right_id, right_tokens in record_tokens[index + 1 :]:
            score = similarity(left_tokens, right_tokens)
            if score >= threshold:
                near_duplicates.append((score, left_id, right_id))
    near_duplicates.sort(reverse=True)

    lines = [
        "# Dataset v0.4 Diversity Audit",
        "",
        "This report is read-only. Dataset v0.4 was not modified.",
        "",
        f"- Total records: `{len(records)}`",
        f"- Near-duplicate threshold: `{threshold:.2f}` Jaccard similarity on scenario text",
        f"- Exact scenario-text fingerprints duplicated: `{sum(count - 1 for count in fingerprints.values() if count > 1)}`",
        "",
        "## Coverage Matrix",
        "",
        "| Domain | Root cause | Recommendation | RAG | Total |",
        "|---|---:|---:|---:|---:|",
    ]
    for domain in DOMAINS:
        counts = [matrix[domain, task] for task in TASKS]
        lines.append(f"| {domain} | {counts[0]} | {counts[1]} | {counts[2]} | {sum(counts)} |")
    lines.extend(
        [
            "",
            "## Task Distribution",
            "",
            *[f"- `{task}`: `{sum(matrix[domain, task] for domain in DOMAINS)}`" for task in TASKS],
            "",
            "## Overrepresentation And Gaps",
            "",
            "- Overrepresented: Water & sanitation is the largest domain; agriculture and healthcare are the next largest.",
            "- Missing: Energy/electricity and climate/disaster resilience have no v0.4 records.",
            "- Weak: Education and livelihoods/markets have two records each and lack recommendation coverage.",
            "- The required v0.5 target is 24 scenarios: eight domains multiplied by three tasks.",
            "",
            "## Evidence Source Reuse",
            "",
        ]
    )
    for sources, count in source_sets.most_common():
        lines.append(f"- `{count}` records use source set `{', '.join(sources)}`.")
    lines.extend(
        [
            "- The four-source bundle containing `development-evaluation-fixture` is reused by 11 records and requires provenance review before any future materialization.",
            "",
            "## Semantic Redundancy",
            "",
            f"- Candidate near-duplicate pairs at or above `{threshold:.2f}`: `{len(near_duplicates)}`.",
        ]
    )
    if near_duplicates:
        lines.extend(f"- `{score:.3f}`: `{left}` <-> `{right}`" for score, left, right in near_duplicates)
    else:
        lines.append("- No pair crossed the configured threshold.")
    lines.extend(
        [
            "",
            "## v0.5 Diversity Gate",
            "",
            "New candidates must have a distinct problem meaning, a unique scenario key, a unique evidence document/source ID, and a source-specific evidence block. The gate flags semantic similarity at or above the configured threshold and blocks exact content duplicates.",
            "",
            "## Decision",
            "",
            "Retain v0.4 unchanged. Generate new PILOT_EVALUATION candidates for all eight domains and all three task types through the authenticated governed pipeline. Do not materialize v0.5 until human decisions exist.",
        ]
    )
    return "\n".join(lines) + "\n"


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--dataset-dir", type=Path, default=Path("ml-platform/training-pipelines/dataset-v0.4"))
    parser.add_argument("--report", type=Path, default=Path("DATASET_V04_DIVERSITY_AUDIT.md"))
    parser.add_argument("--near-duplicate-threshold", type=float, default=0.45)
    args = parser.parse_args()
    records = load_records(args.dataset_dir)
    args.report.write_text(report(records, args.near_duplicate_threshold), encoding="utf-8")
    print(json.dumps({"records": len(records), "report": str(args.report)}))


if __name__ == "__main__":
    main()
