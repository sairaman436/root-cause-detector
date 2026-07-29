# 🌿 Git Workflow

Version: 1.0

Project: AI Rural Root Cause Discovery System

---

# Purpose

This document defines the Git workflow adopted for the project.

The objective is to ensure that code changes are traceable, reviewable, and maintainable while minimizing integration conflicts.

---

# Workflow Objectives

The Git workflow should:

- Maintain a stable main branch.
- Enable parallel feature development.
- Preserve complete change history.
- Support peer review.
- Simplify debugging and rollback.
- Maintain documentation alongside code.

---

# Branch Strategy

The project follows a feature-branch workflow.

```
main
│
├── feature/survey-module
├── feature/gps-clustering
├── feature/image-verification
├── feature/root-cause-engine
├── feature/dashboard
├── feature/authentication
└── hotfix/critical-bug
```

---

# Branch Types

## main

The production-ready branch.

Rules:

- Always deployable
- Protected from direct commits
- Receives only reviewed pull requests

---

## feature/*

Used for new functionality.

Examples:

feature/survey-management

feature/evidence-engine

feature/ai-recommendations

Rules:

- One feature per branch.
- Delete after merge.

---

## hotfix/*

Used for urgent production fixes.

Example:

hotfix/login-error

hotfix/api-timeout

Rules:

- Small scope
- Immediate review
- Merge back into main

---

# Commit Standards

Every commit should represent one logical change.

Commits must be:

- Small
- Focused
- Descriptive

Avoid mixing unrelated changes.

---

# Commit Message Format

Recommended format:

```
<type>: <short description>
```

Examples:

```
feat: add complaint similarity engine

fix: resolve GPS clustering bug

docs: update AI governance rules

refactor: simplify recommendation service

test: add integration tests for survey API

style: format dashboard components

chore: update dependencies
```

---

# Pull Request Process

Every pull request should include:

- Purpose of the change
- Summary of implementation
- Related issue (if applicable)
- Testing performed
- Documentation updates
- Screenshots (for UI changes)

---

# Code Review Checklist

Reviewers should verify:

- Code correctness
- Coding standards compliance
- Security considerations
- Performance impact
- Test coverage
- Documentation updates
- Backward compatibility

---

# Merge Policy

A pull request may be merged only if:

- Review completed
- Required tests passed
- Documentation updated
- No unresolved discussions
- Merge conflicts resolved

---

# Handling Merge Conflicts

When conflicts occur:

1. Pull the latest changes.
2. Resolve conflicts locally.
3. Re-test affected functionality.
4. Commit the resolved changes.
5. Request re-review if necessary.

Never merge unresolved conflicts.

---

# Tagging Releases

Each project release should use semantic versioning.

Format:

vMAJOR.MINOR.PATCH

Examples:

v1.0.0

v1.1.0

v1.1.1

Meaning:

- MAJOR → Breaking changes
- MINOR → New backward-compatible features
- PATCH → Bug fixes

---

# Git Ignore Policy

Do not commit:

- Environment files (.env)
- API keys
- Passwords
- Build artifacts
- Temporary files
- IDE configuration
- Cache directories
- Log files

Sensitive information must never be stored in Git.

---

# Rollback Strategy

If a release introduces critical issues:

- Identify the faulty commit.
- Revert using Git history.
- Validate the rollback.
- Document the incident in the Decision Log.

Avoid rewriting shared history.

---

# Documentation Policy

Documentation changes should accompany implementation changes whenever applicable.

Examples:

New API
↓

Update API documentation

Architecture change
↓

Update architecture documentation

New AI model
↓

Update AI documentation

---

# Repository Health

The repository should always maintain:

- Clear branch history
- Meaningful commit messages
- Updated documentation
- Passing tests
- Stable main branch

---

# Final Principle

Git is more than a version control system—it is the engineering history of the project.

Every commit should help future contributors understand what changed, why it changed, and how the project evolved.