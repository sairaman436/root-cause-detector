# Causal Reasoning

## Purpose

Defines how the platform reasons about causes without overstating causality.

## Framework

The engine uses: observed problem, candidate factor, evidence, potential mechanism, candidate cause, alternative explanation, and confidence. Each causal graph edge stores relationship type, confidence, evidence references, and source.

## Rules

Correlation is not treated as causation. Candidate causes are phrased as likely contributing factors unless direct evidence is strong. Contradictory evidence is surfaced instead of silently discarded.

## Human Role

Human reviewers can accept, reject, modify, add evidence, or flag incorrect reasoning. Reviews are appended and never silently overwrite generated analysis.
