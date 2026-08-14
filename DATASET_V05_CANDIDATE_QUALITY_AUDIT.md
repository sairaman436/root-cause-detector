# Dataset v0.5 Candidate Quality Audit

## Scope and Decision Boundary

This audit covers the 22 completed `PILOT_EVALUATION` candidates currently
pending authenticated human review. It is read-only. No candidate decision,
review status, dataset, v0.4 record, or evaluation set was changed.

The audit evaluates semantic meaning, not identifiers. It inspects the
persisted scenario, pipeline output, retrieved evidence, source IDs,
uncertainty, provenance, and candidate state.

Current state:

- Completed candidates: `22`
- Candidate status: `22 PENDING_APPROVAL`
- Training readiness: `22 PENDING_DATASET_APPROVAL`
- Approved/corrected/rejected: `0 / 0 / 0`
- Dataset v0.5: not materialized
- Blocked, unpersisted recommendations: `2`

## Domain and Task Matrix

The database stores Water and sanitation across `WATER` and `SANITATION`,
and Climate/disaster resilience as `MULTI_DOMAIN`. The matrix below maps
those storage values to the user-facing domains.

| Domain | Root cause | Recommendation | RAG | Total |
|---|---:|---:|---:|---:|
| Water and sanitation | 1 | 1 | 1 | 3 |
| Agriculture and food production | 1 | 1 | 1 | 3 |
| Healthcare access | 1 | 1 | 1 | 3 |
| Energy and electricity | 1 | 0 | 1 | 2 |
| Education | 1 | 1 | 1 | 3 |
| Livelihoods and markets | 1 | 1 | 1 | 3 |
| Climate and disaster resilience | 1 | 0 | 1 | 2 |
| Housing and basic infrastructure | 1 | 1 | 1 | 3 |
| **Total** | **8** | **6** | **8** | **22** |

The completed set is structurally balanced by domain except for the two
recommendation scenarios blocked by the validated-root-cause dependency.
Recommendation coverage is therefore the only material task imbalance.

## Problem-Category Coverage

The 22 completed records declare 22 problem categories:

- Water and sanitation: latrine overflow/containment, waste collection and
  drain blockage, household toilet access and safe disposal.
- Agriculture: crop pest/disease, seed storage, post-harvest food safety.
- Healthcare: staffing coverage, appointment access, facility hours.
- Energy: transformer reliability, grid-outage interpretation.
- Education: teacher attendance, student transport, dropout investigation.
- Livelihoods: seasonal employment, input supply chain, artisan market access.
- Climate: drought preparedness, cyclone-warning communication.
- Housing: roof leakage, market-shed usability, community-facility condition.

Within the completed set there are no exact problem-text duplicates. Against
v0.4, 21 categories are not clear duplicates, one sanitation-overflow category
is a material duplicate, and two categories are adjacent enough to require
human distinction review: household toilet access and teacher attendance.
The two blocked recommendation categories, solar maintenance and flood
resilience, are not included in the completed count.

## Candidate-by-Candidate Audit

All rows below have `PILOT_EVALUATION` provenance, `constructed=true`,
`real_world_data=false`, `training_data=false`, an explicit uncertainty
statement, and pending human review. The provenance is appropriate for a
controlled pilot, but it does not make the records real-world evidence.

| Candidate | Domain / task | Problem and category | Root cause or intervention output | Evidence, citations, provenance | Audit finding and recommendation |
|---|---|---|---|---|---|
| `7802e304-843d-4ce0-b6ee-93518475fb2d` `agri-pest-disease` | Agriculture / root cause | Crop pest outbreak in one production cycle; pest/disease diagnosis and contributing condition are uncertain. | Four generic hypotheses: crop disease or climate stress, poor irrigation, soil health, and market access. | Direct source `PILOT_V05_AGRI_PEST_DISEASE_ROOT_CAUSE_ANALYSIS`; also retrieved `CONTROLLED_PROJECT_PILOT`. | The first hypothesis is relevant, but irrigation, soil, and market claims are not supported by the supplied pest evidence. Requires correction to use domain-specific hypotheses and evidence. |
| `5218b622-7225-4d94-be8e-1f750a26118e` `agri-seed-storage` | Agriculture / recommendation | Seed deterioration between seasons; storage condition and handling. | Outputs irrigation access, soil/crop advisory, and market-access interventions. | Direct seed-storage source plus pest-disease source and `CONTROLLED_PROJECT_PILOT`. | Recommendation target is materially mismatched to seed storage and inherits another scenario's root cause. Requires correction; do not approve as-is. |
| `6f0ec903-a273-422a-bc24-3810d1fd1773` `agri-food-safety` | Agriculture / RAG | Post-harvest food-safety controls and missing inspection evidence. | Generic grounded answer about the scenario and evidence gaps. | Direct food-safety source plus seed-storage, pest-disease, and `CONTROLLED_PROJECT_PILOT` sources. | Evidence is relevant at the direct source, but retrieval is contaminated by neighboring agriculture scenarios. Requires isolated retrieval and citation correction. |
| `e3671940-d0e6-4416-8442-d63d07ab7ede` `climate-drought-preparedness` | Climate / root cause | Drought-preparedness gap; limiting factor is uncertain. | Generic infrastructure, access, resource, and service-availability hypotheses. | Direct drought source plus `CONTROLLED_PROJECT_PILOT`. | Hypotheses are broad and not sufficiently drought-specific; shared source is unrelated. Requires correction. |
| `d3764a9d-c4ee-445b-af94-c3ac86e45bfa` `climate-cyclone-warning` | Climate / RAG | Cyclone-warning communication and missing alert records. | Generic answer about trusted evidence and limitations. | Direct cyclone source plus drought source, blocked flood-recommendation source, and `CONTROLLED_PROJECT_PILOT`. | Cross-scenario and blocked-scenario retrieval contamination. Requires correction and source isolation. |
| `eabc8597-ecb5-41a0-bf5f-ca870985c18e` `education-teacher-attendance` | Education / root cause | Teacher attendance gaps and missed instructional time. | School access, teacher availability, economic pressure, and health/nutrition hypotheses. | Direct teacher-attendance source only. | No cross-source contamination was observed, but the hypotheses are broad and several are unsupported by the supplied attendance evidence. Requires correction or explicit human substantiation. |
| `26134c5c-6057-4b59-9f24-750e1975b2c4` `education-student-transport` | Education / recommendation | Students have difficulty reaching school from remote settlements. | Attendance follow-up, school-access reduction, and household support referral. | Direct transport source plus teacher-attendance source. | Some interventions are relevant, but the teacher-attendance dependency is not a validated causal basis for transport access. Requires correction. |
| `4362d8d-ef25-4eb9-8901-deff8522c4d8` `education-dropout` | Education / RAG | Student dropout risk and missing enrollment/attendance records. | Generic grounded answer. | Direct dropout source plus transport and teacher-attendance sources. | Related education evidence is mixed into the response without a clear source-specific rationale. Requires correction and citation narrowing. |
| `891afd0b-9f3a-4d9a-a142-b9565ca49753` `energy-transformer` | Energy / root cause | Repeated transformer interruptions; dominant reliability constraint is uncertain. | Infrastructure reliability, resource, service-availability, and governance hypotheses. | Direct transformer source only. | Problem is domain-specific, but output is a reusable generic root-cause template rather than transformer-specific analysis. Requires correction. |
| `d3bdc0bf-fc98-4487-b1a0-4daf2feffca6` `energy-grid-outages` | Energy / RAG | Grid-outage records and evidence gaps. | Generic grounded answer. | Direct grid-outage source plus transformer source, blocked solar-maintenance source, and another energy scenario. | Retrieval includes a blocked/unexecuted scenario source and neighboring evidence. Requires correction and strict source eligibility filtering. |
| `1f4df5ac-467a-4095-9f19-f106830c1d35` `health-staffing` | Healthcare / root cause | Intermittent health-center staffing coverage; operational cause is uncertain. | Service availability, infrastructure, governance, and resource hypotheses. | Direct staffing source plus `CONTROLLED_PROJECT_PILOT`. | Generic hypotheses and unrelated shared evidence. Requires correction. |
| `988fb70e-a0b1-431a-ab02-aefb2f4865f2` `health-appointment-access` | Healthcare / recommendation | Difficulty obtaining routine facility appointments. | Administrative coordination, community outreach, and service-access monitoring. | Direct appointment source plus staffing source and `CONTROLLED_PROJECT_PILOT`. | Options are generic and the staffing dependency is not established by the scenario. Requires correction and evidence-specific feasibility. |
| `a6bfea41-eb53-4306-a88b-fe8f8ad26ac8` `health-facility-hours` | Healthcare / RAG | Facility hours and service availability require verification. | Generic grounded answer. | Direct facility-hours source plus appointment, staffing, and `CONTROLLED_PROJECT_PILOT` sources. | Cross-scenario retrieval reduces evidence specificity. Requires correction. |
| `22f4c06b-62d6-4102-adba-d93974bd6adc` `housing-roof-leaks` | Housing / root cause | Recurring community-facility roof leakage; maintenance cause is uncertain. | Service availability, infrastructure, governance, and household-access hypotheses. | Direct roof-leak source only. | Generic hypotheses do not establish building-envelope causes from the available evidence. Requires correction. |
| `28f8ca98-4391-483b-a591-db13acf047fb` `housing-market-shed` | Housing / recommendation | Market shed is unusable during rain; maintenance options are needed. | Administrative coordination, community outreach, and service-access monitoring. | Direct market-shed source plus roof-leak source. | Options are generic and inherit a roof-leak dependency that is not a validated causal basis for market-shed usability. Requires correction. |
| `4900fed5-b800-4944-91c1-06e2f5fd7036` `housing-community-facility` | Housing / RAG | Community-facility condition and missing inspection evidence. | Generic grounded answer. | Direct facility source plus market-shed and roof-leak sources. | Related evidence is retrieved without a clear source-specific justification. Requires correction. |
| `d3e58fe2-8da5-449b-9b13-5caecb8166b8` `livelihood-seasonal-work` | Livelihood / root cause | Seasonal employment interruptions; main livelihood constraint is uncertain. | Seasonal work dependency, credit access, local job availability, and market access. | Direct seasonal-work source plus `CONTROLLED_PROJECT_PILOT`. | Several hypotheses are plausible but not evidenced; shared source is not livelihood-specific. Requires correction. |
| `e4f8b1ae-6317-4f40-8644-7d46205b2622` `livelihood-supply-chain` | Livelihood / recommendation | Small-enterprise input supply-chain disruption. | Administrative coordination, community outreach, and service-access monitoring. | Direct supply-chain source plus seasonal-work source and `CONTROLLED_PROJECT_PILOT`. | Options are generic and the seasonal-work dependency is unsupported. Requires correction. |
| `7bcceac4-a01f-4ab8-ba60-ae2a7ff717e7` `livelihood-artisan-markets` | Livelihood / RAG | Artisan market information and missing buyer records. | Generic grounded answer. | Direct artisan-market source plus seasonal-work, supply-chain, and `CONTROLLED_PROJECT_PILOT` sources. | Cross-scenario retrieval is substantial; answer does not distinguish buyer information from employment or supply-chain evidence. Requires correction. |
| `4c823c36-4045-4081-b06b-20a6e1a4ee01` `water-latrine-overflow` | Water and sanitation / root cause | Shared latrine overflow after rain; containment responsibility is uncertain. | Service availability, infrastructure, governance, and household-access hypotheses. | Direct latrine source only. | Semantically overlaps the immutable v0.4 sanitation-drainage overflow root-cause scenario. Recommend rejection from v0.5 and replacement rather than approval. |
| `ffaa3c74-8351-4fe9-a324-7310e41319a5` `water-waste-collection` | Water and sanitation / recommendation | Irregular solid-waste collection and blocked drains near a market-side water point. | Repair/accountability, water-source monitoring, and government-scheme facilitation. | Direct source plus `development-evaluation-fixture`, `approved-synthetic-rural-policy`, `approved-water-policy`, and `approved-rural-development-manual`. | Explicit development-only synthetic evidence entered retrieval, and recommendations target water-source reliability rather than waste collection. Recommend rejection from v0.5 and re-generation with isolated governed evidence. |
| `3db3bc11-291d-4b57-902e-4f0d6b790bd4` `water-toilet-access` | Water and sanitation / RAG | Household toilet access and safe waste-disposal evidence gaps. | Generic grounded answer. | Direct toilet-access source plus latrine-overflow source. | Topic is adjacent to the v0.4 sanitation-drainage family and shares the new latrine evidence. It may be retained only after a human confirms the distinction and narrows citations. Requires correction/review. |

## Root-Cause Repetition

The root-cause output is not sufficiently domain-specific. The following
generic labels recur across independent domains:

| Repeated hypothesis | Affected completed scenarios |
|---|---:|
| `infrastructure reliability issue` | 4 |
| `governance accountability gap` | 4 |
| `service availability gap` | 4 |
| `household access barrier` | 3 |
| `resource constraint` | 3 |
| `market access limitation` | 2 |

These repetitions are not automatically invalid because the causes can occur
in multiple domains. They are a quality concern because the supporting
evidence does not establish the same causal label in most of those rows.

## Recommendation Repetition

The recommendation engine returned generic intervention families rather than
domain-specific options:

| Repeated recommendation | Affected scenarios |
|---|---:|
| `Administrative coordination` | Healthcare, housing, livelihoods |
| `Community outreach` | Healthcare, housing, livelihoods |
| `Service access monitoring` | Healthcare, housing, livelihoods |

The agriculture recommendation proposes irrigation, soil/crop advisory, and
market-access actions for a seed-storage problem. The water recommendation
proposes water-source reliability actions for a waste-collection problem.
Those are unsupported target mismatches, not useful diversity.

## Evidence and Citation Findings

- All 22 candidates have a unique direct `PILOT_V05_*` source ID and a direct
  scenario evidence block.
- `CONTROLLED_PROJECT_PILOT` is retrieved by `11` candidates, including
  candidates in agriculture, climate, healthcare, and livelihoods. This is a
  shared broad evidence block, not scenario-specific evidence.
- `13` v0.5 scenario source IDs are reused as retrieved context in another
  v0.5 candidate, generally because the RAG collection is shared within a
  domain. This causes neighboring scenarios to cite one another without a
  recorded relevance decision.
- The energy grid RAG candidate retrieved the blocked solar-maintenance source.
  A failed/unpersisted evaluation must not be eligible retrieval evidence.
- The water waste-collection candidate retrieved `development-evaluation-fixture`
  and other legacy policy fixtures. This violates the v0.5 requirement to keep
  development synthetic evidence out of candidate quality and training paths.
- Direct source IDs are present in the input context, but source-ID presence
  alone does not prove that the cited evidence supports the claim.

## Semantic Diversity Findings

### Near-duplicate or materially adjacent scenarios

1. `water-latrine-overflow` is substantially equivalent to the v0.4
   `pilot-v03-exp3-sanitation-drainage-root-001`: both describe post-rain
   overflow and unclear maintenance/containment responsibility. The wording
   and facility label change, but the decision problem is materially the same.
2. `water-toilet-access` is adjacent to the same v0.4 sanitation evidence
   family. It is potentially distinct as household access, but its retrieved
   overflow source makes the distinction unclear.
3. `education-teacher-attendance` is adjacent to the v0.4 education attendance
   scenarios. Teacher attendance is a different actor from student attendance,
   but the candidate needs evidence that proves the actor distinction.
4. The eight RAG scenarios reuse the same response template: request for
   grounded guidance, missing records, generic evidence-limit answer. The
   topics differ, but the task content is largely a wording variation unless
   domain-specific retrieved evidence is isolated and used.

### Exact and measured duplicates

- Exact scenario-key duplicates within the 22 candidates: `0`.
- Identifier-level uniqueness therefore passes, but it is insufficient for
  semantic approval.
- The earlier v0.4 audit reported 10 near-duplicate v0.4 pairs at its 0.45
  Jaccard threshold. The new audit confirms that the v0.5 sanitation overflow
  candidate remains materially adjacent to one of those v0.4 scenarios.

## Candidates Recommended for Approval

None should be approved **as-is**. The direct evidence blocks and provenance
are present, but every candidate has at least one material issue in output
specificity, retrieval isolation, recommendation grounding, or overlap with
existing data. Human approval should be performed only after the affected
candidate is corrected and re-evaluated through the existing gates.

## Candidates Requiring Correction

The following 20 candidates should remain pending and require governed
correction/re-evaluation rather than automatic approval:

- All agriculture candidates.
- All climate candidates.
- All education candidates.
- All energy candidates.
- All healthcare candidates.
- All livelihood candidates.
- `water-toilet-access`.
- All housing candidates.

The corrections must use scenario-specific evidence, remove unrelated
retrieved sources, preserve uncertainty, and make root causes or
recommendations materially supportable.

## Candidates Recommended for Rejection or Replacement

These recommendations do not change live decisions:

1. `water-latrine-overflow`: reject from v0.5 and replace because it
   duplicates the v0.4 sanitation-drainage decision problem.
2. `water-waste-collection`: reject from v0.5 and re-run because development
   synthetic evidence and unrelated water-policy sources entered the result.

The records should remain available in the audit trail; rejection here means
the recommended dataset action, not a persisted human review decision.

## Blocked Recommendations

These two scenarios were not persisted as completed candidates and remain
blocked by `VALIDATED_ROOT_CAUSE_REQUIRED`:

- `pilot-v05-energy-solar-maintenance-recommendation-generation-001`
- `pilot-v05-climate-flood-resilience-recommendation-generation-001`

No root cause should be fabricated or auto-validated to unblock them.

## Remaining Gaps and Required Next Actions

1. Isolate v0.5 evidence retrieval by scenario or apply an explicit relevance
   filter before citation and evaluation.
2. Exclude failed, blocked, development-synthetic, and unrelated legacy
   sources from v0.5 retrieval.
3. Re-run root-cause candidates with domain-specific hypotheses grounded in
   the direct evidence.
4. Re-run recommendation candidates only with a validated root cause that
   actually addresses the scenario, and require intervention-specific
   evidence, feasibility, risk, and uncertainty.
5. Replace the sanitation-overflow duplicate and the contaminated
   waste-collection candidate through the governed pilot workflow.
6. Keep all 22 current candidates pending until corrected results have passed
   quality, PII, provenance, citation, and diversity gates and a human reviewer
   explicitly decides each record.
7. Do not materialize dataset v0.5 until the corrected set has complete review
   decisions and passes split, domain, task, semantic, evidence, and leakage
   validation.

## Audit Conclusion

The candidate set is structurally diverse by domain and task, but it is not
yet semantically or evidentially ready for approval. The main risks are shared
retrieval contamination, generic repeated hypotheses/interventions, one
development-synthetic evidence leak, and the sanitation near-duplicate. No
candidate decision was changed.
