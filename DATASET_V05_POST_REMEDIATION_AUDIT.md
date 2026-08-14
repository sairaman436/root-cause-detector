# Dataset v0.5 Post-Remediation Quality Audit

## Scope and decision boundary

This is a read-only audit of the 11 remediation candidates created after the v0.5 evidence-isolation changes. No candidate decision, historical v0.5 record, dataset, evaluation set, or review state was changed.

The audit separates structural eligibility from human-quality approval readiness, evidence isolation from evidence sufficiency, and corrected candidate versions from genuinely new scenarios.

## Executive result

- Candidates audited: **11**
- Candidate state: **11 PENDING_APPROVAL**
- Synthetic candidates: **0**
- Candidates with training_eligible=true: **0**
- Automated structural gate: **11 passed**
- Approval-ready as-is: **0**
- Workflow-ready for authenticated human review: **11**
- Recommended correction or re-evaluation: **11**
- Dataset v0.5 materialized: **No**

The remediation fixed the observed cross-scenario citation contamination in the persisted candidate records. It did not establish that the generated content is sufficiently specific, causally supported, or practically useful for approval.

## Domain by task matrix

This matrix covers the 11 new candidates only.

| Domain | Root cause | Recommendation | RAG | Total |
|---|---:|---:|---:|---:|
| Agriculture and food production | 1 | 0 | 0 | 1 |
| Education | 1 | 1 | 1 | 3 |
| Energy and electricity | 1 | 0 | 1 | 2 |
| Healthcare access | 1 | 0 | 0 | 1 |
| Housing and basic infrastructure | 1 | 1 | 0 | 2 |
| Livelihoods and markets | 1 | 0 | 0 | 1 |
| Water and sanitation | 1 | 0 | 0 | 1 |
| Climate and disaster resilience | 0 | 0 | 0 | 0 |
| **Total** | **7** | **2** | **2** | **11** |

Climate has no queued candidate. Recommendation and RAG coverage is absent in most domains. Three recommendation attempts remain blocked by VALIDATED_ROOT_CAUSE_REQUIRED and did not produce candidates.

## Candidate-by-candidate audit

All rows have a unique remediation evidence source ID. The cited source matched the scenario provenance source ID for all 11 candidates. These are audit findings, not persisted review decisions.

| Candidate and scenario | Task | Isolation and evidence | Grounding and quality finding | Disposition |
|---|---|---|---|---|
| 1a25d517-0038-45a6-bee3-535837d9db72, agri-pest-disease | Root cause | Direct source only; citation matched; no synthetic source | Crop disease or climate stress is not established by the supplied pest-damage observation. Field scouting and specimen evidence are absent. | Correction |
| 3962c95d-9037-4fdc-b470-4b837cecd340, education-dropout | RAG | Direct source only; citation matched | Answer restates that grounded guidance is needed but provides little evidence interpretation or useful bounded guidance. | Correction |
| 129c6c54-cfae-46b6-9b39-aa5e6d9e1154, education-student-transport | Recommendation | Direct source only; citation matched | Transport context is relevant, but attendance follow-up and household support are generic. Stored root cause is the problem statement, not an explicit validated causal result. | Correction plus validated-root-cause review |
| 5e77a843-2368-44ce-b5c9-dc487952ac5a, education-teacher-attendance | Root cause | Direct source only; citation matched | Teacher availability is plausible; the school-access-barrier secondary hypothesis is not supported by the supplied evidence. | Correction |
| f546da75-fd25-407d-b2f1-23f9345656ef, energy-grid-outages | RAG | Direct source only; prior solar source absent; citation matched | Answer is a generic reliability heading and does not explain outage evidence or distinguish observed records from gaps. | Correction |
| 00c36462-3ee2-4828-a14b-41b21b24796d, energy-transformer | Root cause | Direct source only; citation matched | Infrastructure reliability restates the observed interruption; resource constraint is unsupported. | Correction |
| 7bdaf319-c6eb-46ec-bb4c-edb6c4b14cef, health-staffing | Root cause | Direct source only; prior shared source absent; citation matched | Staffing coverage is observed, but workload and coverage mismatch is not evidenced. | Correction |
| 39bf5eb4-dc8f-4762-a1c7-3251d1ac9fc9, housing-market-shed | Recommendation | Direct source only; prior roof dependency absent; citation matched | Buyer-information and order-delivery interventions are not supported by the rain and usability evidence. Root cause is still a scenario statement. | Correction plus validated-root-cause review |
| 61c56f49-542f-4ae2-a908-255c5e60c1cd, housing-roof-leaks | Root cause | Direct source only; citation matched | Maintenance responsibility is a plausible investigation direction; inspection and material-gap claims are not established. | Correction |
| ed77421c-10ca-4711-b5ea-a3af6146d480, livelihood-seasonal-work | Root cause | Direct source only; prior shared source absent; citation matched | Seasonal work dependency is plausible; credit access constraint is unsupported. | Correction |
| 7efab948-96cb-4a8e-afe5-2ebf97d1dfd6, water-school-handwashing | Root cause | Direct source only; distinct replacement; citation matched | Replacement is distinct from latrine overflow, but school access and teacher availability are not relevant causal explanations for handwashing access. | Correction |

## Old to new remediation mapping

YES in the isolation column means the specific cross-scenario evidence defect was removed. NO in the quality column means the candidate still needs correction.

| Historical candidate | Original defect | Remediated candidate | Isolation fixed | Content fixed |
|---|---|---|---|---|
| 7802e304-843d-4ce0-b6ee-93518475fb2d | Pest root cause mixed generic hypotheses and shared evidence | 1a25d517-0038-45a6-bee3-535837d9db72 | YES | NO, climate-stress causality unproven |
| 4362d8d-ef25-4eb9-8901-deff8522c4d8 | Dropout RAG mixed education sources | 3962c95d-9037-4fdc-b470-4b837cecd340 | YES | NO, answer remains generic |
| 26134c5c-6057-4b59-9f24-750e1975b2c4 | Transport recommendation inherited teacher dependency | 129c6c54-cfae-46b6-9b39-aa5e6d9e1154 | YES | NO, weak root-cause linkage |
| eabc8597-ecb5-41a0-bf5f-ca870985c18e | Teacher root cause had broad hypotheses | 5e77a843-2368-44ce-b5c9-dc487952ac5a | YES | NO, school-access hypothesis unsupported |
| d3bdc0bf-fc98-4487-b1a0-4daf2feffca6 | Grid RAG mixed transformer, solar, and neighbor sources | f546da75-fd25-407d-b2f1-23f9345656ef | YES | NO, answer remains thin |
| 891afd0b-9f3a-4d9a-a142-b9565ca49753 | Transformer root cause was generic | 00c36462-3ee2-4828-a14b-41b21b24796d | YES | NO, resource constraint unsupported |
| 1f4df5ac-467a-4095-9f19-f106830c1d35 | Staffing root cause mixed generic and shared evidence | 7bdaf319-c6eb-46ec-bb4c-edb6c4b14cef | YES | NO, workload mismatch unsupported |
| 28f8ca98-4391-483b-a591-db13acf047fb | Market-shed recommendation inherited roof dependency | 39bf5eb4-dc8f-4762-a1c7-3251d1ac9fc9 | YES | NO, buyer and order options unsupported |
| 22f4c06b-62d6-4102-adba-d93974bd6adc | Roof-leak root cause used generic hypotheses | 61c56f49-542f-4ae2-a908-255c5e60c1cd | YES | NO, inspection/material hypotheses unverified |
| d3e58fe2-8da5-449b-9b13-5caecb8166b8 | Seasonal-work root cause mixed generic and shared evidence | ed77421c-10ca-4711-b5ea-a3af6146d480 | YES | NO, credit constraint unsupported |
| 4c823c36-4045-4081-b06b-20a6e1a4ee01 | Latrine overflow duplicated v0.4 sanitation | 7efab948-96cb-4a8e-afe5-2ebf97d1dfd6 replacement | YES | NO, causal output misaligned with handwashing |

The old waste-collection candidate has no queued remediation candidate: the greywater replacement completed structurally but scored 0.7832 and was blocked below the candidate threshold. The old seed-storage, health-appointment, and livelihood-supply-chain recommendations were blocked before persistence by VALIDATED_ROOT_CAUSE_REQUIRED.

## Evidence isolation and citation status

Fresh database invariants for the 11 queued candidates show:

- 11/11 citations exactly match the scenario provenance evidence source ID.
- 11/11 are classified PILOT_EVALUATION.
- 11/11 are non-synthetic and have source_type=EVALUATION_RESULT.
- 11/11 have training_eligible=false and approval_status=PENDING_APPROVAL.
- 0/11 candidate citations contain a development, synthetic, or fixture source.
- 0/11 persisted candidate citation records show a cross-scenario source.

These checks prove source identity and governance state; they do not prove that every cited excerpt supports every causal or intervention claim.

## Root-cause grounding status

| Result | Count |
|---|---:|
| Root-cause candidates audited | 7 |
| Direct source and source-ID match | 7 |
| Explicit uncertainty present | 7 |
| Candidate with at least one unsupported or weakly evidenced causal hypothesis | 7 |
| Approval-ready root-cause candidates | 0 |

The drought result correctly failed closed as unresolved evidence and did not create a candidate. For the 7 queued root-cause candidates, the direct evidence is too thin to support the additional hypotheses emitted by the normalized output.

## Recommendation grounding status

| Result | Count |
|---|---:|
| Recommendation candidates audited | 2 |
| Direct source and source-ID match | 2 |
| Multiple intervention options | 2 |
| Options needing domain-specific correction | 2 |
| Explicit validated-root-cause linkage sufficient for approval | 0 |

The VALIDATED_ROOT_CAUSE_REQUIRED gate remains active for blocked recommendations. The two queued records passed structural eligibility, but their stored root_cause values are scenario/problem statements and their options include generic interventions.

## RAG status

| Result | Count |
|---|---:|
| RAG candidates audited | 2 |
| Direct source and source-ID match | 2 |
| Cross-scenario source in persisted candidate output | 0 |
| Substantive grounded answer ready for approval | 0 |

Both RAG candidates are structurally valid and cited, but the answers are mostly generic statements about grounded guidance or evidence limitations.

## Raw model versus normalized candidate output

For all 11 queued records, normalized output is a JSON object, but the recorded model_response begins with Markdown fences or explanatory text rather than a strict JSON object. The candidate layer therefore has a canonical normalized output while the raw model contract remains non-strict for this batch. This is a production-quality risk and does not justify automatic approval.

## Diversity status

- Exact scenario-key duplicates within the 11 new candidates: 0.
- Reused direct evidence blocks across these candidates: 0 observed.
- Development-synthetic evidence reuse: 0 observed.
- water-school-handwashing is a distinct replacement for the old sanitation-overflow scenario.
- The other 10 queued rows are correction versions of historical v0.5 scenario families, not 10 new problem categories.
- No queued climate candidate exists.
- No queued recommendation exists for agriculture, healthcare, energy, livelihood, climate, or sanitation.

## Governance status

All 11 candidates remain unchanged and available for authenticated human review. No approval, correction, rejection, reviewer identity, or timestamp was written during this audit.

All 11 are suitable to display in Training Review because provenance, evidence, citations, PII status, and pending state are present. None is suitable for approval as-is. Reviewers should correct with preserved original content or reject with a reason.

## Remaining blocked records

Structural-gate failures with no candidate:

- agri-food-safety RAG: FAILED_STRUCTURAL_GATE.
- climate-cyclone-warning RAG: FAILED_STRUCTURAL_GATE.
- climate-drought-preparedness root cause: FAILED_STRUCTURAL_GATE; normalized result is unresolved evidence.

Below-threshold results with no candidate:

- health-facility-hours RAG: 0.7511.
- housing-community-facility RAG: 0.7479.
- livelihood-artisan-markets RAG: 0.7511.
- water-household-greywater recommendation: 0.7832.
- water-toilet-access RAG: 0.7510.

Recommendation dependency blockers:

- agri-seed-storage recommendation: VALIDATED_ROOT_CAUSE_REQUIRED.
- health-appointment-access recommendation: VALIDATED_ROOT_CAUSE_REQUIRED.
- livelihood-supply-chain recommendation: VALIDATED_ROOT_CAUSE_REQUIRED.

Historical v0.5 candidates remain unchanged and pending. Dataset v0.5 must not be materialized until human decisions exist and corrected results pass semantic, evidence, PII, provenance, citation, domain/task, and leakage gates.

## Audit conclusion

The remediation successfully fixed the concrete evidence-isolation defect for the 11 queued candidates: citations are scenario-specific, governed, non-synthetic, and pending human review. It did not resolve the second-order quality defect: normalized outputs still contain generic or unsupported causal hypotheses, generic recommendations, and shallow RAG answers. No candidate should be auto-approved or used to materialize dataset v0.5.
