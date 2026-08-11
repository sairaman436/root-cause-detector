"""
Purpose: Defines and validates the canonical dataset-v0.3 task contract.
Why it exists: Training and evaluation must agree on one structured-output and
source-grounding protocol before another adapter experiment is permitted.
Architecture fit: Shared MLOps contract boundary used by dataset validation,
Qwen prompt construction, and future evaluation workers.
"""

from __future__ import annotations

import json
import re
from pathlib import Path
from typing import Any


CONTRACT_PATH = Path(__file__).with_name("dataset-v0.3-contract.json")
DATASET_VERSION = "dataset-v0.3"
SEQUENCE_LENGTH = 1024
SOURCE_ID = re.compile(r"^[A-Za-z0-9][A-Za-z0-9._:-]{2,127}$")
EMAIL = re.compile(r"[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}")
PHONE = re.compile(r"(?<!\d)(?:\+?\d{10,12})(?!\d)")
UUID = re.compile(r"(?i)\b[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\b")


def load_contract() -> dict[str, Any]:
    return json.loads(CONTRACT_PATH.read_text(encoding="utf-8"))


def _error(path: str, message: str) -> str:
    return f"{path}:{message}"


def _string(value: Any, path: str, errors: list[str]) -> None:
    if not isinstance(value, str) or not value.strip():
        errors.append(_error(path, "required_nonempty_string"))


def _source_ids(value: Any, path: str, errors: list[str]) -> list[str]:
    if not isinstance(value, list) or not value:
        errors.append(_error(path, "required_nonempty_array"))
        return []
    result: list[str] = []
    for index, source_id in enumerate(value):
        if not isinstance(source_id, str) or not SOURCE_ID.fullmatch(source_id):
            errors.append(_error(f"{path}[{index}]", "invalid_source_id"))
        else:
            result.append(source_id)
    return result


def _citations(value: Any, path: str, errors: list[str]) -> list[str]:
    if not isinstance(value, list) or not value:
        errors.append(_error(path, "required_nonempty_array"))
        return []
    ids: list[str] = []
    for index, citation in enumerate(value):
        if not isinstance(citation, dict) or set(citation) != {"source_id"}:
            errors.append(_error(f"{path}[{index}]", "citation_must_only_contain_source_id"))
            continue
        ids.extend(_source_ids([citation.get("source_id")], f"{path}[{index}].source_id", errors))
    return ids


def _object_keys(value: dict[str, Any], required: set[str], path: str, errors: list[str]) -> None:
    missing = required - set(value)
    extra = set(value) - required
    for key in sorted(missing):
        errors.append(_error(path, f"missing_{key}"))
    for key in sorted(extra):
        errors.append(_error(path, f"unexpected_{key}"))


def _validate_target(task: str, target: Any) -> tuple[list[str], list[str]]:
    errors: list[str] = []
    target_ids: list[str] = []
    if not isinstance(target, dict):
        return ["output:must_be_json_object"], target_ids
    if task == "root-cause-analysis":
        _object_keys(target, {"summary", "root_causes", "uncertainties", "citations"}, "output", errors)
        _string(target.get("summary"), "output.summary", errors)
        causes = target.get("root_causes")
        if not isinstance(causes, list) or not causes:
            errors.append("output.root_causes:required_nonempty_array")
        else:
            for index, cause in enumerate(causes):
                path = f"output.root_causes[{index}]"
                if not isinstance(cause, dict):
                    errors.append(_error(path, "must_be_object"))
                    continue
                _object_keys(cause, {"name", "description", "evidence_source_ids", "confidence"}, path, errors)
                _string(cause.get("name"), f"{path}.name", errors)
                _string(cause.get("description"), f"{path}.description", errors)
                target_ids.extend(_source_ids(cause.get("evidence_source_ids"), f"{path}.evidence_source_ids", errors))
                if not isinstance(cause.get("confidence"), (int, float)) or not 0 <= cause["confidence"] <= 1:
                    errors.append(_error(f"{path}.confidence", "must_be_number_between_zero_and_one"))
        uncertainties = target.get("uncertainties")
        if not isinstance(uncertainties, list) or not uncertainties or any(not isinstance(item, str) or not item.strip() for item in uncertainties):
            errors.append("output.uncertainties:required_nonempty_string_array")
        target_ids.extend(_citations(target.get("citations"), "output.citations", errors))
    elif task == "recommendation-generation":
        _object_keys(target, {"root_cause", "recommendations", "uncertainties", "citations"}, "output", errors)
        root_cause = target.get("root_cause")
        if not isinstance(root_cause, dict):
            errors.append("output.root_cause:must_be_object")
        else:
            _object_keys(root_cause, {"description", "evidence_source_ids"}, "output.root_cause", errors)
            _string(root_cause.get("description"), "output.root_cause.description", errors)
            target_ids.extend(_source_ids(root_cause.get("evidence_source_ids"), "output.root_cause.evidence_source_ids", errors))
        recommendations = target.get("recommendations")
        if not isinstance(recommendations, list) or len(recommendations) < 2:
            errors.append("output.recommendations:minimum_two_options_required")
        else:
            for index, recommendation in enumerate(recommendations):
                path = f"output.recommendations[{index}]"
                if not isinstance(recommendation, dict):
                    errors.append(_error(path, "must_be_object"))
                    continue
                _object_keys(recommendation, {"id", "title", "description", "evidence_source_ids", "feasibility", "risks", "implementation_steps"}, path, errors)
                for key in ("id", "title", "description"):
                    _string(recommendation.get(key), f"{path}.{key}", errors)
                target_ids.extend(_source_ids(recommendation.get("evidence_source_ids"), f"{path}.evidence_source_ids", errors))
                feasibility = recommendation.get("feasibility")
                if not isinstance(feasibility, dict) or set(feasibility) != {"rating", "rationale"} or feasibility.get("rating") not in {"LOW", "MODERATE", "HIGH", "UNKNOWN"}:
                    errors.append(_error(f"{path}.feasibility", "invalid_feasibility"))
                elif not isinstance(feasibility.get("rationale"), str) or not feasibility["rationale"].strip():
                    errors.append(_error(f"{path}.feasibility.rationale", "required_nonempty_string"))
                risks = recommendation.get("risks")
                if not isinstance(risks, list) or not risks:
                    errors.append(_error(f"{path}.risks", "required_nonempty_array"))
                else:
                    for risk_index, risk in enumerate(risks):
                        risk_path = f"{path}.risks[{risk_index}]"
                        if not isinstance(risk, dict) or set(risk) != {"description", "severity", "mitigation"} or risk.get("severity") not in {"LOW", "MEDIUM", "HIGH", "UNKNOWN"}:
                            errors.append(_error(risk_path, "invalid_risk"))
                        else:
                            for key in ("description", "mitigation"):
                                _string(risk.get(key), f"{risk_path}.{key}", errors)
                steps = recommendation.get("implementation_steps")
                if not isinstance(steps, list) or not steps or any(not isinstance(step, str) or not step.strip() for step in steps):
                    errors.append(_error(f"{path}.implementation_steps", "required_nonempty_string_array"))
        uncertainties = target.get("uncertainties")
        if not isinstance(uncertainties, list) or not uncertainties or any(not isinstance(item, str) or not item.strip() for item in uncertainties):
            errors.append("output.uncertainties:required_nonempty_string_array")
        target_ids.extend(_citations(target.get("citations"), "output.citations", errors))
    elif task == "rag-grounded-responses":
        _object_keys(target, {"answer", "uncertainties", "citations"}, "output", errors)
        _string(target.get("answer"), "output.answer", errors)
        uncertainties = target.get("uncertainties")
        if not isinstance(uncertainties, list) or not uncertainties or any(not isinstance(item, str) or not item.strip() for item in uncertainties):
            errors.append("output.uncertainties:required_nonempty_string_array")
        target_ids.extend(_citations(target.get("citations"), "output.citations", errors))
    else:
        errors.append("task:unsupported_task")
    return errors, target_ids


def _contains_pii(value: Any) -> bool:
    if isinstance(value, str):
        pii_text = UUID.sub("", value)
        return bool(EMAIL.search(pii_text) or PHONE.search(pii_text))
    if isinstance(value, dict):
        return any(_contains_pii(item) for item in value.values())
    if isinstance(value, list):
        return any(_contains_pii(item) for item in value)
    return False


def validate_record(record: dict[str, Any], sequence_length: int = SEQUENCE_LENGTH) -> list[str]:
    errors: list[str] = []
    if not isinstance(record, dict):
        return ["record:must_be_object"]
    if record.get("dataset_version") != DATASET_VERSION:
        errors.append("dataset_version:mismatch")
    task = record.get("task")
    if task not in {"root-cause-analysis", "recommendation-generation", "rag-grounded-responses"}:
        errors.append("task:unsupported_task")
    for key in ("example_id", "scenario_group", "input", "output"):
        if not isinstance(record.get(key), str) or not record[key].strip():
            errors.append(f"{key}:required_nonempty_string")
    if record.get("synthetic") is True:
        errors.append("synthetic:production_record_forbidden")
    citations = record.get("citations")
    input_source_ids: set[str] = set()
    if not isinstance(citations, list) or not citations:
        errors.append("citations:required_nonempty_array")
    else:
        for index, citation in enumerate(citations):
            if not isinstance(citation, dict) or not isinstance(citation.get("source_id"), str) or not SOURCE_ID.fullmatch(citation["source_id"]):
                errors.append(f"citations[{index}]:invalid_source_id")
            else:
                input_source_ids.add(citation["source_id"])
    try:
        target = json.loads(record.get("output", ""))
    except (TypeError, json.JSONDecodeError):
        errors.append("output:malformed_json")
        target = None
    target_errors, target_source_ids = _validate_target(str(task), target)
    errors.extend(target_errors)
    for source_id in sorted(set(target_source_ids) - input_source_ids):
        errors.append(f"citation_contract:target_source_id_not_in_input:{source_id}")
    if len(str(record.get("output", "")).split()) > sequence_length:
        errors.append("output:exceeds_sequence_length")
    if "Retrieved evidence and citation context" not in str(record.get("input", "")):
        errors.append("input:missing_retrieved_evidence_context")
    if _contains_pii(record):
        errors.append("privacy:pii_detected")
    return sorted(set(errors))


def validate_generated_target(task: str, output_text: str, input_source_ids: set[str]) -> list[str]:
    """Applies the output and source-ID contract to an inference result."""
    try:
        target = json.loads(output_text)
    except (TypeError, json.JSONDecodeError):
        return ["output:malformed_json"]
    errors, target_source_ids = _validate_target(task, target)
    for source_id in sorted(set(target_source_ids) - input_source_ids):
        errors.append(f"citation_contract:target_source_id_not_in_input:{source_id}")
    return sorted(set(errors))


def task_instruction(task: str) -> str:
    return {
        "root-cause-analysis": "Return only the canonical root-cause JSON object with summary, root_causes, uncertainties, and citations.",
        "recommendation-generation": "Return only the canonical recommendation JSON object with root_cause, at least two recommendations, uncertainties, and citations.",
        "rag-grounded-responses": "Return only the canonical RAG JSON object with answer, uncertainties, and citations.",
    }[task]


def format_training_example(tokenizer: Any, row: dict[str, Any], include_target: bool = True) -> str:
    task = str(row["task"])
    citation_context = json.dumps(row["citations"], ensure_ascii=False, sort_keys=True)
    messages = [
        {"role": "system", "content": "You are a rural decision intelligence assistant. Use only the supplied evidence. Never invent source IDs or facts."},
        {"role": "user", "content": f"Task: {task}\nInput: {row['input']}\nRetrieved evidence and citation context:\n{citation_context}\n{task_instruction(task)}"},
    ]
    if include_target:
        messages.append({"role": "assistant", "content": row["output"]})
    if hasattr(tokenizer, "apply_chat_template"):
        return tokenizer.apply_chat_template(messages, tokenize=False, add_generation_prompt=not include_target)
    suffix = f"\nAssistant: {row['output']}" if include_target else "\nAssistant:"
    return f"System: {messages[0]['content']}\nUser: {messages[1]['content']}" + suffix
