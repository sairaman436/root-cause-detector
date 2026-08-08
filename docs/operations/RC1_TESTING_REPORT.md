# RC1 Testing Report

Purpose: Defines the validation suite for RC1 acceptance.
Why it exists: A release candidate must be reproducible from clean commands rather than manual confidence.
Architecture fit: The test plan covers the monorepo services, frontend workspaces, Python services, Docker topology, and release metadata.

Related documents: `.ceos/constitutions/testing-constitution.md`, `MASTER_TECHNICAL_DEBT.md`, `docs/operations/RC1_RELEASE_CANDIDATE_REPORT.md`.

## Required Validation Commands

```powershell
npm run format:check
npm run typecheck
npm run test
npm run build:frontends
$env:JAVA_HOME='C:\Program Files\Java\jdk-24'; .\mvnw.cmd -B -pl services/core-backend -am test
Get-ChildItem services -Filter pyproject.toml -Recurse | ForEach-Object { Push-Location $_.Directory.FullName; python -m pytest; Pop-Location }
docker compose config --quiet
git diff --check
```

## Coverage Position

| Layer                              | RC1 Status            |
| ---------------------------------- | --------------------- |
| Backend unit and integration tests | Required for RC1 pass |
| Frontend unit and type tests       | Required for RC1 pass |
| Python AI service tests            | Required for RC1 pass |
| Docker configuration validation    | Required for RC1 pass |
| End-to-end browser tests           | Open debt             |
| Load and soak tests                | Open debt             |
| AI red-team tests                  | Open debt             |

## Acceptance Rule

RC1 cannot be released with failing required validation commands. Missing enterprise-scale test categories are recorded as release debt and must be completed before production certification.
