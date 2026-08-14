# Multimodal Evidence Coverage Audit

## Scope

This audit uses the live governed RAG corpus, the saved controlled rerun artifact, and read-only service/runtime checks. No evidence, dataset, evaluation set, review decision, or model was changed.

Sources are counted by unique `source`/source ID. A source is considered structurally usable when it is active, indexed with a searchable chunk, has provenance metadata, and is not marked development or synthetic. Structural usability does not mean that the source supports a particular image claim.

## Corpus Inventory

The live RAG service reports 64 indexed documents. The requested domains contain the following relevant source IDs:

### Agriculture

7 unique active governed source IDs were indexed:

- `CONTROLLED_PROJECT_PILOT`
- `PILOT_V05_AGRI_FOOD_SAFETY_RAG_GROUNDED_RESPONSES`
- `PILOT_V05_AGRI_PEST_DISEASE_ROOT_CAUSE_ANALYSIS`
- `PILOT_V05_AGRI_SEED_STORAGE_RECOMMENDATION_GENERATION`
- `PILOT_V05R_AGRI_FOOD_SAFETY_RAG_GROUNDED_RESPONSES`
- `PILOT_V05R_AGRI_PEST_DISEASE_ROOT_CAUSE_ANALYSIS`
- `PILOT_V05R_AGRI_SEED_STORAGE_RECOMMENDATION_GENERATION`

The pest/disease and controlled crop-stress records are the nearest matches to the agriculture image. They describe reported pilot conditions and evidence gaps, not verified diagnosis of the visible yellow/brown plants. All have active chunks and source metadata; none is sufficiently scenario-specific to establish the image's cause.

### Healthcare

7 unique active governed source IDs were indexed:

- `CONTROLLED_PROJECT_PILOT`
- `PILOT_V05_HEALTH_APPOINTMENT_ACCESS_RECOMMENDATION_GENERATION`
- `PILOT_V05_HEALTH_FACILITY_HOURS_RAG_GROUNDED_RESPONSES`
- `PILOT_V05_HEALTH_STAFFING_ROOT_CAUSE_ANALYSIS`
- `PILOT_V05R_HEALTH_APPOINTMENT_ACCESS_RECOMMENDATION_GENERATION`
- `PILOT_V05R_HEALTH_FACILITY_HOURS_RAG_GROUNDED_RESPONSES`
- `PILOT_V05R_HEALTH_STAFFING_ROOT_CAUSE_ANALYSIS`

These sources concern healthcare operations, facility hours, staffing, appointments, and referral access. The uploaded image was visually described as a building/schoolhouse scene; it did not establish that the building was a health facility. The corpus therefore has healthcare operational evidence, but not evidence that links this image to a healthcare condition. Medical inference is not justified.

### Livelihoods

8 unique active governed source IDs were indexed:

- `CONTROLLED_PROJECT_PILOT`
- `PILOT_V04_REPLACEMENT_LIVELIHOOD_STORAGE_RAG`
- `PILOT_V05_LIVELIHOOD_ARTISAN_MARKETS_RAG_GROUNDED_RESPONSES`
- `PILOT_V05_LIVELIHOOD_SEASONAL_WORK_ROOT_CAUSE_ANALYSIS`
- `PILOT_V05_LIVELIHOOD_SUPPLY_CHAIN_RECOMMENDATION_GENERATION`
- `PILOT_V05R_LIVELIHOOD_ARTISAN_MARKETS_RAG_GROUNDED_RESPONSES`
- `PILOT_V05R_LIVELIHOOD_SEASONAL_WORK_ROOT_CAUSE_ANALYSIS`
- `PILOT_V05R_LIVELIHOOD_SUPPLY_CHAIN_RECOMMENDATION_GENERATION`

The source corpus is not empty. The controlled rerun retrieved `PILOT_V05_LIVELIHOOD_SUPPLY_CHAIN_RECOMMENDATION_GENERATION` at score `0.1893`, but the root-cause service returned zero validated root causes. The source describes small-enterprise input delivery disruption, while the image only showed people, a building, a road, and cars. The conservative root-cause gate correctly rejected the unsupported link.

### Water/Sanitation

The sanitation domain contains 5 unique active pilot/approved source IDs. The water domain contains 5 unique indexed source IDs. Two are excluded from governed retrieval:

- `approved-synthetic-rural-policy`: synthetic policy source
- `development-evaluation-fixture`: development-only synthetic fixture

The remaining 8 source IDs are structurally usable, including sanitation pilot records, `PILOT_V05_WATER_WASTE_COLLECTION_RECOMMENDATION_GENERATION`, `approved-water-policy`, and `approved-rural-development-manual`. They cover toilet access, greywater, handwashing, latrine overflow, waste collection, bore-well maintenance, and water reliability. The live multimodal request did not reach retrieval because the uploaded file was not an image.

## Corpus Coverage Matrix

| Domain | Relevant governed sources | Structurally usable sources | Retrieved in controlled rerun | Passed RAG validation | Root cause supported | Recommendation possible |
| --- | ---: | ---: | ---: | ---: | --- | --- |
| Agriculture | 7 | 7 | 0 | 0 | No | No |
| Healthcare | 7 | 7 | 0 | 0 | No | No |
| Livelihoods | 8 | 8 | 1 | 1 citation; RCA rejected | No | No |
| Water/Sanitation | 8 usable; 10 indexed including 2 rejected | 8 | 0 | 0 | No | No |

The direct corpus inventory shows active chunks and valid source metadata. The validation counts above refer to the actual controlled multimodal rerun, not an unconstrained text-only search.

## Failed-Run Traces

### Agriculture

`IMAGE -> VISION -> RAG QUERY -> 0 QUALIFYING CITATIONS -> STOP`

- Vision: valid observations of yellow plants and brown leaves; uncertainty stated that the image did not establish a diagnosis or cause.
- Query included `agriculture`, `brown leaves`, `yellow plants`, the bounded observation summary, and uncertainty.
- Full governed query returned no citation passing the existing RAG evidence gate.
- A separate read-only short-query check found the pest/disease sources, proving the corpus is searchable. Those sources still do not establish a diagnosis for this image.
- Classification: **EVIDENCE COVERAGE GAP**, with retrieval sensitivity as a secondary issue. The missing item is image-specific, governed agricultural evidence, not generic agriculture text.

### Healthcare

`IMAGE -> VISION -> RAG QUERY -> 0 QUALIFYING CITATIONS -> STOP`

- Vision described a green-roofed building, road, people, and a possible schoolhouse; it did not confirm a clinic or health center.
- Query included canonical `healthcare`, the actual observations, and uncertainty.
- Full governed query returned no qualifying citation.
- A short-query check found facility-hours, staffing, appointment, and controlled-primary-care sources, but none links the photographed building to a healthcare problem.
- Classification: **EVIDENCE COVERAGE GAP** for the image scenario. The visual classification limitation is separate from healthcare corpus availability. No medical condition was inferred.

### Livelihoods

`IMAGE -> VISION -> RAG -> 1 CITATION -> ROOT CAUSE VALIDATION -> STOP`

- Vision described a group near a building and road, with bags and cars; market activity was not established.
- Query included canonical `livelihood`, the observed concepts, the bounded observation summary, and uncertainty.
- One livelihood source passed retrieval and citation validation at score `0.1893`.
- Root-cause generation produced candidates but zero validated root causes. The retrieved source concerned supply-chain disruption, which was not supported by the image observations or a second scenario-specific fact.
- Recommendation was not attempted because `VALIDATED_ROOT_CAUSE_REQUIRED` remained enforced.
- Classification: **ROOT-CAUSE GAP**. The failure is a correct conservative refusal, not a reason to lower the gate.

### Water/Sanitation

`IMAGE -> VISION -> STOP`

- The file `csp-water-sanitation.jpg` is not a JPEG image. Its first bytes are `<!DOCTYPE html>` and local image decoding fails with an image decode error.
- Direct Moondream returned `unable to make llava embedding from image` for this file because the bytes were invalid image data.
- The backend returned `VISION_UNAVAILABLE`; no retrieval query, evidence validation, root cause, or recommendation was produced.
- Ollama `/api/tags` was healthy and listed `moondream:1.8b`; Ollama reported the RTX 3050 CUDA device and loaded the model. Ollama version was `0.5.7-0-ga420a45-dirty`.
- A valid agriculture JPEG completed a direct Moondream smoke test in 772 ms and returned an observation, confirming the model/runtime was available.
- Classification: **VISION RUNTIME GAP**, with the concrete cause being invalid input-file content, not a CUDA or corpus failure. The original `403 ACCESS_DENIED` occurred under the lower-privilege field-surveyor account and is a separate authorization event.

## Validation and Provenance Findings

- Indexed documents have active status, one searchable chunk, source ID, publisher, document version, document type, checksum, and embedding metadata.
- Controlled-pilot records are provenance-bearing evaluation evidence and are not real village measurements. They remain usable only within their stated evaluation scope.
- Development and synthetic water sources are rejected by the governed-only filter and were not used in the controlled rerun.
- Direct short-query citations resolve to indexed chunks, so source-ID and structural citation resolution are working.
- A source ID alone does not prove scenario relevance. Agriculture, Healthcare, and Livelihood failures demonstrate why the semantic grounding gates correctly remain in place.

## Decision Summary

| Domain | Final classification | Reason |
| --- | --- | --- |
| Agriculture | **EVIDENCE COVERAGE GAP** | Broad agricultural evidence exists, but no governed source supports the specific visible condition or diagnosis. |
| Healthcare | **EVIDENCE COVERAGE GAP** | Healthcare operational evidence exists, but the image does not establish a healthcare facility or condition. |
| Livelihoods | **ROOT-CAUSE GAP** | Evidence was retrieved, but it did not support a validated cause for the visual scenario. |
| Water/Sanitation | **VISION RUNTIME GAP** | The submitted `.jpg` was HTML; Moondream and CUDA were available and valid-image inference worked. |

## Recommended Next Engineering Action

1. Add an input-integrity check that validates MIME, magic bytes, and decodability before calling Moondream; surface `INVALID_IMAGE_INPUT` instead of `VISION_UNAVAILABLE` for this case.
2. Treat Agriculture and Healthcare as corpus coverage work for image-grounded scenarios. Add only independently governed, scenario-relevant evidence through the existing evidence workflow; do not substitute generic web knowledge.
3. Keep the Livelihood root-cause gate unchanged. If the domain needs evaluation coverage, create scenario-specific evidence and observations through governed review rather than weakening validation.
4. Investigate retrieval scoring/query-length sensitivity separately, because short read-only domain queries find sources that the full multimodal query does not qualify. Do not change thresholds in this audit.

No fixes were applied. No datasets, evaluation sets, evidence records, or model artifacts were modified.
