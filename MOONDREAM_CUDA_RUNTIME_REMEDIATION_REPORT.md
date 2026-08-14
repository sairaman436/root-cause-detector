# Moondream CUDA Runtime Remediation Report

Date: 2026-08-14

## Result

**PASS**

The local GPU bridge was restored, direct Moondream inference succeeded on the real agriculture image, and the same image completed the authenticated application path:

`IMAGE → MOONDREAM → OBSERVATIONS → RAG → GOVERNED EVIDENCE → ROOT CAUSE → GROUNDED RECOMMENDATION`

No dataset, evaluation set, model weights, or training configuration was changed.

## Runtime Inventory

| Layer | Observed value |
|---|---|
| Host GPU | NVIDIA GeForce RTX 3050 6GB Laptop GPU |
| Host driver | 610.88 |
| Host CUDA UMD | 13.3 |
| GPU memory | 6144 MiB total |
| Docker Desktop | 29.6.2 |
| Docker Compose | 5.3.1 |
| Docker runtime | `runc` with NVIDIA device request/hook |
| WSL | 2.7.11.0 |
| WSL kernel | 6.18.33.2-microsoft-standard-WSL2 |
| Ollama | 0.5.7 |
| Vision model | `moondream:1.8b` |
| Model digest | `55fc3abd386771e5b5d1bbcc732f3c3f4df6e9f9f08f1131f9cc27ba2d1eec5b` |
| Model metadata | GGUF, phi2 + CLIP, 1B parameters, Q4_0, approximately 1.7 GB |

## Diagnosis

Before remediation:

- Host `nvidia-smi` detected the RTX 3050.
- Docker exposed `gpus: all`, but an independent GPU container failed with `nvidia-container-cli: initialization error: WSL environment detected but no adapters were found`.
- The Ollama container reported `CUDA driver version is insufficient for CUDA runtime version`, `ggml_backend_cuda_init: invalid device 0`, and model-runner startup failures.
- WSL exposed `/dev/dxg`, but the injected WSL NVIDIA libraries were version `610.57.01` while the active Windows kernel-mode driver was `610.88`.

This was a stale/uninitialized Windows WSL GPU bridge. The application, compose GPU declaration, and Moondream model selection were not the cause.

## Fix Applied

The smallest environment-only recovery was applied:

1. Restart Docker Desktop.
2. Shut down the WSL instance with `wsl --shutdown`.
3. Start the existing Docker Compose services again.
4. Recreate only the AI inference and web portal images after the contract/query fixes.

No CUDA package was installed or downgraded. No application fallback was introduced. The existing `gpus: all` configuration was retained.

After recovery, an independent GPU container started successfully and the Ollama container reported:

`NVIDIA GeForce RTX 3050 6GB Laptop GPU, 610.57.01 user-space utility, KMD 610.88, CUDA UMD 13.3, compute capability 8.6, 6.0 GiB total, 5.0 GiB available.`

Ollama logs reported CUDA inference compute using the GPU and no subsequent `invalid device 0` error.

## Small Application Compatibility Fixes

Direct Moondream returned valid natural-language visual observations, but one response exceeded the existing 500-character observation field. The AI service now splits complete model prose at sentence/word boundaries into bounded observation records without silently truncating content or inventing facts. A regression test covers this behavior.

The inspection UI also now builds the RAG query from the user question and high-signal terms actually present in the returned observations. This prevents the current deterministic governed index from being diluted by unrelated descriptive prose. The query remains auditable in the UI and contains no hidden model reasoning or fabricated terms.

## Direct Moondream Smoke Test

Image:

- File: `C:\Users\saira\AppData\Local\Temp\csp-agriculture.jpg`
- Type: JPEG
- Size: approximately 457 KB
- SHA-256: `0373B8AF5638EF20877BEA75ECDDAB2DB3C55D79946FD43E553D5EC4F4CE6BFE`

Request: image plus the existing observation prompt through Ollama `/api/chat`.

Observed response:

> The image shows a large field of green plants with yellow leaves scattered throughout it. Some plants have brown and yellow patches that may indicate stress or disease. The image contains no text or human-made objects.

Measured result:

- HTTP status: 200
- End-to-end direct Ollama latency: approximately 5,916 ms
- Ollama reported generation duration: approximately 5,773 ms
- Generated tokens: 117
- Model: `moondream:1.8b`

## AI Service Verification

The same image through `POST http://localhost:8101/v1/vision/analyze` returned HTTP 200 after the normalization fix.

- Request latency: approximately 1,443 ms
- Vision-reported latency: 1,192 ms
- Observations: 2 in the direct AI-service call
- Uncertainty: `The image does not establish a diagnosis or cause.`
- Model: `moondream:1.8b`
- GPU memory field: not reported by the AI service

## End-to-End Multimodal Run

The authenticated web portal AI Inspection Lab was used with the same image and question:

`What visible crop condition is present in the leaves?`

### Retrieval query

```text
User question: What visible crop condition is present in the leaves?
Observed terms: crop leaves plants yellow brown pests diseases damage
```

### Model observations

The vision model returned a real observation describing green plants with yellow and brown leaves, possible pest or disease-related damage, and holes in leaves. It explicitly stated that the image does not establish a diagnosis or cause.

### Retrieved governed evidence

The UI displayed five retrieved source records. The relevant crop-disease records included:

- `PILOT_V05_AGRI_PEST_DISEASE_ROOT_CAUSE_ANALYSIS`
- `PILOT_V05R_AGRI_PEST_DISEASE_ROOT_CAUSE_ANALYSIS`

The relevant excerpts state that crop pest damage was reported, while field scouting, specimen identification, and treatment records were not supplied. This supports investigation of pest/disease damage but does not establish a definitive diagnosis.

The grounding validator prevented unrelated retrieved records from becoming recommendation evidence. The final source trace for the generated options contained the crop-disease source IDs only.

### Root cause

The application produced an evidence-supported agriculture finding describing possible crop disease or climate stress contributing to the visible crop condition, with explicit uncertainty and approximately 60% displayed confidence. The output distinguished the model observation from the retrieved evidence and did not claim a confirmed diagnosis.

### Recommendations

Two grounded options were generated:

1. `Crop disease scouting and treatment verification`
2. `Soil and crop advisory support`

Both options targeted the validated crop-disease finding and required field verification records, monitoring capacity, and human approval. No generic market-access recommendation was emitted for this crop-disease evidence.

### Source trace

`Recommendation → validated root cause → crop-disease evidence IDs → governed source records`

The UI displayed the trace with the two crop-disease source IDs. Citation IDs were drawn from the actual RAG response; no citation was fabricated by the frontend.

### Measured latency

- Total UI pipeline latency: 6,907 ms
- Vision stage: 1,164 ms
- Direct RAG retrieval measurement for the same compact query: 6 ms retrieval, 1,487 ms grounded RAG inference
- Root-cause and recommendation timings were not exposed as separate response fields; they are included only in the measured total.

## Failure Test

Ollama was stopped temporarily and the same AI endpoint was called with the same real image.

- Result: HTTP 503
- Error code: `OLLAMA_CONNECTION_FAILED`
- Message: `Vision analysis unavailable.`
- No observation was returned.
- The frontend fail-closed path therefore produced no downstream evidence-backed recommendation.

Ollama was restarted afterward and GPU visibility was reverified.

## Health and GPU Verification After Recovery

Passed:

- Ollama model listing includes `moondream:1.8b` and `qwen2.5:0.5b`.
- Ollama container `nvidia-smi` sees the RTX 3050.
- GPU memory query: 6144 MiB total, 186 MiB observed after the smoke test.
- AI inference service readiness: ready, provider status ok, model available.
- RAG service readiness: ready, Qdrant reachable.
- Core backend health: UP.
- Web portal health: ok.

## Tests and Builds

- AI inference tests: 14 passed
- RAG tests: 8 passed
- Frontend tests: 2 passed
- Frontend typecheck: passed
- AI inference Docker build: passed
- Web portal Docker build: passed
- Targeted backend grounding tests previously validated: 14 passed

The full backend suite was not run for this milestone.

## Remaining Limitations

- WSL reports a user-space NVIDIA utility version of `610.57.01` while the host KMD is `610.88`; GPU inference works after the WSL refresh, but this version skew should be monitored after a host reboot or driver update.
- Per-request GPU utilization is not returned by the AI service; `nvidia-smi` confirms device visibility and aggregate memory, not exact per-request attribution.
- The current vision model reports visual observations, not diagnosis. Medical/agriculture decisions remain subject to governed evidence, uncertainty, and human review.
- Only the agriculture image was run live in this milestone. Other domains were not claimed as successful.

