-- Purpose: Creates the Rural Intelligence Research Laboratory schema for Research-1.
-- Why it exists: The platform needs a permanent research organization for projects, experiments, papers, datasets, benchmarks, hypotheses, findings, and publications.
-- Architecture fit: Adds research metadata and scientific governance without production AI capability changes.

CREATE SCHEMA IF NOT EXISTS research_lab;

CREATE TABLE research_lab.research_projects (
    id UUID PRIMARY KEY,
    project_key VARCHAR(120) NOT NULL,
    title VARCHAR(220) NOT NULL,
    division VARCHAR(120) NOT NULL,
    program VARCHAR(120) NOT NULL,
    objective TEXT NOT NULL,
    principal_investigator VARCHAR(160) NOT NULL,
    status VARCHAR(40) NOT NULL,
    governance_state VARCHAR(80) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_research_project_key UNIQUE (project_key),
    CONSTRAINT ck_research_project_status CHECK (status IN ('PROPOSED','ACTIVE','PAUSED','COMPLETED','ARCHIVED')),
    CONSTRAINT ck_research_project_governance CHECK (governance_state IN ('SCIENTIFIC_GOVERNANCE_REQUIRED','APPROVED','REJECTED','EXTERNAL_REVIEW_REQUIRED'))
);

CREATE TABLE research_lab.research_experiments (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    experiment_key VARCHAR(120) NOT NULL,
    title VARCHAR(220) NOT NULL,
    hypothesis TEXT NOT NULL,
    methodology TEXT NOT NULL,
    benchmark_suite VARCHAR(160) NOT NULL,
    approval_status VARCHAR(40) NOT NULL,
    replication_status VARCHAR(80) NOT NULL,
    reproducibility_report TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_research_experiment_project FOREIGN KEY (project_id) REFERENCES research_lab.research_projects(id) ON DELETE CASCADE,
    CONSTRAINT uq_research_experiment_key UNIQUE (experiment_key),
    CONSTRAINT ck_research_experiment_approval CHECK (approval_status IN ('DRAFT','APPROVED','REJECTED','PAUSED')),
    CONSTRAINT ck_research_experiment_replication CHECK (replication_status IN ('REPLICATION_REQUIRED','REPLICATED','FAILED_REPLICATION','NOT_APPLICABLE'))
);

CREATE TABLE research_lab.research_papers (
    id UUID PRIMARY KEY,
    project_id UUID,
    title VARCHAR(260) NOT NULL,
    authors TEXT NOT NULL,
    source VARCHAR(220) NOT NULL,
    doi VARCHAR(180),
    topic VARCHAR(120) NOT NULL,
    review_status VARCHAR(60) NOT NULL,
    trust_score DOUBLE PRECISION NOT NULL,
    discovered_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_research_paper_project FOREIGN KEY (project_id) REFERENCES research_lab.research_projects(id) ON DELETE SET NULL,
    CONSTRAINT ck_research_paper_trust CHECK (trust_score >= 0 AND trust_score <= 1)
);

CREATE TABLE research_lab.research_datasets (
    id UUID PRIMARY KEY,
    project_id UUID,
    dataset_key VARCHAR(120) NOT NULL,
    title VARCHAR(220) NOT NULL,
    source VARCHAR(220) NOT NULL,
    license VARCHAR(160) NOT NULL,
    quality_score DOUBLE PRECISION NOT NULL,
    governance_status VARCHAR(60) NOT NULL,
    registered_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_research_dataset_project FOREIGN KEY (project_id) REFERENCES research_lab.research_projects(id) ON DELETE SET NULL,
    CONSTRAINT uq_research_dataset_key UNIQUE (dataset_key),
    CONSTRAINT ck_research_dataset_quality CHECK (quality_score >= 0 AND quality_score <= 1)
);

CREATE TABLE research_lab.research_benchmarks (
    id UUID PRIMARY KEY,
    benchmark_key VARCHAR(120) NOT NULL,
    domain VARCHAR(120) NOT NULL,
    task_type VARCHAR(120) NOT NULL,
    metric_definition TEXT NOT NULL,
    baseline TEXT NOT NULL,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_research_benchmark_key UNIQUE (benchmark_key),
    CONSTRAINT ck_research_benchmark_status CHECK (status IN ('DRAFT','ACTIVE','RETIRED'))
);

CREATE TABLE research_lab.research_hypotheses (
    id UUID PRIMARY KEY,
    project_id UUID,
    statement TEXT NOT NULL,
    rationale TEXT NOT NULL,
    status VARCHAR(40) NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_research_hypothesis_project FOREIGN KEY (project_id) REFERENCES research_lab.research_projects(id) ON DELETE SET NULL,
    CONSTRAINT ck_research_hypothesis_confidence CHECK (confidence >= 0 AND confidence <= 1)
);

CREATE TABLE research_lab.research_findings (
    id UUID PRIMARY KEY,
    project_id UUID,
    experiment_id UUID,
    title VARCHAR(220) NOT NULL,
    summary TEXT NOT NULL,
    evidence_ref VARCHAR(240) NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    replication_status VARCHAR(80) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_research_finding_project FOREIGN KEY (project_id) REFERENCES research_lab.research_projects(id) ON DELETE SET NULL,
    CONSTRAINT fk_research_finding_experiment FOREIGN KEY (experiment_id) REFERENCES research_lab.research_experiments(id) ON DELETE SET NULL,
    CONSTRAINT ck_research_finding_confidence CHECK (confidence >= 0 AND confidence <= 1)
);

CREATE TABLE research_lab.publications (
    id UUID PRIMARY KEY,
    project_id UUID,
    publication_type VARCHAR(80) NOT NULL,
    title VARCHAR(260) NOT NULL,
    abstract_text TEXT NOT NULL,
    authors TEXT NOT NULL,
    review_status VARCHAR(60) NOT NULL,
    uri VARCHAR(300) NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_publication_project FOREIGN KEY (project_id) REFERENCES research_lab.research_projects(id) ON DELETE SET NULL,
    CONSTRAINT ck_publication_type CHECK (publication_type IN ('RESEARCH_PAPER','TECHNICAL_REPORT','EXPERIMENT_REPORT','BENCHMARK','WHITEPAPER','INTERNAL_RFC','SCIENTIFIC_REVIEW','RESEARCH_CHARTER')),
    CONSTRAINT ck_publication_review CHECK (review_status IN ('DRAFT','IN_REVIEW','APPROVED','PUBLISHED','REJECTED'))
);

CREATE INDEX idx_research_project_division_program ON research_lab.research_projects(division, program, status);
CREATE INDEX idx_research_experiment_project_status ON research_lab.research_experiments(project_id, approval_status, replication_status);
CREATE INDEX idx_research_paper_topic_review ON research_lab.research_papers(topic, review_status, trust_score);
CREATE INDEX idx_research_dataset_governance ON research_lab.research_datasets(governance_status, quality_score);
CREATE INDEX idx_research_benchmark_domain_status ON research_lab.research_benchmarks(domain, status);
CREATE INDEX idx_research_hypothesis_project_status ON research_lab.research_hypotheses(project_id, status);
CREATE INDEX idx_research_finding_created ON research_lab.research_findings(created_at, confidence);
CREATE INDEX idx_publication_type_review ON research_lab.publications(publication_type, review_status, published_at);
