"""Contract tests for the dataset-v0.3 training/evaluation boundary."""

import json
import sys
from pathlib import Path


PIPELINE_ROOT = Path(__file__).parents[2] / "ml-platform" / "training-pipelines"
sys.path.insert(0, str(PIPELINE_ROOT))

from contracts.dataset_v03_contract import (  # noqa: E402
    DATASET_VERSION,
    format_inference_example,
    format_inference_repair_example,
    format_training_example,
    validate_generated_target,
    validate_record,
    v03_json_schema,
)


SOURCE_IDS = [
    {"source_id": "pilot.water.circular.001"},
    {"source_id": "pilot.water.circular.002"},
]


def _target(task: str) -> dict:
    citations = [{"source_id": "pilot.water.circular.001"}]
    if task == "root-cause-analysis":
        return {
            "summary": "Water supply interruptions are associated with pump downtime.",
            "root_causes": [
                {
                    "name": "Pump downtime",
                    "description": "The pump is unavailable during reported interruptions.",
                    "evidence_source_ids": ["pilot.water.circular.001"],
                    "confidence": 0.8,
                }
            ],
            "uncertainties": ["Maintenance records were not available."],
            "citations": citations,
        }
    if task == "recommendation-generation":
        option = {
            "id": "option-1",
            "title": "Schedule preventive maintenance",
            "description": "Inspect the pump on a fixed schedule.",
            "evidence_source_ids": ["pilot.water.circular.001"],
            "feasibility": {"rating": "HIGH", "rationale": "Local technicians can perform inspections."},
            "risks": [{"description": "Missed inspections", "severity": "LOW", "mitigation": "Track visits."}],
            "implementation_steps": ["Assign a technician", "Record each inspection"],
        }
        return {
            "root_cause": {
                "description": "Pump downtime contributes to interruptions.",
                "evidence_source_ids": ["pilot.water.circular.001"],
            },
            "recommendations": [option, {**option, "id": "option-2", "title": "Keep a spare component"}],
            "uncertainties": ["Spare-part availability is unknown."],
            "citations": citations,
        }
    return {
        "answer": "The supplied guidance links interruptions to pump downtime.",
        "uncertainties": ["The duration of downtime is not specified."],
        "citations": citations,
    }


def _record(task: str = "root-cause-analysis") -> dict:
    target = _target(task)
    return {
        "dataset_version": DATASET_VERSION,
        "example_id": "pilot-example-001",
        "scenario_group": "pilot-water-001",
        "task": task,
        "input": "Assess the reported water interruptions.\n\nRetrieved evidence and citation context:\n" + json.dumps(SOURCE_IDS),
        "output": json.dumps(target),
        "citations": SOURCE_IDS,
        "synthetic": False,
    }


def test_valid_records_cover_all_canonical_tasks():
    for task in ("root-cause-analysis", "recommendation-generation", "rag-grounded-responses"):
        assert validate_record(_record(task)) == []


def test_malformed_target_is_rejected():
    record = _record()
    record["output"] = '{"summary":'
    assert "output:malformed_json" in validate_record(record)


def test_target_source_id_must_exist_in_input_context():
    record = _record()
    record["output"] = json.dumps({**_target("root-cause-analysis"), "citations": [{"source_id": "unknown.source.001"}]})
    errors = validate_record(record)
    assert "citation_contract:target_source_id_not_in_input:unknown.source.001" in errors


def test_retrieved_context_is_required():
    record = _record()
    record["input"] = "Assess the reported water interruptions."
    assert "input:missing_retrieved_evidence_context" in validate_record(record)


def test_uuid_provenance_is_not_treated_as_pii():
    record = _record()
    record["example_id"] = "pilot-550e8400-e29b-41d4-a716-446655440000"
    assert "privacy:pii_detected" not in validate_record(record)


def test_oversized_output_is_rejected():
    record = _record()
    record["output"] = json.dumps({**_target("rag-grounded-responses"), "answer": "word " * 1025})
    assert "output:exceeds_sequence_length" in validate_record(record, sequence_length=1024)


def test_generated_target_uses_only_input_source_ids():
    output = json.dumps(_target("rag-grounded-responses"))
    assert validate_generated_target("rag-grounded-responses", output, {"pilot.water.circular.001"}) == []
    assert validate_generated_target("rag-grounded-responses", output, {"pilot.water.circular.002"})


def test_training_formatter_includes_context_and_generation_contract():
    class Tokenizer:
        def apply_chat_template(self, messages, tokenize=False, add_generation_prompt=False):
            assert "Retrieved evidence and citation context:" in messages[1]["content"]
            assert add_generation_prompt is True
            return "formatted"

    assert format_training_example(Tokenizer(), _record(), include_target=False) == "formatted"


def test_inference_formatter_includes_exact_schema_and_allowed_source_ids():
    class Tokenizer:
        def apply_chat_template(self, messages, tokenize=False, add_generation_prompt=False):
            assert add_generation_prompt is True
            user_prompt = messages[1]["content"]
            assert '"evidence_source_ids"' in user_prompt
            assert '"source_id"' in user_prompt
            assert "pilot.water.circular.001" in user_prompt
            assert "Return one JSON object only." in user_prompt
            return "formatted"

    assert format_inference_example(Tokenizer(), _record()) == "formatted"


def test_inference_repair_formatter_preserves_strict_errors():
    class Tokenizer:
        def apply_chat_template(self, messages, tokenize=False, add_generation_prompt=False):
            assert add_generation_prompt is True
            prompt = messages[1]["content"]
            assert "Validation errors" in prompt
            assert "output.citations" in prompt
            assert "Previous response" in prompt
            return "repair-formatted"

    assert format_inference_repair_example(
        Tokenizer(), _record(), ["output.citations:required_nonempty_array"], '{"summary":"bad"}'
    ) == "repair-formatted"


def test_v03_json_schema_enforces_dynamic_source_ids():
    schema = v03_json_schema("rag-grounded-responses", {"pilot.water.circular.001"})
    assert schema["additionalProperties"] is False
    assert schema["properties"]["citations"]["items"]["properties"]["source_id"]["enum"] == [
        "pilot.water.circular.001"
    ]
