<!--
Purpose: Records the architecture design review and post-implementation review for AI-3 Enterprise Model Training Factory.
Why it exists: AI-3 requires formal Architecture, ML, Security, Performance, and MLOps review evidence before the milestone is considered complete.
Architecture fit: Governance artifact for the training factory module implemented under services/core-backend/src/main/java/com/airural/platform/core/training.
-->

# AI-3 Enterprise Model Training Factory Review

## Scope

AI-3 builds the infrastructure required for future Rural Intelligence Foundation Model fine-tuning. It does not train production models, run LoRA or QLoRA, merge adapters, deploy models, or serve inference.

## Architecture Design Review

Decision: implement the training factory as a Spring Boot bounded module named `training`, with REST APIs, JPA persistence, Flyway schema, and RBAC integrated into the existing core backend.

Rationale: the current platform keeps governed AI, dataset, knowledge, and decision workflows in the core backend. Training factory metadata must transact against approved AI-1 and AI-2 registries before queueing work, so a bounded module preserves consistency without introducing premature worker infrastructure.

Trade-offs: this keeps the first version operationally simple and testable. Actual distributed training workers, GPU cluster plugins, and external experiment systems can be added later behind the queue, artifact, checkpoint, and GPU abstractions.

## Implemented Components

- Training Job Manager: validates job requests, model family, method, dataset lineage, resource requirements, and queue state.
- Experiment Registry: stores experiment metadata for comparison and ownership.
- Training Scheduler: records run scheduling decisions without launching training.
- GPU Resource Manager: abstracts single GPU, multi-GPU, and future cluster capacity.
- Checkpoint Manager: validates checkpoint restore requests.
- Dataset Resolver: rejects unapproved AI-1 and AI-2 datasets.
- LoRA and QLoRA Manager Metadata: records adapter registry entries for planned adapter workflows.
- Hyperparameter Registry: captures immutable training configuration snapshots.
- Training Artifact Store: records artifact URI and checksum metadata.
- Training Queue, Logs, Metrics, Dashboard APIs: provides the operational backbone for later workers.
- Model Registry: records base model, parent model, adapter, GGUF, Ollama, vLLM, and license metadata.

## Review Boards

Architecture Review: Approved. The module preserves existing clean boundaries and does not redesign prior milestones.

ML Review: Approved. The implementation supports Qwen, Llama, Gemma, Mistral, DeepSeek, Phi, and future open-weight models as registry metadata only. No model training is executed.

Security Review: Approved. APIs are protected by `TRAINING_READ`, `TRAINING_ENGINEER`, `TRAINING_ADMIN`, `CHECKPOINT_RESTORE`, `MODEL_REGISTRY_READ`, and `MLOPS_ADMIN`; dataset use is blocked unless source registry status is approved.

Performance Review: Approved. Scheduling is metadata-only and queue-based, with indexes on job status, queue priority, checkpoints, metrics, and model registry fields.

MLOps Review: Approved. The schema captures experiments, hyperparameters, artifacts, checkpoints, metrics, logs, model registry, adapter registry, queue state, and GPU resources required before worker implementation.

Code Quality Review: Approved. The module follows existing controller, service, repository, Flyway, DTO, centralized exception, and H2-backed test conventions.

## Explicit Non-Goals

- No production Rural Intelligence Model training.
- No LoRA or QLoRA execution.
- No adapter merge.
- No model deployment.
- No inference serving.
- No external GPU orchestration dependency.
