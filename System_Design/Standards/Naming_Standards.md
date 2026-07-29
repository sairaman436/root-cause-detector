# Naming_Standards.md

> **Version:** 1.0
> **Status:** Active
> **Owner:** Engineering Team
> **Applies To:** All documentation, source code, APIs, databases, infrastructure, AI assets, and repository artifacts.

---

# Purpose

This document establishes standardized naming conventions for all project artifacts to ensure consistency, readability, maintainability, and collaboration across the software development lifecycle.

Following a consistent naming strategy enables:

- Better code readability
- Easier navigation
- Faster onboarding
- Improved tooling support
- Consistent documentation
- Reduced ambiguity

---

# Table of Contents

1. General Principles
2. Repository Standards
3. Documentation Standards
4. Source Code Standards
5. API Standards
6. Database Standards
7. AI Standards
8. Infrastructure Standards
9. Git Standards
10. Configuration Standards
11. Logging Standards
12. Versioning Standards
13. Anti-Patterns
14. Review Checklist

---

# General Principles

Names should be:

- Meaningful
- Descriptive
- Consistent
- Searchable
- Concise
- Domain-Oriented

Avoid:

- Abbreviations
- Ambiguous names
- Single-letter identifiers
- Magic values
- Temporary names

Good

```
ComplaintService
```

Bad

```
CS
```

---

# Repository Standards

Repository names should use:

```
kebab-case
```

Example

```
ai-rural-root-cause-discovery
```

Folders

```
Pascal_Case
```

Example

```
04_System_Design
```

Subfolders

```
Pascal_Case
```

Example

```
Designs
Templates
Standards
```

---

# Documentation Standards

Documentation files

```
Pascal_Case.md
```

Examples

```
Frontend_Design.md

Backend_Design.md

API_Design.md
```

Templates

```
<Name>_Template.md
```

Examples

```
Database_Template.md

API_Template.md
```

Standards

```
<Name>_Standards.md
```

Examples

```
Naming_Standards.md

UML_Standards.md
```

---

# Source Code Standards

## Packages / Namespaces

```
lowercase
```

Example

```
com.company.auth

services.ai

repositories
```

---

## Classes

```
PascalCase
```

Example

```
SurveyController

RecommendationEngine

CitizenService
```

---

## Interfaces

```
PascalCase
```

Recommended Prefix

```
IRepository

ILogger
```

or language-specific conventions if preferred.

---

## Methods

```
camelCase
```

Examples

```
generateRecommendation()

validateSurvey()

saveComplaint()
```

---

## Variables

```
camelCase
```

Examples

```
surveyId

userName

confidenceScore
```

---

## Constants

```
UPPER_SNAKE_CASE
```

Examples

```
MAX_RETRY_COUNT

DEFAULT_TIMEOUT
```

---

## Enums

```
PascalCase
```

Values

```
UPPER_SNAKE_CASE
```

Example

```
SurveyStatus

PENDING

APPROVED

REJECTED
```

---

# API Standards

REST endpoints

```
kebab-case
```

Examples

```
GET /api/v1/surveys

POST /api/v1/root-causes

GET /api/v1/recommendations
```

Resources should use plural nouns.

Good

```
/users

/complaints

/reports
```

Bad

```
/getUser

/createSurvey
```

---

## JSON Fields

Use

```
camelCase
```

Example

```json
{
  "surveyId": 101,
  "villageName": "Palasa",
  "confidenceScore": 0.92
}
```

---

# Database Standards

Schemas

```
snake_case
```

Tables

```
snake_case
```

Example

```
survey_records

user_profiles

complaint_logs
```

Columns

```
snake_case
```

Example

```
created_at

updated_at

user_id
```

Primary Keys

```
id
```

Foreign Keys

```
<entity>_id
```

Example

```
user_id

survey_id
```

Indexes

```
idx_<table>_<column>
```

Example

```
idx_users_email
```

Unique Constraints

```
uk_<table>_<column>
```

Example

```
uk_users_email
```

Foreign Keys

```
fk_<table>_<referenced_table>
```

---

# AI Standards

Models

```
<domain>-<version>
```

Example

```
root-cause-v1

recommendation-v2
```

Datasets

```
<domain>_<purpose>_<version>
```

Example

```
crop_disease_training_v1
```

Prompt Files

```
prompt_<purpose>.md
```

Examples

```
prompt_summary.md

prompt_classification.md
```

Experiments

```
EXP-001

EXP-002
```

---

# Infrastructure Standards

Docker Images

```
lowercase
```

Example

```
ai-api

survey-service
```

Docker Containers

```
kebab-case
```

Kubernetes

```
kebab-case
```

Examples

```
survey-api

recommendation-engine

ai-service
```

---

# Git Standards

Branches

```
feature/

bugfix/

hotfix/

release/
```

Examples

```
feature/user-authentication

bugfix/api-timeout

release/v1.2.0
```

Commits

Use Conventional Commits.

Examples

```
feat:

fix:

docs:

refactor:

test:

chore:
```

---

# Configuration Standards

Environment Variables

```
UPPER_SNAKE_CASE
```

Examples

```
DATABASE_URL

JWT_SECRET

MODEL_PATH
```

Configuration Files

```
kebab-case
```

Example

```
application-prod.yml

docker-compose.yml
```

---

# Logging Standards

Logger Names

```
Package.Class
```

Log Fields

```
camelCase
```

Correlation IDs

```
correlationId
```

Request IDs

```
requestId
```

Trace IDs

```
traceId
```

---

# Versioning Standards

Use Semantic Versioning.

```
MAJOR.MINOR.PATCH
```

Example

```
2.4.1
```

---

# Common Anti-Patterns

Avoid

- temp
- test123
- final_final
- data1
- obj
- misc
- thing
- helper
- util (unless truly generic)
- abbreviations without documentation
- inconsistent casing
- mixed naming styles

---

# Review Checklist

## General

- [ ] Meaningful names
- [ ] Consistent casing
- [ ] Domain terminology used

## Documentation

- [ ] Correct file naming
- [ ] Correct folder naming

## Source Code

- [ ] Classes follow PascalCase
- [ ] Methods follow camelCase
- [ ] Constants follow UPPER_SNAKE_CASE

## Database

- [ ] Tables use snake_case
- [ ] Columns use snake_case
- [ ] Constraints properly named

## API

- [ ] REST resources are plural
- [ ] Endpoints use kebab-case
- [ ] JSON fields use camelCase

## Git

- [ ] Branch naming correct
- [ ] Commit message follows Conventional Commits

---

# Guiding Principle

> **Names are part of the system's design. Every identifier—whether a document, class, API endpoint, database object, or infrastructure resource—should communicate its purpose clearly, consistently, and unambiguously, enabling engineers to understand and maintain the system with minimal cognitive effort.**