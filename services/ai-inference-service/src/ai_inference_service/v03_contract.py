"""
Purpose: Defines the runtime v0.3 JSON Schema and source-ID validation boundary.
Why it exists: The local inference service must enforce the same structured contract used by the training and evaluation pipeline.
Architecture fit: Shared application contract for constrained Qwen/Ollama generation and response validation.
"""

from __future__ import annotations

from typing import Any


def _source(source_ids: list[str]) -> dict[str, Any]:
    if not source_ids:
        raise ValueError("V03_SOURCE_IDS_REQUIRED")
    return {"type": "string", "enum": source_ids}


def _citations(source_ids: list[str]) -> dict[str, Any]:
    return {
        "type": "array",
        "minItems": 1,
        "items": {
            "type": "object",
            "additionalProperties": False,
            "required": ["source_id"],
            "properties": {"source_id": _source(source_ids)},
        },
    }


def build_schema(task: str, source_ids: set[str]) -> dict[str, Any]:
    """Build a strict v0.3 schema with a row-specific source-ID enum."""

    allowed = sorted(source_ids)
    source = _source(allowed)
    citations = _citations(allowed)
    strings = {"type": "array", "minItems": 1, "items": {"type": "string", "minLength": 1}}
    if task == "rag-grounded-responses":
        return {
            "type": "object",
            "additionalProperties": False,
            "required": ["answer", "uncertainties", "citations"],
            "properties": {
                "answer": {"type": "string", "minLength": 1},
                "uncertainties": strings,
                "citations": citations,
            },
        }
    if task == "root-cause-analysis":
        root_cause = {
            "type": "object",
            "additionalProperties": False,
            "required": ["name", "description", "evidence_source_ids", "confidence"],
            "properties": {
                "name": {"type": "string", "minLength": 1},
                "description": {"type": "string", "minLength": 1},
                "evidence_source_ids": {"type": "array", "minItems": 1, "items": source},
                "confidence": {"type": "number", "minimum": 0, "maximum": 1},
            },
        }
        return {
            "type": "object",
            "additionalProperties": False,
            "required": ["summary", "root_causes", "uncertainties", "citations"],
            "properties": {
                "summary": {"type": "string", "minLength": 1},
                "root_causes": {"type": "array", "minItems": 1, "items": root_cause},
                "uncertainties": strings,
                "citations": citations,
            },
        }
    if task == "recommendation-generation":
        recommendation = {
            "type": "object",
            "additionalProperties": False,
            "required": ["id", "title", "description", "evidence_source_ids", "feasibility", "risks", "implementation_steps"],
            "properties": {
                "id": {"type": "string", "minLength": 1},
                "title": {"type": "string", "minLength": 1},
                "description": {"type": "string", "minLength": 1},
                "evidence_source_ids": {"type": "array", "minItems": 1, "items": source},
                "feasibility": {
                    "type": "object",
                    "additionalProperties": False,
                    "required": ["rating", "rationale"],
                    "properties": {
                        "rating": {"type": "string", "enum": ["LOW", "MODERATE", "HIGH", "UNKNOWN"]},
                        "rationale": {"type": "string", "minLength": 1},
                    },
                },
                "risks": {
                    "type": "array",
                    "minItems": 1,
                    "items": {
                        "type": "object",
                        "additionalProperties": False,
                        "required": ["description", "severity", "mitigation"],
                        "properties": {
                            "description": {"type": "string", "minLength": 1},
                            "severity": {"type": "string", "enum": ["LOW", "MEDIUM", "HIGH", "UNKNOWN"]},
                            "mitigation": {"type": "string", "minLength": 1},
                        },
                    },
                },
                "implementation_steps": strings,
            },
        }
        return {
            "type": "object",
            "additionalProperties": False,
            "required": ["root_cause", "recommendations", "uncertainties", "citations"],
            "properties": {
                "root_cause": {
                    "type": "object",
                    "additionalProperties": False,
                    "required": ["description", "evidence_source_ids"],
                    "properties": {
                        "description": {"type": "string", "minLength": 1},
                        "evidence_source_ids": {"type": "array", "minItems": 1, "items": source},
                    },
                },
                "recommendations": {"type": "array", "minItems": 2, "items": recommendation},
                "uncertainties": strings,
                "citations": citations,
            },
        }
    raise ValueError(f"V03_UNSUPPORTED_TASK:{task}")


def validate_payload(payload: Any, schema: dict[str, Any]) -> list[str]:
    """Validate a generated object without normalizing or fabricating fields."""

    from jsonschema import Draft202012Validator

    return [error.message for error in Draft202012Validator(schema).iter_errors(payload)]

