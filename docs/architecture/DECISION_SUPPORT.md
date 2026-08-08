# Decision Support

## Purpose

Defines how the platform supports human decisions after root-cause analysis.

## Principles

- Evidence precedes recommendation.
- Recommendations must link to validated root causes.
- Confidence is multi-dimensional, not a single truth claim.
- Scheme eligibility is never assumed without verified local eligibility evidence.
- Human approval is required before field execution.

## Recommendation Decision Inputs

- Validated root causes from the root-cause engine.
- Survey and evidence snapshots.
- Trusted RAG citations.
- Village and domain context.
- Available resource information.
- Human constraints and implementation boundaries.

## Outputs

Each option includes title, description, target root cause, target population, domain, intervention type, priority, expected outcomes, required resources, effort, timeframe, feasibility, risks, dependencies, evidence, confidence dimensions, assumptions, limitations, implementation plan, success indicators, and status.

## Trade-offs

The current implementation uses deterministic domain taxonomies and weighted scoring rather than an unconstrained LLM planner. This makes behavior explainable and testable, but limits novelty. Future AI expansion can add provider-generated candidates behind the same evidence, safety, and review contracts.
