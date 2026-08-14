# Backend Build Remediation Report

## Scope

This remediation addressed the reproducibility failure for the `core-backend`
Docker image. No application behavior, database migration, RAG rule, grounding
gate, dataset, evaluation set, or model configuration was changed.

## Build Environment

- Maven Wrapper: Apache Maven 3.9.11.
- Host Java used for independent resolution/tests: Java 24.0.1.
- Docker build runtime: Eclipse Temurin Java 21.0.11 on Alpine.
- Docker Engine: 29.6.2.
- BuildKit: 0.31.2.
- No Maven proxy environment variables or user Maven settings were present.
- HTTPS/TLS access to Maven Central succeeded from the host and from a Java 21
  Alpine container.

## Exact Failure

The dependency path is:

`spring-kafka:3.2.2` -> `kafka-clients:3.7.1` -> `com.github.luben:zstd-jni:1.5.6-3`

The first reproducible Docker build failure occurred before `zstd-jni` was
resolved. Maven failed while resolving the Spring Boot BOM:

`org.springframework.boot:spring-boot-dependencies:pom:3.3.2`

The exact Docker BuildKit error was:

`Unknown host repo.maven.apache.org: Try again`

Independent checks showed that Maven Central was available and the zstd artifact
was valid:

- Docker DNS resolved `repo.maven.apache.org` to Cloudflare.
- A Java 21 Alpine container downloaded the Spring Boot BOM successfully in five
  consecutive checks.
- `zstd-jni-1.5.6-3.jar` returned HTTP 200.
- Artifact size was `6,650,280` bytes.
- SHA-1 was `823b794106e4bcb80110f49408d1641231f25927`, matching Maven Central.
- Host Maven dependency resolution completed successfully.

The failure was therefore transient Docker BuildKit repository DNS/transfer
behavior on a fresh Maven cache, not an unavailable artifact, dependency
conflict, TLS failure, or bad dependency version.

## Fix

`services/core-backend/Dockerfile` now:

- uses a persistent BuildKit cache for `/root/.m2`;
- sets a bounded Maven Wagon read timeout of 300 seconds;
- retains verified Maven Central resolution;
- retries the complete Maven invocation at most three times for transient model
  or repository failures;
- makes no dependency, mirror, credential, or runtime changes.

## Build and Deployment Result

BuildKit produced the replacement image:

- Image: `ai-rural-root-cause-platform-core-backend:latest`
- Image digest: `sha256:2c856c13e9cea9c71c78711aaec173b8d196e73a1a65b4fc3e434d88d1978cb4`
- Container: `ai-rural-root-cause-platform-core-backend-1`
- Container image digest: matches the replacement image
- Deployment: recreated with `docker compose up -d --no-deps --force-recreate core-backend`
- Status: running and healthy

The Compose client timed out while waiting for the long BuildKit operation, but
the image was present in the local image store and was independently verified
by digest before deployment.

## Database and Service Verification

- PostgreSQL: accepting connections on port 5432.
- Flyway: `identity.flyway_schema_history` shows successful migrations through
  V33, including `human evaluation workflow`, `pilot evaluation energy domain`,
  and the prior governed pipeline migrations.
- Backend actuator health: HTTP 200, status `UP`.
- AI inference readiness: HTTP 200, model available.
- RAG readiness: HTTP 200, Qdrant connected.
- Qdrant readiness: HTTP 200.
- Ollama tags endpoint: HTTP 200; `moondream:1.8b` available.

## Regression Tests

Focused backend suite:

- 28 tests passed, 0 failures, 0 errors.
- Included image magic-byte/decoding validation, AI integration, local LLM,
  safety, root-cause grounding, and recommendation grounding tests.

Python AI/RAG suite:

- 22 tests passed, 0 failures.
- Included RAG pipeline and local LLM tests.

## Multimodal Smoke Test

Image: valid real water/sanitation JPEG at
`C:\Users\saira\AppData\Local\Temp\csp-water-sanitation-valid.jpg`.

### Vision

- Endpoint: `POST /api/v1/ai/vision/analyze`
- Model: `moondream:1.8b`
- Provider: Ollama
- Result: successful validated observation
- Latency: 15,550 ms
- Observation: the image showed a water pump in an open field with dry grass,
  on a dirt mound, with a blue cover over the spout; no people or other objects
  were visible.
- Uncertainty: the image did not establish a diagnosis or cause.

### RAG and Evidence Governance

The actual retrieval query included the user question, water/sanitation domain,
validated visual concepts, the model observation summary, and model uncertainty.

- RAG result: `INSUFFICIENT_EVIDENCE`
- Citation validation: `NOT_APPLICABLE`
- Retrieved governed citations: 0
- Retrieval latency: 2,681 ms
- Root cause: not generated
- Recommendation: not generated

This is the expected safe result for the available governed corpus. The rebuilt
backend did not fabricate evidence, citations, root causes, or recommendations.

## Final Status

**Backend image rebuilt and deployed successfully.** The backend, database,
AI, RAG, Qdrant, and Ollama health checks pass. The valid-image multimodal
smoke test reaches real Moondream and stops safely at insufficient governed
evidence.

Remaining operational limitation: the local Compose build can still be slow on
a cold Maven cache, but transient repository failures are bounded and the Maven
cache now persists across rebuilds.
