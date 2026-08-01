# Purpose: Defines the production operating model for the platform.

# Why it exists: Aligns SRE, DevSecOps, MLOps, database, and support teams on routine operations.

# Architecture fit: Supports Milestone 11 enterprise readiness and long-term maintainability.

# Operations Manual

## Operating Cadence

- Release review: every deployment.
- Availability review: daily.
- Security and dependency review: weekly.
- Disaster recovery drill: monthly.
- Architecture and cost review: quarterly.

## SLOs

- API availability: 99.9 percent monthly.
- Core API p95 latency: under 500 ms for non-AI endpoints.
- Decision analysis p95 latency: under 15 seconds for governed synchronous requests.
- Backup restore validation: one successful restore per month.

## Escalation

- SRE owns availability and incident command.
- Security owns vulnerability and secret incidents.
- MLOps owns model, prompt, drift, and evaluation incidents.
- Data engineering owns pipeline and warehouse incidents.
