# Model Evaluation Methodology

## Document Control

| Field | Value |
|---|---|
| Methodology ID | `MODEL-EVALUATION-METHODOLOGY` |
| Methodology version | `1.0.0` |
| Output contract | `dataset-v0.3` |
| Human rubric | `HUMAN-QUALITY-RUBRIC@1.0.0` |
| Evaluation framework | `ml-platform/evaluation/evaluation-framework.yaml` |
| Status | Approved for controlled Base-versus-Fine-tuned comparison after coverage gates pass |

## Purpose

This methodology defines the single evaluation contract for comparing the
production Base Qwen model with a future fine-tuned Qwen adapter. It combines
the existing automated v0.3 contract gates with blinded human quality scoring.
It exists because valid JSON and source-ID validity establish structural
correctness, but do not establish causal quality, evidence faithfulness,
usefulness, or safe uncertainty handling.

The methodology does not replace the existing structural or citation contract,
does not change model prompts, and does not permit test-set modification after
the comparison begins.

Related controls:

- `ml-platform/training-pipelines/contracts/dataset_v03_contract.py` defines the
  immutable output and source-ID rules.
- `ml-platform/evaluation/rubrics/human-quality-rubric-v1.0.yaml` defines the
  human scoring anchors and aggregation.
- `BASELINE_EVALUATION_REPORT.md` records the structural-only baseline run that
  preceded this rubric version.
- `DATASET_V03_QUALITY_REPORT.md` records dataset provenance and split gates.

## Evaluation Identity

Every run must persist or report this complete identity tuple:

| Field | Required value/source |
|---|---|
| `model_version` | Exact Ollama/model registry identifier, such as `qwen2.5:0.5b` |
| `dataset_version` | Immutable dataset version, currently `dataset-v0.3` |
| `prompt_version` | Exact prompt registry or formatter version used for the request |
| `inference_configuration` | Provider, constrained-decoding mode, temperature, timeout, context limit, and fallback mode |
| `evaluation_version` | `MODEL-EVALUATION-METHODOLOGY@1.0.0` |
| `rubric_version` | `HUMAN-QUALITY-RUBRIC@1.0.0` when human scoring is performed |
| `example_id` | Immutable held-out example identifier |
| `output_sha256` | Hash of the exact canonical output scored |

An execution identifier may be added for traceability but does not substitute
for `evaluation_version` or `rubric_version`.

## Evaluation Stages

Each model is evaluated independently against the same immutable TEST examples.
The order, prompts, evidence, allowed source IDs, generation settings, timeout,
and output validators are held constant.

1. Load only `dataset-v0.3` TEST rows.
2. Verify the dataset digest, split isolation, scenario grouping, and v0.3
   record validation before inference.
3. Render the same v0.3 input for each model.
4. Run constrained generation with no unconstrained fallback.
5. Record provider errors, timeout, latency, memory telemetry when available,
   repair attempts, and exact output hash.
6. Apply the existing automated gates: JSON parsing, canonical schema,
   required fields, allowed source IDs, evidence context, PII, and output
   limits.
7. Present structurally valid outputs to independent human reviewers using the
   rubric. Reviewers see the same task/input/evidence context and do not see
   model identity or comparison order.
8. Aggregate scores per the rubric and retain reviewer-level records. Never
   replace reviewer scores with an AI judge score.
9. Compare models by task and dimension. Report denominators, missing tasks,
   failures, and confidence limitations without imputing absent metrics.

## Held-Out Coverage Gate

The TEST split must contain at least one independently approved example for
each required task:

- root-cause analysis: required
- recommendation generation: required
- RAG-grounded response: required

Current `dataset-v0.3` coverage is:

| Task | TEST count | Status |
|---|---:|---|
| Root cause | 1 | Present |
| Recommendation | 0 | Blocking gap |
| RAG | 2 | Present |

The existing approved recommendation records are not eligible to fill this
gap if they were used in TRAIN or VALIDATION. Reusing them would introduce
split leakage.

The required replacement path is:

1. Register a new, distinct `PILOT_EVALUATION` recommendation scenario with
   evidence, source IDs, provenance, and a bounded canonical target.
2. Execute it through the existing pilot evaluation endpoint and Qwen/RAG
   pipeline.
3. Generate the existing training candidate only after evaluation and PII/
   quality gates pass.
4. Leave it `PENDING` in the authenticated Training Review queue.
5. Require explicit human `APPROVE` or validated `CORRECT` before dataset
   materialization.
6. Assign it to TEST only during a new immutable dataset build, keeping its
   scenario group out of TRAIN and VALIDATION.

No synthetic development fixture may satisfy this gate. No record is approved
or promoted by this methodology.

## Human Review Protocol

Human review is a measurement activity, not an approval shortcut.

- Use at least two independent reviewers for every model output.
- Blind model identity, training status, and presentation order.
- Show the problem, retrieved evidence, citation context, allowed source IDs,
  task, and exact candidate output.
- Score every applicable rubric dimension from 0 to 4.
- Record evidence references used and concise rationale for every score.
- Trigger a third-person adjudication when any dimension differs by more than
  one point.
- Keep candidate approval/rejection separate from model-quality scoring.
- Do not score or promote records containing PII, invalid citations, malformed
  JSON, or unresolved provenance failures.

## Metrics and Interpretation

Automated metrics remain binary/count metrics from the v0.3 contract. Human
metrics are normalized to 0-1 only after reviewer reconciliation:

- Root-cause quality: rubric dimension score.
- Recommendation quality: rubric dimension score; not applicable when no
  recommendation TEST row exists.
- RAG/evidence quality: rubric dimension score plus existing source-ID validity.
- Uncertainty handling: rubric dimension score plus required-field presence.
- Practical usefulness: rubric dimension score.
- Unsupported claims: recorded as evidence-quality failures and reviewer notes;
  no claim rate is reported unless reviewers adjudicate claim counts using the
  same procedure for both models.

A model comparison is `COMPARABLE` only when all three task coverage gates pass,
both models are evaluated on every applicable TEST row, and structural and
human-review records are complete. A result can still be statistically weak
because of small sample size; the report must state that limitation.

## Current Status

- Rubric: created and versioned as `HUMAN-QUALITY-RUBRIC@1.0.0`.
- Methodology: versioned as `MODEL-EVALUATION-METHODOLOGY@1.0.0`.
- Structural contract: unchanged.
- Dataset-v0.3: unchanged and immutable.
- New recommendation holdout: required, not created by this documentation
  change, and must remain pending authenticated human review until explicitly
  decided.
- Experiment 004: blocked until the recommendation TEST coverage gate and
  human-review records are complete.
