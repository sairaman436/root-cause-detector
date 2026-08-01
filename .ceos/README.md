# Chief Engineering Operating System

## Purpose

The Chief Engineering Operating System, or CEOS, is the permanent internal engineering handbook for the AI Rural Root Cause Discovery and Decision Intelligence Platform. It defines how engineering work is designed, reviewed, implemented, tested, secured, released, operated, and remembered.

## Why This Exists

The platform now contains identity, survey, evidence, geospatial, eventing, AI foundation, multi-agent intelligence, decision intelligence, and production hardening capabilities. Future milestones must not rely on ad hoc judgment or individual preference. CEOS creates a single operating model so every team can make consistent decisions under scale, risk, regulation, and long-term maintenance pressure.

## Authority

CEOS is binding for all future platform work. When a team proposes a change that conflicts with CEOS, the team must either align the change or record a formal exception through [Decision Record Template](templates/decision-record-template.md) and the relevant review board in [Review Boards](organization/review-boards.md).

## Operating Principles

- Engineering decisions must optimize for trust, maintainability, security, explainability, and operability over speed alone.
- Architecture decisions must preserve approved milestones and evolve through documented records, not silent redesign.
- AI behavior must be governed as production software, including traceability, evaluation, safety, rollback, and human review for consequential decisions.
- Security, privacy, and auditability are product capabilities, not late-stage compliance activities.
- Tests, observability, runbooks, and rollback paths are required parts of done work.
- Documentation must explain why a decision exists, when it applies, how to follow it, tradeoffs, best practices, and anti-patterns.

## Directory Map

- [Constitutions](constitutions/README.md): binding principles for engineering, architecture, AI, data, security, testing, DevOps, MLOps, documentation, and release governance.
- [Standards](standards/README.md): repository, coding, prompt, AI, review, and workflow standards.
- [Playbooks](playbooks/README.md): execution procedures for implementation, incident response, design review, migration, release, and operational readiness.
- [Organization](organization/README.md): team boundaries, review boards, accountability, escalation, and decision authority.
- [Templates](templates/README.md): decision records, architecture proposals, engineering plans, security reviews, release reviews, and incident reviews.
- [Memory](memory/PROJECT_MEMORY.md): durable project context, approved milestone history, invariants, and lessons.

## Related Documents

CEOS does not replace approved architecture and milestone documents. It governs how those documents are used and how future documents are created. Related repository documentation includes [docs/engineering/CODING_STANDARDS.md](../docs/engineering/CODING_STANDARDS.md), [docs/engineering/DEPENDENCY_RULES.md](../docs/engineering/DEPENDENCY_RULES.md), [docs/operations/OPERATIONS_MANUAL.md](../docs/operations/OPERATIONS_MANUAL.md), and [docs/operations/ENTERPRISE_READINESS_CHECKLIST.md](../docs/operations/ENTERPRISE_READINESS_CHECKLIST.md).

## Change Governance

Changes to CEOS require review by the Engineering Council, Architecture Review Board, Security Review Board, and any affected specialist board listed in [Review Boards](organization/review-boards.md). Editorial fixes may be approved by Documentation Engineering when they do not change policy.
