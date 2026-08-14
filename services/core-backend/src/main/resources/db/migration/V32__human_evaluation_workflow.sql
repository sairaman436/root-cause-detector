-- Purpose: Persists authenticated human-quality reviews for the immutable held-out evaluation set.
-- Why it exists: Structural evaluation cannot establish semantic quality, usefulness, or uncertainty handling.
-- Architecture fit: Adds a review-only evaluation boundary without changing training data, evaluation artifacts, or training-review governance.

CREATE TABLE IF NOT EXISTS evaluation.human_evaluations (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    evaluation_set_version   VARCHAR(80)  NOT NULL,
    example_id               VARCHAR(160) NOT NULL,
    task                     VARCHAR(80)  NOT NULL,
    model_version            VARCHAR(160) NOT NULL,
    prompt_version           VARCHAR(200) NOT NULL,
    rubric_version           VARCHAR(80)  NOT NULL,
    inference_configuration JSONB        NOT NULL,
    output_sha256            CHAR(64)     NOT NULL,
    reviewer_id              UUID         NOT NULL REFERENCES identity.users(id),
    reviewer_comments        TEXT,
    evidence_references_used JSONB        NOT NULL DEFAULT '[]'::jsonb,
    reviewed_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_human_evaluation_reviewer UNIQUE (evaluation_set_version, example_id, reviewer_id)
);

CREATE TABLE IF NOT EXISTS evaluation.human_evaluation_scores (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    human_evaluation_id      UUID NOT NULL UNIQUE REFERENCES evaluation.human_evaluations(id) ON DELETE CASCADE,
    root_cause_quality       INTEGER CHECK (root_cause_quality BETWEEN 0 AND 4),
    recommendation_quality   INTEGER CHECK (recommendation_quality BETWEEN 0 AND 4),
    rag_evidence_quality     INTEGER CHECK (rag_evidence_quality BETWEEN 0 AND 4),
    uncertainty_handling    INTEGER CHECK (uncertainty_handling BETWEEN 0 AND 4),
    practical_usefulness     INTEGER CHECK (practical_usefulness BETWEEN 0 AND 4)
);

CREATE INDEX IF NOT EXISTS idx_human_evaluations_set_example
    ON evaluation.human_evaluations(evaluation_set_version, example_id);
CREATE INDEX IF NOT EXISTS idx_human_evaluations_reviewer_time
    ON evaluation.human_evaluations(reviewer_id, reviewed_at DESC);

