-- Purpose: Connect real learning candidates to authenticated review and dataset lineage.
-- Why it exists: Candidate decisions must preserve reviewer identity, corrections, rejection evidence, synthetic status, and dataset version.
-- Architecture fit: Extends the existing AI-7 learning schema without creating a second governance system or touching synthetic JSONL fixtures.

ALTER TABLE learning.learning_records ADD COLUMN IF NOT EXISTS task_type VARCHAR(80);
ALTER TABLE learning.learning_records ADD COLUMN IF NOT EXISTS scenario_group VARCHAR(180);
ALTER TABLE learning.learning_records ADD COLUMN IF NOT EXISTS synthetic BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE learning.learning_records ADD COLUMN IF NOT EXISTS dataset_version VARCHAR(80);

ALTER TABLE learning.human_reviews ADD COLUMN IF NOT EXISTS reviewer_user_id UUID REFERENCES identity.users(id);
ALTER TABLE learning.human_reviews ADD COLUMN IF NOT EXISTS correction_validated BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE learning.training_candidates ADD COLUMN IF NOT EXISTS reviewer_user_id UUID REFERENCES identity.users(id);
ALTER TABLE learning.training_candidates ADD COLUMN IF NOT EXISTS dataset_version VARCHAR(80);
ALTER TABLE learning.training_candidates ADD COLUMN IF NOT EXISTS synthetic BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE learning.training_candidates ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMP;
ALTER TABLE learning.training_candidates ADD COLUMN IF NOT EXISTS review_decision VARCHAR(40);
ALTER TABLE learning.training_candidates ADD COLUMN IF NOT EXISTS review_id UUID REFERENCES learning.human_reviews(id);

ALTER TABLE learning.approval_workflows ADD COLUMN IF NOT EXISTS reviewer_user_id UUID REFERENCES identity.users(id);

CREATE INDEX IF NOT EXISTS idx_learning_records_training_review ON learning.learning_records(approval_status, training_eligible, synthetic);
CREATE INDEX IF NOT EXISTS idx_training_candidates_review_status ON learning.training_candidates(approval_status, synthetic, created_at);
CREATE INDEX IF NOT EXISTS idx_training_candidates_review_id ON learning.training_candidates(review_id);
CREATE INDEX IF NOT EXISTS idx_human_reviews_reviewer ON learning.human_reviews(reviewer_user_id, reviewed_at);
