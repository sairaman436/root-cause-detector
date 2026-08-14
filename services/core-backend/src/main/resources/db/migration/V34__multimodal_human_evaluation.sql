-- Purpose: Persists authenticated human reviews of immutable multimodal traces.
-- Why it exists: Browser-local scores are not auditable and the multimodal rubric uses a 1-5 scale.
-- Architecture fit: Keeps review records separate from immutable trace artifacts and the held-out text rubric.

CREATE TABLE IF NOT EXISTS evaluation.multimodal_human_reviews (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trace_id                 VARCHAR(160) NOT NULL,
    artifact_version         VARCHAR(120) NOT NULL,
    evaluation_round         VARCHAR(120) NOT NULL,
    reviewer_id              UUID NOT NULL REFERENCES identity.users(id),
    rubric_version           VARCHAR(80) NOT NULL,
    observation_quality      INTEGER NOT NULL CHECK (observation_quality BETWEEN 1 AND 5),
    evidence_relevance       INTEGER NOT NULL CHECK (evidence_relevance BETWEEN 1 AND 5),
    root_cause_quality       INTEGER NOT NULL CHECK (root_cause_quality BETWEEN 1 AND 5),
    recommendation_quality   INTEGER CHECK (recommendation_quality BETWEEN 1 AND 5),
    grounding                INTEGER NOT NULL CHECK (grounding BETWEEN 1 AND 5),
    overall_usefulness       INTEGER NOT NULL CHECK (overall_usefulness BETWEEN 1 AND 5),
    failure_classification   VARCHAR(80) NOT NULL,
    unsupported_claim_flags  JSONB NOT NULL DEFAULT '{}'::jsonb,
    reviewer_comments        TEXT,
    submission_status        VARCHAR(40) NOT NULL DEFAULT 'SUBMITTED',
    reviewed_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_multimodal_review_reviewer UNIQUE (trace_id, reviewer_id)
);

CREATE INDEX IF NOT EXISTS idx_multimodal_reviews_trace
    ON evaluation.multimodal_human_reviews(trace_id);
CREATE INDEX IF NOT EXISTS idx_multimodal_reviews_reviewer_time
    ON evaluation.multimodal_human_reviews(reviewer_id, reviewed_at DESC);

