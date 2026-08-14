# Production Reliability Report

## Scope

- Validation date: 2026-08-11
- Runtime: local Docker Compose stack with Qwen `qwen2.5:0.5b` through the constrained v0.3 inference path.
- No training, fine-tuning, model changes, dataset changes, or database business-data writes were performed.
- Dataset v0.3 digest before and after validation: `5a046ef1f2a76518a790a8fac4245ea2803094a6a1d8f0859a3776d7f6f3586b`.

## Tests Performed

| Test | Result |
|---|---:|
| Root-cause repeated sequential calls | 5/5 passed |
| Root-cause concurrent calls | 3/3 passed |
| Clean-restart cold/warm cycle | 3/3 passed |
| Explicit evidence-grounding repeat | 3/3 passed |
| RAG-grounded query | 1/1 passed |
| AI/RAG/v0.3 Python contract tests | 27 passed, 0 failed |
| Recommendation unit tests | 7 passed, 0 failed |
| Provider-unavailable fault test | 1/1 explicit failure, no fabricated output |
| Existing unavailable-model/unavailable-provider tests | Passed |

## Reliability Results

### Qwen Root-Cause Inference

- 14/14 validation calls returned HTTP 200 and canonical `dataset-v0.3` output.
- Structured output: 14/14.
- Citation/source-ID validity: 14/14; every checked citation referenced `pilot.water.circular.001`, which was present in the request.
- Explicit evidence grounding: 3/3; each root cause contained the permitted evidence source ID.
- Repair attempts: 0 on successful calls.
- Clean-restart cold latency: 8,864.41 ms.
- Clean-restart warm latencies: 3,831.55 ms and 4,275.90 ms; average 4,053.73 ms.
- Five-call sequential run: 5,391.39 ms average, 4,088.83–9,007.43 ms range.

### RAG-Grounded Generation

- HTTP result: 200.
- Support status: `SUPPORTED`.
- Citation validation: `VALIDATED`.
- Retrieved citations: 3/3 with source IDs returned by the retrieval index.
- Answer present: yes.
- Qwen inference latency within RAG request: 6,365 ms; total request latency: 6,475.65 ms.

### Recommendation Generation

- `RecommendationEngineTests`: 1/1 passed.
- `RecommendationIntelligenceServiceTests`: 6/6 passed, including root-cause linkage, RAG evidence linkage, multiple options, persistence, and reviewability.
- Full Spring integration execution remains blocked before recommendation execution by the existing H2 incompatibility documented below.

### Concurrency

- 3/3 concurrent root-cause requests succeeded with valid structured output and citations.
- Per-request latency: 4,628.69 ms, 8,939.16 ms, and 13,379.31 ms.
- Wall-clock batch latency: 13,380.86 ms.
- The existing constrained-generation lock serializes model calls. This preserves correctness but creates queueing latency under concurrency.

## Failure and Timeout Behavior

- An isolated unreachable-Ollama test returned `LLM_CONSTRAINED_GENERATION_FAILED` with HTTP-equivalent status 502 after 3,917.93 ms. No output or citation was fabricated.
- Existing service tests preserve explicit `OLLAMA_MODEL_UNAVAILABLE` and `OLLAMA_UNAVAILABLE` errors rather than reporting success.
- RAG provider failure uses the existing deterministic evidence-summary fallback. A fault-injected RAG call confirmed the fallback retained the retrieved excerpt and citation marker `[1]`.
- No silent unconstrained fallback was observed.

## GPU Telemetry

- Host sample: NVIDIA RTX 3050 6GB, 7 MiB used of 6,144 MiB, 0% utilization at the sample point.
- AI responses reported `gpu_memory: null`.
- Ollama has no container GPU device request, and its logs report `no compatible GPUs were discovered`, `library=cpu`, and `cpu_avx2` execution.
- Container-level GPU memory/utilization is therefore unavailable for this deployment. The measured inference path is CPU-backed; the host `nvidia-smi` sample must not be interpreted as model GPU usage.

## Test-Environment Failure

- The selected backend Spring integration tests did not reach recommendation execution because Flyway migration `V25__enterprise_rag_knowledge_retrieval.sql` uses PostgreSQL `gen_random_uuid()` and the H2 test profile cannot parse it.
- Affected tests: `DecisionPlatformIntegrationTests` and `CoreWorkflowRecoveryIntegrationTests`.
- This is an existing test-profile/database-compatibility blocker, not a failure observed in the healthy running Docker backend.

## Remaining Blockers

1. Resolve the H2/PostgreSQL migration compatibility for the backend integration-test profile, then rerun the secured recommendation end-to-end tests.
2. Provide Docker GPU device/runtime configuration if container-level GPU telemetry and GPU-backed Ollama inference are required.
3. Address constrained-generation serialization or add capacity if the platform must meet higher concurrent-request latency targets.
