# Scheme Matching

## Purpose

Defines how the recommendation engine surfaces possible government-scheme alignment without claiming eligibility.

## Matching Inputs

- Recommendation intervention type.
- Domain.
- RAG citations from trusted knowledge sources.
- Target population.
- Knowledge snapshot version.

## Output Contract

Scheme matches include scheme name, source, eligibility evidence, applicable population, relevant benefit, limitations, source date/version, and status.

## Eligibility Safety Rule

The canonical status for generated scheme matches is `ELIGIBILITY_REQUIRES_VERIFICATION` unless the system has verified eligibility criteria, local administrative evidence, beneficiary records, and current scheme rules. The current implementation intentionally keeps scheme matches cautious because local eligibility is not fully represented in Sprint data.

## Trade-offs

This conservative design reduces false eligibility claims and keeps public-sector recommendations auditable. The trade-off is that reviewers must confirm scheme details manually until future milestones add structured scheme catalogs and eligibility rule engines.
