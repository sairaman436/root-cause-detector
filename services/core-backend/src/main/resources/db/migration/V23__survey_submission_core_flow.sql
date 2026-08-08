-- Purpose: Adds the minimal persisted survey submission model required for the core application flow.
-- Why it exists: Recovery verification identified that surveys could be authored but not submitted through a durable API.
-- Architecture fit: Extends the existing survey bounded context without introducing analytics, AI, or reporting-side processing.

CREATE TABLE survey.survey_submissions (
    id UUID PRIMARY KEY,
    survey_id UUID NOT NULL,
    organization_id UUID NOT NULL,
    submitted_by_user_id UUID NOT NULL,
    status VARCHAR(40) NOT NULL,
    submitted_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_survey_submissions_survey FOREIGN KEY (survey_id) REFERENCES survey.surveys(id)
);

CREATE TABLE survey.survey_submission_answers (
    id UUID PRIMARY KEY,
    submission_id UUID NOT NULL,
    question_id UUID NOT NULL,
    question_code VARCHAR(120) NOT NULL,
    answer_value TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_submission_answers_submission FOREIGN KEY (submission_id) REFERENCES survey.survey_submissions(id) ON DELETE CASCADE,
    CONSTRAINT fk_submission_answers_question FOREIGN KEY (question_id) REFERENCES survey.survey_questions(id),
    CONSTRAINT uq_submission_answer_question UNIQUE (submission_id, question_id)
);

CREATE INDEX idx_survey_submissions_survey ON survey.survey_submissions (survey_id, submitted_at DESC);
CREATE INDEX idx_survey_submissions_org ON survey.survey_submissions (organization_id, submitted_at DESC);
CREATE INDEX idx_survey_submissions_user ON survey.survey_submissions (submitted_by_user_id, submitted_at DESC);
CREATE INDEX idx_submission_answers_question ON survey.survey_submission_answers (question_id);
