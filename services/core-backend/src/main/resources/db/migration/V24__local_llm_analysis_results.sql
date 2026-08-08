-- Purpose: Adds durable storage for validated local LLM root-cause analysis results.
-- Why it exists: Real Ollama/Qwen analysis must be traceable to survey context, prompt version, model metadata, latency, request ID, and success/failure status without storing sensitive prompt content.
-- Architecture fit: Extends the AI bounded context with provider-neutral analysis persistence for the local LLM integration milestone.

CREATE TABLE ai.llm_analysis_results (
    id UUID PRIMARY KEY,
    request_id UUID NOT NULL UNIQUE,
    survey_id UUID NOT NULL,
    submission_id UUID,
    requested_by_user_id UUID NOT NULL,
    provider VARCHAR(80) NOT NULL,
    model VARCHAR(180) NOT NULL,
    model_version VARCHAR(180),
    prompt_id VARCHAR(120) NOT NULL,
    prompt_version VARCHAR(40) NOT NULL,
    status VARCHAR(40) NOT NULL,
    latency_ms BIGINT NOT NULL,
    tokens_estimate INTEGER NOT NULL,
    result_json TEXT NOT NULL,
    error_code VARCHAR(120),
    error_message VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_llm_analysis_survey FOREIGN KEY (survey_id) REFERENCES survey.surveys(id),
    CONSTRAINT fk_llm_analysis_submission FOREIGN KEY (submission_id) REFERENCES survey.survey_submissions(id),
    CONSTRAINT fk_llm_analysis_user FOREIGN KEY (requested_by_user_id) REFERENCES identity.users(id),
    CONSTRAINT chk_llm_analysis_status CHECK (status IN ('SUCCEEDED', 'FAILED')),
    CONSTRAINT chk_llm_analysis_latency CHECK (latency_ms >= 0),
    CONSTRAINT chk_llm_analysis_tokens CHECK (tokens_estimate >= 0)
);

CREATE INDEX idx_llm_analysis_survey_created ON ai.llm_analysis_results (survey_id, created_at DESC);
CREATE INDEX idx_llm_analysis_submission_created ON ai.llm_analysis_results (submission_id, created_at DESC);
CREATE INDEX idx_llm_analysis_provider_model ON ai.llm_analysis_results (provider, model, created_at DESC);
CREATE INDEX idx_llm_analysis_prompt ON ai.llm_analysis_results (prompt_id, prompt_version, created_at DESC);
CREATE INDEX idx_llm_analysis_status_created ON ai.llm_analysis_results (status, created_at DESC);
