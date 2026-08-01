# Research-1 Rural Intelligence Research Laboratory

## Purpose

Research-1 establishes the Rural Intelligence Research Laboratory as a permanent research organization for next-generation rural intelligence technologies. It is not production AI development, model training, inference serving, or product feature work.

## Mission

The laboratory continuously researches:

- New models
- New algorithms
- New datasets
- New evaluation methods
- New planning systems
- New multi-agent architectures
- New decision frameworks
- New scientific discoveries

## Architecture Fit

The module is implemented under `com.airural.platform.core.research` and follows the approved backend pattern:

- REST adapter: `ResearchController`
- Application service: `ResearchLaboratoryService`
- Domain persistence: research JPA entities
- Infrastructure adapters: Spring Data repositories
- Database schema: `research_lab`
- Flyway migration: `V21__rural_intelligence_research_laboratory.sql`
- Security authorities: `RESEARCH_READ`, `RESEARCH_MANAGE`, `RESEARCHER`, `AI_SCIENTIST`, `AI_ADMIN`

## Research Divisions

The laboratory charter recognizes:

- Agriculture Intelligence
- Health Intelligence
- Education Intelligence
- Climate Intelligence
- Water Intelligence
- Livelihood Intelligence
- Infrastructure Intelligence
- Governance Intelligence
- Disaster Intelligence
- Economic Intelligence

## Long-Term Research Programs

The research roadmap recognizes:

- Autonomous Rural Planning
- Scientific Knowledge Discovery
- Policy Simulation
- Village Digital Twins
- Multi-Agent Cooperation
- Causal Discovery
- Decision Intelligence
- Satellite Intelligence
- Climate Prediction
- Economic Forecasting

## Research Workflow

1. Create governed research project.
2. Register research foundation records: benchmark, dataset registry, and literature review.
3. Propose experiment with hypothesis and methodology.
4. Approve experiment under scientific governance.
5. Track replication and reproducibility.
6. Register findings with confidence and evidence.
7. Publish reviewed outputs as papers, technical reports, benchmarks, whitepapers, RFCs, or scientific reviews.
8. Transfer production-ready findings only through future governance and engineering intake.

## Experiment Registry

Experiments track:

- Project linkage
- Experiment key
- Hypothesis
- Methodology
- Benchmark suite
- Approval status
- Replication status
- Reproducibility report

## Publication System

The publication system supports:

- Research papers
- Technical reports
- Experiment reports
- Benchmarks
- Whitepapers
- Internal RFCs
- Scientific reviews
- Research charter

## Benchmark Framework

Benchmark records support:

- Reasoning
- Policy
- Agriculture
- Health
- Education
- Climate
- Infrastructure
- Planning
- Forecasting

## Knowledge Discovery

Research-1 records discovered papers, research datasets, and literature-review sources. Automated crawling, extraction, and RAG ingestion remain owned by the knowledge acquisition platform; the laboratory consumes those outputs as governed research evidence.

## Database Schema

The `research_lab` schema includes:

- `research_projects`
- `research_experiments`
- `research_papers`
- `research_datasets`
- `research_benchmarks`
- `research_hypotheses`
- `research_findings`
- `publications`

The schema includes primary keys, foreign keys, unique keys, status checks, confidence/quality checks, and indexes for division/program, experiment status, paper review, dataset governance, benchmark domain, hypotheses, findings, and publication review.

## REST APIs

- `POST /api/v1/research/project`
- `POST /api/v1/research/experiment`
- `GET /api/v1/research/projects`
- `GET /api/v1/research/publications`
- `GET /api/v1/research/findings`

The same routes are exposed under `/research`.

## Scientific Governance

Research artifacts require:

- Scientific review before publication
- Replication status before production transfer
- Reproducibility reports for experiments
- Dataset provenance and governance state
- Benchmark traceability
- Separation from production model release and serving systems

## Research Roadmap

The next laboratory expansions should add:

- Deeper hypothesis lifecycle states
- Peer review workflows
- Experiment result artifacts
- Research knowledge graph links
- External literature discovery jobs
- Research-to-production governance handoff

## Out Of Scope

Research-1 does not build production software features, train models, deploy models, serve inference, automate agents, or change production AI behavior.
