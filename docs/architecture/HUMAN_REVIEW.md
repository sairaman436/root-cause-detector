# Human Review

## Purpose

Defines the validation workflow for generated root-cause analyses.

## Actions

Reviewers may accept, reject, modify, add evidence, or flag incorrect reasoning. Each review stores reviewer, action, notes, optional modified analysis, additional evidence, correction, and timestamp.

## Governance Rule

Generated analyses remain immutable. Human reviews append correction records and update review status, but do not silently overwrite the original AI-assisted analysis.

## Operational Use

Review is required when confidence is below the configured threshold, contradictory evidence is present, or consequential recommendations will drive field action.
