# Git_Workflow.md

> **Document Version:** 1.0
> **Status:** Approved
> **Owner:** Engineering Team
> **Project:** AI Rural Root Cause Discovery System
> **Last Updated:** 2026-07-28

---

# Git Workflow

---

# Document Information

| Field | Value |
|---------|---------|
| Project | AI Rural Root Cause Discovery System |
| Module | Git Workflow |
| Version | 1.0 |
| Status | Approved |
| Owner | Engineering Team |

---

# Purpose

This document defines the Git workflow used throughout the project to ensure:

- Consistent collaboration
- High code quality
- Controlled releases
- Traceable development history
- Reliable CI/CD automation

---

# Objectives

The Git workflow shall:

- Standardize branching
- Support parallel development
- Protect production code
- Simplify releases
- Enable rapid hotfixes
- Maintain repository integrity

---

# Repository Model

The project follows a **GitFlow-inspired workflow** with protected long-lived branches and short-lived feature branches.

```text
main
│
├── develop
│     ├── feature/*
│     ├── bugfix/*
│     └── spike/*
│
├── release/*
│
└── hotfix/*
```

---

# Branch Definitions

## main

Purpose

- Production-ready code only

Characteristics

- Protected branch
- No direct commits
- Tagged releases only

---

## develop

Purpose

- Integration branch for ongoing development

Characteristics

- Receives completed features
- Continuously tested
- Stable enough for staging deployments

---

## feature/*

Examples

```text
feature/authentication

feature/ai-prediction

feature/dashboard
```

Purpose

Develop new functionality.

Created from

```text
develop
```

Merged into

```text
develop
```

Delete after merge.

---

## bugfix/*

Examples

```text
bugfix/login-timeout

bugfix/report-export
```

Purpose

Fix defects discovered during development or testing.

---

## release/*

Examples

```text
release/v1.0.0

release/v1.1.0
```

Purpose

Prepare production releases.

Activities

- Final testing
- Documentation updates
- Version bump
- Release notes

Merge into

- main
- develop

---

## hotfix/*

Examples

```text
hotfix/security-patch

hotfix/payment-timeout
```

Purpose

Critical production fixes.

Created from

```text
main
```

Merged into

- main
- develop

---

# Branch Protection Rules

Protect

- main
- develop

Restrictions

- No force pushes
- No direct commits
- Pull Request required
- Successful CI required
- Required approvals
- Signed commits (recommended)

---

# Feature Development Workflow

```text
develop

↓

Create feature branch

↓

Implement feature

↓

Local testing

↓

Commit changes

↓

Push feature branch

↓

Open Pull Request

↓

Code Review

↓

CI Validation

↓

Merge into develop

↓

Delete feature branch
```

---

# Pull Request Workflow

Every Pull Request shall include:

- Clear title
- Description
- Linked work item
- Testing evidence
- Screenshots (UI changes)
- Updated documentation (if applicable)

---

# Pull Request Checklist

- Code compiles
- Tests pass
- Linting passes
- Static analysis passes
- Documentation updated
- No merge conflicts
- Reviewer approval received

---

# Commit Message Convention

Follow the Conventional Commits specification.

Format

```text
<type>(scope): short description
```

Examples

```text
feat(auth): add JWT authentication

fix(api): handle null survey response

docs(readme): update installation guide

refactor(ai): simplify prediction service

test(survey): add validation tests

chore(ci): update GitHub Actions
```

---

# Commit Types

| Type | Purpose |
|--------|----------|
| feat | New feature |
| fix | Bug fix |
| docs | Documentation |
| style | Formatting only |
| refactor | Code restructuring |
| perf | Performance improvement |
| test | Tests |
| build | Build system changes |
| ci | CI/CD changes |
| chore | Maintenance |
| revert | Revert previous commit |

---

# Merge Strategy

Preferred

- Squash and Merge for feature branches

Allowed

- Rebase and Merge (small, linear histories)

Avoid

- Merge commits unless preserving history is necessary.

---

# Release Workflow

```text
develop

↓

release/vX.Y.Z

↓

Regression Testing

↓

Documentation Review

↓

Version Update

↓

Merge → main

↓

Create Git Tag

↓

Deploy Production

↓

Merge Back → develop
```

---

# Versioning

Use Semantic Versioning.

Format

```text
MAJOR.MINOR.PATCH
```

Example

```text
1.4.2
```

Definitions

- MAJOR — Breaking changes
- MINOR — Backward-compatible features
- PATCH — Backward-compatible fixes

---

# Git Tags

Release tags

```text
v1.0.0

v1.1.0

v2.0.0
```

Tags shall reference production releases only.

---

# Hotfix Workflow

```text
main

↓

hotfix/security

↓

Testing

↓

Merge → main

↓

Tag Release

↓

Merge → develop
```

---

# Conflict Resolution

Developers shall:

- Pull latest changes before starting work
- Rebase frequently where appropriate
- Resolve conflicts locally
- Re-run tests before pushing

---

# CI/CD Integration

Every push shall trigger:

- Build
- Unit tests
- Linting
- Static analysis
- Dependency scanning

Pull Requests additionally trigger:

- Integration tests
- Code coverage checks
- Security scanning

---

# Repository Hygiene

Maintain

- Small Pull Requests
- Frequent commits
- Descriptive branch names
- Deleted merged branches
- Updated documentation

Avoid

- Large unrelated changes
- Binary files unless required
- Committing secrets
- Committing generated artifacts

---

# Security Practices

Never commit:

- API keys
- Passwords
- Private certificates
- Access tokens
- Environment secrets

Use

- `.gitignore`
- Secret scanning
- Environment variables
- Secure secret management

---

# Emergency Procedures

If an incorrect commit reaches `main`:

1. Assess impact
2. Create hotfix branch
3. Prepare corrective commit
4. Review
5. Merge
6. Tag release
7. Document incident

---

# Metrics

Track

- Lead time for changes
- Deployment frequency
- Pull Request review time
- Merge success rate
- Build success rate
- Change failure rate

---

# References

- Branching Strategy
- Code Review Guidelines
- CI/CD Documentation
- Secure Coding Standards
- Architecture Decision Records (ADRs)

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | 2026-07-28 | Initial Version | Engineering Team |