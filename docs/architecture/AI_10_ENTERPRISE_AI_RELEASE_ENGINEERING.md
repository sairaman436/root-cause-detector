# AI-10 Enterprise AI Release Engineering

## Purpose

AI-10 prepares the first official Rural Intelligence Foundation Model v1.0 for enterprise production release. The release is treated as a governed enterprise AI product, not as a loose model artifact.

## Why It Exists

Enterprise AI releases require reproducible versioning, signed artifacts, checksums, SBOM references, model cards, release notes, certification evidence, compatibility validation, support lifecycle policy, rollback records, and board approvals. This module creates the release engineering control plane without training models, deploying models, or changing inference behavior.

## Architecture Fit

The module is implemented under `com.airural.platform.core.release` and follows the approved backend structure:

- REST adapter: `ReleaseController`
- Application service: `ReleaseEngineeringService`
- Domain persistence: release JPA entities
- Infrastructure adapters: Spring Data repositories
- Database schema: `model_release`
- Flyway migration: `V20__enterprise_ai_release_engineering.sql`
- RBAC authorities: `RELEASE_READ`, `RELEASE_REVIEW`, `RELEASE_PROMOTE`, `RELEASE_ROLLBACK`, `AI_AUDITOR`, `AI_ADMIN`

## Release Scope

The first official release is:

- Model name: Rural Intelligence Foundation Model
- Version: `v1.0.0`
- Channel: `LTS`
- Lifecycle status: `STABLE`
- Release type: enterprise production release

## Release Artifacts

AI-10 tracks metadata for:

- Production model
- Research model
- Development model
- Long-term support model
- GGUF package
- Safetensors package
- Ollama package
- vLLM package
- Docker/OCI image
- SBOM references
- Checksums
- Digital signatures
- Release notes
- Model card

The backend stores release metadata and integrity evidence. Binary model packaging remains owned by the model optimization and artifact storage systems.

## Release Pipeline

The release pipeline is:

1. Training
2. Evaluation
3. Optimization
4. Governance
5. Certification
6. Release candidate
7. Regression validation
8. Production release

Promotion requires all certification gates and board approvals to pass.

## Certification Gates

Certification records cover:

- Accuracy
- Reasoning
- Policy compliance
- Safety
- Performance
- Security
- Hallucination
- Citation accuracy
- Latency
- Memory
- Resource usage

## Compatibility Matrix

Compatibility records cover:

- Ollama
- vLLM
- llama.cpp
- Linux
- Windows
- macOS
- CPU
- NVIDIA GPU
- AMD GPU
- Cloud
- Air-gapped systems

## Model Card Contract

The generated model card includes:

- Purpose
- Capabilities
- Limitations
- Known risks
- Evaluation scores
- Training data summary
- Supported languages
- Hardware requirements
- Safety notes
- License

## Support Lifecycle

The support lifecycle records:

- Stable releases
- Hotfix releases
- Security patch policy
- LTS support window
- Retirement policy
- Upgrade path

For `v1.0.0`, the release is recorded as LTS with a three-year support window and a migration path through hotfix, minor, and major version upgrades.

## REST APIs

- `GET /api/v1/release/latest`
- `GET /api/v1/release/history`
- `GET /api/v1/release/artifacts`
- `GET /api/v1/release/model-card`
- `POST /api/v1/release/promote`
- `POST /api/v1/release/rollback`

The same endpoints are also exposed under `/release`.

## Security

Release engineering applies:

- Artifact signing metadata
- SHA-256 checksum validation metadata
- SBOM references
- Supply-chain integrity records
- Release audit history
- Least-privilege RBAC

## Final Certification Report

The release requires approval from:

- Enterprise Architecture Board
- AI Research Board
- Security Board
- Performance Board
- Governance Board
- External Audit
- Release Board

The implemented release service refuses promotion when any certification record is failed or any board approval is not approved.

## Out Of Scope

AI-10 does not train models, evaluate models, optimize artifacts, serve inference, or deploy infrastructure. It records and governs release engineering outputs from previous milestones.
