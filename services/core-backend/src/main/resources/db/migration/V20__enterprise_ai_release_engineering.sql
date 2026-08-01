-- Purpose: Creates the Enterprise AI Release Engineering schema for AI-10.
-- Why it exists: Rural Intelligence Foundation Model v1.0 requires semantic versioning, artifacts, release candidates, certifications, approvals, history, metrics, compatibility, and support lifecycle records.
-- Architecture fit: Adds release engineering controls without training models, deploying models, or changing inference behavior.

CREATE SCHEMA IF NOT EXISTS model_release;

CREATE TABLE model_release.release_versions (
    id UUID PRIMARY KEY,
    model_name VARCHAR(180) NOT NULL,
    semantic_version VARCHAR(40) NOT NULL,
    release_channel VARCHAR(40) NOT NULL,
    lifecycle_status VARCHAR(40) NOT NULL,
    lts BOOLEAN NOT NULL,
    model_card_json TEXT NOT NULL,
    release_notes TEXT NOT NULL,
    license TEXT NOT NULL,
    released_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_release_semver UNIQUE (semantic_version),
    CONSTRAINT ck_release_semver CHECK (semantic_version LIKE 'v%.%.%'),
    CONSTRAINT ck_release_channel CHECK (release_channel IN ('RELEASE_CANDIDATE','BETA','STABLE','LTS','EXPERIMENTAL')),
    CONSTRAINT ck_release_lifecycle CHECK (lifecycle_status IN ('RELEASE_CANDIDATE','BETA','STABLE','LTS','EXPERIMENTAL','DEPRECATED','RETIRED'))
);

CREATE TABLE model_release.release_artifacts (
    id UUID PRIMARY KEY,
    release_version_id UUID NOT NULL,
    artifact_type VARCHAR(80) NOT NULL,
    package_format VARCHAR(80) NOT NULL,
    deployment_target VARCHAR(120) NOT NULL,
    uri VARCHAR(300) NOT NULL,
    checksum_sha256 VARCHAR(128) NOT NULL,
    signature VARCHAR(180) NOT NULL,
    sbom_ref VARCHAR(240) NOT NULL,
    size_bytes BIGINT NOT NULL,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_release_artifact_version FOREIGN KEY (release_version_id) REFERENCES model_release.release_versions(id) ON DELETE CASCADE,
    CONSTRAINT uq_release_artifact UNIQUE (release_version_id, artifact_type, package_format, deployment_target),
    CONSTRAINT ck_release_artifact_status CHECK (status IN ('GENERATED','SIGNED_VALIDATED','FAILED','REVOKED'))
);

CREATE TABLE model_release.release_candidates (
    id UUID PRIMARY KEY,
    release_version_id UUID NOT NULL,
    candidate_tag VARCHAR(80) NOT NULL,
    status VARCHAR(40) NOT NULL,
    validation_status VARCHAR(40) NOT NULL,
    regression_status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_release_candidate_version FOREIGN KEY (release_version_id) REFERENCES model_release.release_versions(id) ON DELETE CASCADE,
    CONSTRAINT uq_release_candidate UNIQUE (candidate_tag),
    CONSTRAINT ck_release_candidate_status CHECK (status IN ('CREATED','VALIDATING','PROMOTED','REJECTED','RETIRED'))
);

CREATE TABLE model_release.release_certifications (
    id UUID PRIMARY KEY,
    release_version_id UUID NOT NULL,
    certification_type VARCHAR(80) NOT NULL,
    board VARCHAR(120) NOT NULL,
    score DOUBLE PRECISION NOT NULL,
    status VARCHAR(40) NOT NULL,
    evidence_ref VARCHAR(240) NOT NULL,
    certified_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_release_certification_version FOREIGN KEY (release_version_id) REFERENCES model_release.release_versions(id) ON DELETE CASCADE,
    CONSTRAINT uq_release_certification UNIQUE (release_version_id, certification_type),
    CONSTRAINT ck_release_certification_status CHECK (status IN ('PASSED','FAILED','WAIVED')),
    CONSTRAINT ck_release_certification_score CHECK (score >= 0 AND score <= 1)
);

CREATE TABLE model_release.release_approvals (
    id UUID PRIMARY KEY,
    release_version_id UUID NOT NULL,
    board VARCHAR(120) NOT NULL,
    decision VARCHAR(40) NOT NULL,
    rationale TEXT NOT NULL,
    approved_by UUID,
    decided_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_release_approval_version FOREIGN KEY (release_version_id) REFERENCES model_release.release_versions(id) ON DELETE CASCADE,
    CONSTRAINT uq_release_approval_board UNIQUE (release_version_id, board),
    CONSTRAINT ck_release_approval_decision CHECK (decision IN ('APPROVED','REJECTED','CONDITIONAL'))
);

CREATE TABLE model_release.release_history (
    id UUID PRIMARY KEY,
    release_version_id UUID NOT NULL,
    event_type VARCHAR(120) NOT NULL,
    from_status VARCHAR(40) NOT NULL,
    to_status VARCHAR(40) NOT NULL,
    rationale TEXT NOT NULL,
    actor_id UUID,
    event_hash VARCHAR(128) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_release_history_version FOREIGN KEY (release_version_id) REFERENCES model_release.release_versions(id) ON DELETE CASCADE,
    CONSTRAINT uq_release_history_hash UNIQUE (event_hash)
);

CREATE TABLE model_release.release_metrics (
    id UUID PRIMARY KEY,
    release_version_id UUID NOT NULL,
    downloads BIGINT NOT NULL,
    deployments BIGINT NOT NULL,
    failures BIGINT NOT NULL,
    rollback_rate DOUBLE PRECISION NOT NULL,
    compatibility_score DOUBLE PRECISION NOT NULL,
    adoption_score DOUBLE PRECISION NOT NULL,
    captured_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_release_metrics_version FOREIGN KEY (release_version_id) REFERENCES model_release.release_versions(id) ON DELETE CASCADE
);

CREATE TABLE model_release.compatibility_reports (
    id UUID PRIMARY KEY,
    release_version_id UUID NOT NULL,
    platform VARCHAR(80) NOT NULL,
    runtime VARCHAR(80) NOT NULL,
    hardware_profile VARCHAR(80) NOT NULL,
    status VARCHAR(40) NOT NULL,
    notes TEXT NOT NULL,
    validated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_compatibility_version FOREIGN KEY (release_version_id) REFERENCES model_release.release_versions(id) ON DELETE CASCADE,
    CONSTRAINT uq_compatibility_target UNIQUE (release_version_id, platform, runtime, hardware_profile),
    CONSTRAINT ck_compatibility_status CHECK (status IN ('PASSED','FAILED','CONDITIONAL'))
);

CREATE TABLE model_release.support_lifecycle (
    id UUID PRIMARY KEY,
    release_version_id UUID NOT NULL,
    support_tier VARCHAR(40) NOT NULL,
    support_start TIMESTAMP WITH TIME ZONE NOT NULL,
    support_end TIMESTAMP WITH TIME ZONE NOT NULL,
    retirement_policy TEXT NOT NULL,
    upgrade_path TEXT NOT NULL,
    security_patch_policy TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_support_lifecycle_version FOREIGN KEY (release_version_id) REFERENCES model_release.release_versions(id) ON DELETE CASCADE,
    CONSTRAINT uq_support_release UNIQUE (release_version_id),
    CONSTRAINT ck_support_tier CHECK (support_tier IN ('STANDARD','LTS','SECURITY_ONLY','RETIRED'))
);

CREATE INDEX idx_release_versions_status ON model_release.release_versions(lifecycle_status, released_at);
CREATE INDEX idx_release_artifacts_version_type ON model_release.release_artifacts(release_version_id, artifact_type, package_format);
CREATE INDEX idx_release_certifications_version_status ON model_release.release_certifications(release_version_id, status);
CREATE INDEX idx_release_approvals_version_decision ON model_release.release_approvals(release_version_id, decision);
CREATE INDEX idx_release_history_version_created ON model_release.release_history(release_version_id, created_at);
CREATE INDEX idx_release_metrics_version_captured ON model_release.release_metrics(release_version_id, captured_at);
CREATE INDEX idx_compatibility_version_status ON model_release.compatibility_reports(release_version_id, status, runtime);
