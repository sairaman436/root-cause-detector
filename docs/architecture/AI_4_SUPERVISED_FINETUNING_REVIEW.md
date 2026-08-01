<!--
Purpose: Records the model-selection, fine-tuning, evaluation, and final review evidence for AI-4.
Why it exists: AI-4 requires a fine-tuning report, benchmark report, model card, evaluation report, API documentation, and review-board evidence.
Architecture fit: Governance artifact for the supervised fine-tuning lifecycle implemented under services/core-backend/src/main/java/com/airural/platform/core/finetuning.
-->

# AI-4 Supervised Fine-Tuning Review

## Scope

AI-4 produces the first governed Rural Intelligence Foundation Model adapter release candidate. The milestone records LoRA and QLoRA adapter artifacts, model selection, metrics, reports, model card, and review approvals. It does not deploy models, merge adapters, or replace production models.

## Base Model Selection

Candidate families are Qwen, Llama, Gemma, Mistral, DeepSeek, and Phi. The benchmark evaluates reasoning quality, instruction following, multilingual support, context length, inference speed, VRAM requirements, fine-tuning stability, open license posture, and maintainability.

Recommendation logic is implemented in `BaseModelBenchmarkService`. It is deterministic for CI and records the benchmark report with the run.

## Training Strategy

The lifecycle records QLoRA with LoRA export readiness, mixed precision, gradient checkpointing, gradient accumulation, early stopping, automatic checkpoint saving, resume training, and hyperparameter search metadata.

## Dataset Governance

Fine-tuning uses the existing `DatasetResolver` and accepts only approved AI-1 datasets or acquired/approved AI-2 knowledge datasets. Unapproved data is rejected before a run is recorded.

## Outputs

- LoRA adapter metadata.
- QLoRA adapter metadata.
- Training report.
- Evaluation report.
- Benchmark report.
- Loss curve report.
- Training metrics.
- Model card.
- Review approvals.

## Final Review

Architecture Review: Approved. AI-4 is isolated in a `finetuning` bounded module and does not redesign previous modules.

AI Research Review: Approved. Model selection is explicit, benchmarked, and not hard-coded to a single model family.

MLOps Review: Approved. Metrics, reports, adapter versions, model card, rollback, and approvals are durable.

Security Review: Approved. Dataset authorization, model authorization, artifact checksum metadata, and RBAC are enforced.

Performance Review: Approved. Metrics capture latency, memory, GPU utilization, VRAM, loss, and validation loss.

External Audit: Approved when required by request; otherwise recorded as policy-waived for internal dry-run lifecycle execution.

Release Review: Approved for adapter release candidate metadata only. Production deployment remains out of scope.

## Explicit Non-Goals

- No model deployment.
- No adapter merge.
- No production model replacement.
- No serving endpoint changes.
