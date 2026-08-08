# Evidence Model

## Purpose

Documents evidence categories and confidence scoring for root-cause intelligence.

## Categories

The API preserves `OBSERVED_FACT`, `RETRIEVED_EVIDENCE`, `MODEL_INFERENCE`, `HYPOTHESIS`, `RECOMMENDATION`, and `CONTRADICTORY_EVIDENCE`. Model-generated assumptions are never promoted to facts.

## Scoring

Evidence confidence combines source reliability, relevance, freshness, quantity, consistency, and contradiction penalty. The score is a transparent decision-support confidence score, not a calibrated scientific probability.

## Source Priority

Village-specific survey and uploaded evidence are weighted above generic retrieved knowledge. Generic knowledge remains labeled as retrieved evidence.
