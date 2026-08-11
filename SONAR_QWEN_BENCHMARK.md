# SONAR / Qwen Benchmark

## 1. Environment

This is an experimental benchmark record. It does not promote or replace the production model.

| Item | Observed value |
| --- | --- |
| Host | Windows 11, x86_64 |
| Python | 3.14.3 |
| PyTorch | 2.11.0 |
| Transformers | 5.1.0 |
| Hugging Face Hub | 1.14.0 |
| SONAR runtime install | Blocked: no compatible `fairseq2n` distribution for this interpreter |
| Scenario count | 1 |
| Runs per model | 1 |
| Scenario | `benchmark-water-reliability-001` |
| Prompt and context | Identical `ROOT_CAUSE_ANALYSIS@1.0.0`, survey context, evidence, and citation context |

The scenario is the existing local Qwen water-reliability workflow: unreliable village water supply, a well source, delayed bore-well repairs, one field-note reference, and one approved-policy citation.

## 2. Model Configuration

### Qwen

- Provider: existing `OllamaProvider`.
- Model: `qwen2.5:0.5b`.
- Local Ollama metadata: GGUF, Q4_K_M, 494.03M parameters, 397,821,319-byte artifact.
- Default behavior: unchanged and remains the platform default.

### SONAR

- Provider: new opt-in `SONARProvider`.
- Model: [`raxtemur/sonar-llm-100m`](https://huggingface.co/raxtemur/sonar-llm-100m).
- Loader: the checkpoint's `sonarllm_model` loader through `snapshot_download`.
- Runtime: `sonar-space` plus a compatible Fairseq2/PyTorch installation.
- Activation: `LLM_PROVIDER=sonar`, `LLM_MODEL=raxtemur/sonar-llm-100m`.
- Production default: unchanged; SONAR is not automatically selected.

The SONAR model card documents a custom generator and the official SONAR project documents the `sonar-space` and Fairseq2 dependency relationship. See the [SONAR installation guidance](https://github.com/facebookresearch/SONAR#installing).

## 3. Qwen Results

| Metric | Current run |
| --- | --- |
| Successful generation rate | 0/1, 0.0% |
| Structured-output success | 0/1, 0.0% |
| Inference latency | 3,412.58 ms to observed HTTP 500 failure; not a successful inference latency |
| Root-cause quality | Not scored |
| Evidence grounding | Not scored |
| Citation correctness | Not scored |
| Hallucination / unsupported-claim rate | Not scored |
| Recommendation quality | Not scored |

The installed model was present in the local Ollama model list, but the identical structured request returned `OLLAMA_SERVER_ERROR` with HTTP 500. Existing Qwen integration tests remain green, and the prior local integration report records the validated Qwen path, but this run does not provide a successful runtime benchmark sample.

## 4. SONAR Results

| Metric | Current run |
| --- | --- |
| Successful generation rate | 0/1, 0.0% |
| Structured-output success | 0/1, 0.0% |
| Inference latency | 1,457.37 ms to observed checkpoint-unavailable failure; not a successful inference latency |
| Root-cause quality | Not scored |
| Evidence grounding | Not scored |
| Citation correctness | Not scored |
| Hallucination / unsupported-claim rate | Not scored |
| Recommendation quality | Not scored |

SONAR did not generate output. The checkpoint was not cached and the attempted optional dependency installation failed because the current Python 3.14/Windows environment has no compatible `fairseq2n` distribution. No SONAR quality or resource score is inferred from this failure.

## 5. Side-by-Side Comparison

| Metric | Qwen | SONAR | Interpretation |
| --- | --- | --- | --- |
| Generation success | 0/1 | 0/1 | No valid quality comparison is possible |
| Structured output | 0/1 | 0/1 | No model output was available |
| Successful latency | Not available | Not available | Failure-path timings are excluded |
| Memory usage | Not available | Not available | Failure-path RSS is not model memory |
| Root-cause quality | Not measured | Not measured | Requires successful outputs and a human/reference rubric |
| Evidence grounding | Not measured | Not measured | Requires citation-bearing outputs and source verification |
| Citation correctness | Not measured | Not measured | Requires reference-level citation checks |
| Unsupported-claim rate | Not measured | Not measured | No outputs to inspect |
| Recommendation quality | Not measured | Not measured | Requires successful root-cause-to-recommendation evaluation |

The current run is an environment and integration validation, not a passing model benchmark. Equal zero-success rates must not be interpreted as equal model quality.

## 6. Failure Cases

### Qwen

- Ollama reported the model as installed.
- The structured generation request returned HTTP 500.
- The existing Qwen adapter was not modified.
- Root-cause, citation, hallucination, and recommendation scores were not produced.

### SONAR

- The checkpoint was not present in the local Hugging Face cache.
- Installing `.[sonar,benchmark]` failed during Fairseq2 dependency resolution because no compatible `fairseq2n` distribution was available for Python 3.14/Windows.
- SONAR therefore stopped before model loading and generation.

## 7. Resource Usage

No valid model-memory or throughput measurements were captured.

- Qwen artifact size observed: 397,821,319 bytes. This is an Ollama artifact size, not resident memory.
- SONAR model card lists a 401 MB `pytorch_model.bin`; the checkpoint was not downloaded locally.
- The runner captured process RSS around failure paths, but those values are intentionally excluded because they do not represent loaded-model memory.
- GPU, CPU utilization, peak memory, token throughput, and warm/cold successful latency remain unmeasured.

## 8. Recommendation

**Keep Both.**

- Keep Qwen as the current production/default provider because its existing integration remains unchanged and has prior successful local acceptance evidence.
- Keep SONAR as an experimental provider behind `LLM_PROVIDER=sonar`.
- Do not promote SONAR, change production configuration, or claim model-quality superiority until the benchmark is rerun in a supported Python/Fairseq2 environment with both providers producing valid structured outputs.

### Required rerun gate

1. Use a supported Python environment for `sonar-space` and a Fairseq2/PyTorch build compatible with that environment.
2. Install the optional SONAR dependencies and download the checkpoint.
3. Resolve the current Ollama HTTP 500 for the identical Qwen request.
4. Run at least three cold/warm iterations per provider with the same scenario set.
5. Apply the existing evaluation criteria and human/reference labels for root-cause quality, grounding, citations, unsupported claims, and recommendations.
6. Record only successful, reproducible measurements before any model-selection decision.

## Validation Evidence

- `python -m pytest tests`: 9 passed.
- `python -m py_compile src/ai_inference_service/main.py scripts/benchmark_models.py`: passed.
- `git diff --check`: passed.
- Production Qwen configuration was not changed.
