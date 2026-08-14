# Live Multimodal RAG Smoke Test Report

Date: 2026-08-14
Environment: Local Docker Compose
Final run: Real image -> moondream -> observations -> governed RAG -> root cause -> recommendations

## Environment Recovery

Docker Desktop was stopped and its Docker API pipe was unavailable. The existing Docker Desktop installation was started; no database reset or data deletion was performed.

Services verified healthy:

- Ollama: healthy; `moondream:1.8b` available
- AI inference service: `200 /health/ready`
- Core backend: healthy
- RAG service: `200 /health`
- Qdrant: `200 /readyz`
- Web portal: healthy; `200 /api/health`
- GPU: RTX 3050 6GB, 5795 MiB / 6144 MiB observed after the run

## Image Used

- Local file: `C:\Users\saira\AppData\Local\Temp\csp-agriculture.jpg`
- Type: JPEG
- Size: 468,251 bytes
- SHA-256: `0373B8AF5638EF20877BEA75ECDDAB2DB3C55D79946FD43E553D5EC4F4CE6BFE`
- Public source: South Dakota State University Extension, soybean frogeye leaf spot image
- Source URL: https://extension.sdstate.edu/sites/default/files/2020-07/W-00719-01-Soybean-fungal-diseases-frogeye-leaf-spot.jpg
- User question: `What visible crop condition is present in the leaves?`

The image was uploaded to the local authenticated portal only. It was not added to a dataset or evaluation set.

## Vision

- Endpoint: `POST /api/v1/ai/vision/analyze`
- Model: `moondream:1.8b`
- Provider: Ollama
- Observations returned: 1
- Vision latency shown in the portal: 962 ms
- GPU memory in the AI-service response: not reported because `nvidia-smi` is not installed inside that container

Actual model observation:

> The image shows a close-up view of a field filled with green plants that appear to be in poor condition. The plants are covered in brown spots, which could indicate disease or pests affecting them. Some of the leaves have been partially eaten by insects, further contributing to their unhealthy appearance. Despite these issues, there is still some greenery visible within the field, suggesting that it may not be entirely barren or devoid of life.

Actual uncertainty returned:

> The image does not establish a diagnosis or cause.

One earlier live attempt returned `VISION_INVALID_OUTPUT`; the fail-closed path stopped it. The final portal run returned a validated observation.

## RAG

Actual retrieval query shown in the portal:

```text
User question: What visible crop condition is present in the leaves?
Image observations:
- visual_observation: The image shows a close-up view of a field filled with green plants that appear to be in poor condition. The plants are covered in brown spots, which could indicate disease or pests affecting them. Some of the leaves have been partially eaten by insects, further contributing to their unhealthy appearance. Despite these issues, there is still some greenery visible within the field, suggesting that it may not be entirely barren or devoid of life.
Vision uncertainty: The image does not establish a diagnosis or cause.
```

RAG returned five governed citations and marked the response `SUPPORTED` / `VALIDATED`:

| Source ID | Title | Score | Citation ID | Evidence excerpt |
|---|---|---:|---|---|
| `PILOT_V05_AGRI_FOOD_SAFETY_RAG_GROUNDED_RESPONSES` | Controlled pilot evidence: pilot-v05-agri-food-safety-rag-grounded-responses-001 | 0.2174 | `7eac8ec8-2fe7-4971-8e64-2a5f123f0755` | Post-harvest food-safety controls require verification; inspection findings, pathogen tests, and cold-chain records are not supplied. |
| `PILOT_V05R_AGRI_PEST_DISEASE_ROOT_CAUSE_ANALYSIS` | Approved root-cause evidence: pilot-v05r-agri-pest-disease-root-cause-analysis-001 | 0.2062 | `299fe341-bc84-4a99-999d-e963292228ff` | Crop disease or climate stress may contribute; pest damage is reported; field scouting and specimen identification are not supplied. |
| `PILOT_V05R_AGRI_FOOD_SAFETY_RAG_GROUNDED_RESPONSES` | Controlled pilot evidence: pilot-v05r-agri-food-safety-rag-grounded-responses-001 | 0.1972 | `3d3af2b0-7ee5-4a23-8a85-e36b3f7e50b7` | Post-harvest food-safety controls require verification; inspection findings, pathogen tests, and cold-chain records are not supplied. |
| `CONTROLLED_PROJECT_PILOT` | PILOT_EVALUATION Controlled Irrigation Evidence | 0.1699 | `ea0bb9ad-f831-4d62-b958-616e394cae55` | Constructed pilot evidence reports irrigation interruptions, delayed pump repairs, maintenance uncertainty, and crop stress; it is not real village data. |
| `PILOT_V05_AGRI_PEST_DISEASE_ROOT_CAUSE_ANALYSIS` | Controlled pilot evidence: pilot-v05-agri-pest-disease-root-cause-analysis-001 | 0.1598 | `2324a7d1-adca-44d1-8f54-2f6f5b9a122a` | Pest damage is reported in a controlled pilot; field scouting, specimen identification, and treatment records are not supplied. |

The frontend passed only citations meeting its existing score threshold of `0.18` into root-cause and recommendation generation. Therefore the recommendation trace used the first three source IDs above. No development-synthetic evidence was used.

## Root Cause

- Persisted analysis ID: `09b12c5c-5dc0-4196-99f0-d4cccc1e31cd`
- Model version: `qwen2.5:0.5b`
- Validated finding shown in the portal: `crop disease or climate stress may contribute to What visible crop condition is present in the leaves?`
- Displayed confidence: 88%
- Supporting source IDs: `PILOT_V05_AGRI_FOOD_SAFETY_RAG_GROUNDED_RESPONSES`, `PILOT_V05R_AGRI_PEST_DISEASE_ROOT_CAUSE_ANALYSIS`, `PILOT_V05R_AGRI_FOOD_SAFETY_RAG_GROUNDED_RESPONSES`
- Uncertainty: causality is not established; field scouting, specimen identification, treatment records, and other verification are missing.

The root-cause result distinguishes the image observation from a causal conclusion and retains uncertainty. It is decision-support inference, not a diagnosis.

## Recommendation

- Persisted recommendation set ID: `9fa75e57-b640-49e8-9591-839ea3ccbcac`
- Status: `AI_GENERATED`; human approval remains required
- Model/prompt: `qwen2.5-local` / `RECOMMENDATION_GENERATION@1.0.0`
- Options shown in the portal: 3

| Option | Feasibility | Evidence strength | Confidence | Evidence source IDs |
|---|---|---:|---:|---|
| Irrigation access improvement (agriculture) | Requires resource verification | 62% | 74% | The three selected source IDs |
| Soil and crop advisory support (agriculture) | Requires resource verification | 62% | 74% | The three selected source IDs |
| Market-access support (agriculture) | Requires resource verification | 62% | 74% | The three selected source IDs |

All options include human approval, field verification, resource verification, risks, dependencies, and no automatic execution. The displayed source trace is:

```text
Recommendation
  -> Root cause 09b12c5c-5dc0-4196-99f0-d4cccc1e31cd
  -> Retrieved governed evidence
  -> Source IDs listed above
```

## Provenance Verification

- Every source ID attached to the recommendation was present in the retrieved citation list: PASS.
- Every attached source ID had a non-empty retrieved excerpt: PASS.
- The source excerpts are actual persisted RAG records: PASS.
- Source content semantically supports every generated option: PARTIAL.

The pest-damage source supports an uncertain crop-disease/pest hypothesis. The food-safety sources are agricultural governed records but are not directly specific to leaf disease, and the market-access option is generic relative to the image evidence. The system exposed these options rather than blocking them on semantic relevance. This is a quality limitation and is not being represented as a clean production-grounding pass.

## Safety Checks

- Unauthenticated vision request: HTTP 401, PASS.
- Invalid/unsupported image path: existing targeted backend check returned HTTP 415, PASS.
- Vision invalid output: HTTP 502 and no downstream recommendation, PASS.
- Vision provider unavailable: covered by AI tests; no fabricated observation, PASS.
- RAG insufficient evidence: observed during a bounded diagnostic request as `INSUFFICIENT_EVIDENCE`; downstream generation was stopped, PASS.
- Invalid citation/source ID: covered by existing RAG and backend tests; no fabricated citation, PASS.
- Human approval: recommendation remained `AI_GENERATED`; no intervention was executed, PASS.

## Frontend Verification

The authenticated web portal at `http://localhost:3000/` displayed the same live result:

1. Uploaded image preview
2. Model Observations
3. Actual retrieval query
4. Five retrieved evidence items with source IDs, excerpts, scores, and citation IDs
5. Evidence validation
6. Root cause and uncertainty
7. Three recommendations with feasibility and risk sections
8. Recommendation -> root cause -> evidence -> source trace

Portal result: `Analysis available`, all eight trace stages marked `OK`.

## Latency

| Stage | Result |
|---|---:|
| Vision | 962 ms shown by portal |
| Retrieval | Not exposed as a per-stage value by the current portal response |
| Root cause | Not exposed as a per-stage value by the current portal response |
| Recommendation | Not exposed as a per-stage value by the current portal response |
| Total browser workflow | 7,721 ms shown by portal |

The AI service reports `gpu_memory: null`; the Ollama container confirmed GPU access separately. The post-run GPU sample was 5795 / 6144 MiB with 5% utilization.

## Tests

- AI inference tests: 13 passed
- RAG tests: 8 passed
- Frontend tests: 3 passed across admin and web workspaces
- Frontend typecheck: passed
- Web portal Docker build: passed
- AI inference Docker build and health: passed
- Backend/RAG/Qdrant/Ollama/web health checks: passed
- Full backend suite: intentionally not run for this milestone

## Final Result

`PIPELINE_COMPLETED_WITH_GROUNDING_LIMITATION`

The real image successfully traversed the live vision -> RAG -> governed evidence -> root cause -> recommendation path, and the same result was visible in the portal with structurally valid source traces. A strict production-quality pass is blocked by semantic evidence relevance: some retrieved pilot records and generated options are generic or only indirectly related to the visible leaf condition. No datasets, evaluation sets, model weights, or human review decisions were modified.

Remaining blocker: add/enable semantic evidence-to-claim validation that fails closed when a retrieved source does not directly support the root-cause or recommendation claim. This report does not weaken that requirement or claim production readiness.
