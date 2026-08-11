# Training Corpus Status

## Examples Available For Review

- Available for human review: `9`
- Source: `dataset-v0.1-dev-synthetic` review queue.
- Root-cause analysis: `3`
- Recommendation generation: `3`
- RAG-grounded responses: `3`
- Pending human review: `9`

The queue preserves each original development record, including source, scenario group, model/prompt metadata, citations, and the original AI output. Existing synthetic `VALIDATED` fields are retained as source provenance only; they are not treated as human approval.

## Review Decisions

- Explicitly approved: `0`
- Explicitly corrected: `0`
- Explicitly rejected: `0`
- Automatically approved: `0`

Each queue item requires a human decision of `APPROVE`, `CORRECT`, or `REJECT`, reviewer identity, review ID, timestamp, and decision evidence. `CORRECT` additionally requires a corrected output and explicit correction validation.

The queue is loaded at `ml-platform/training-pipelines/review-queues/dataset-v0.1-dev-synthetic-review.jsonl`. The `review_training_corpus.py decide` command records one explicit decision at a time; it does not alter the source record or any production dataset.

## Final Dataset Counts

`dataset-v0.1` was not promoted or rewritten. Its validated manifest remains:

- Accepted examples: `0`
- Train: `0`
- Validation: `0`
- Test: `0`
- Rejected examples: `0`
- Split leakage: `0`

## Validation Result

- Review queue creation: **PASS**; all 9 source records preserved and effective decisions reset to `PENDING_HUMAN_REVIEW`.
- Provenance preservation: **PASS**.
- Duplicate example ID check: **PASS**.
- No-auto-approval control: **PASS**.
- Pending queue promotion guard: **PASS**; no production dataset directory is created from pending records.
- Synthetic production exclusion: **PASS**; even an explicit review of a synthetic queue item cannot materialize it into `dataset-v0.1`.
- Existing dataset and training safety tests plus queue tests: `11 passed`.
- Queue script compilation: **PASS**.
- No training or fine-tuning was started.

## Manual Review Readiness

**READY**. Reviewers can inspect the flat review projection and preserved original record, then record APPROVE, CORRECT, or REJECT. No decision has been entered automatically.

## Remaining Blockers

- No human approval, correction, or rejection decisions have been recorded yet.
- The current queue contains only synthetic development fixtures. They remain development-only and cannot become production training data under the existing production gate.
- A non-synthetic, provenance-complete source record must receive explicit human approval or validated correction before `dataset-v0.1` can be materialized.
