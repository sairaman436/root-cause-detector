# Purpose: Verifies that the Milestone 1 repository foundation files exist.
# Why it exists: Provides a quick sanity check for bootstrap completeness before Milestone 2 begins.
# Architecture fit: Supports the implementation backlog gate between repository bootstrap and core platform work.
$required = @(
  "pom.xml",
  "package.json",
  "pyproject.toml",
  "docker-compose.yml",
  ".github/workflows/ci.yml",
  "services/core-backend/pom.xml",
  "apps/web-portal/package.json",
  "services/ai-inference-service/pyproject.toml",
  "services/ai-inference-service/src/ai_inference_service/main.py",
  "services/rag-service/pyproject.toml",
  "services/rag-service/src/rag_service/main.py",
  "services/agent-orchestrator/pyproject.toml",
  "services/agent-orchestrator/src/agent_orchestrator/main.py",
  "services/reporting-service/pyproject.toml",
  "services/reporting-service/src/reporting_service/main.py",
  "services/notification-service/pyproject.toml",
  "services/notification-service/src/notification_service/main.py",
  "apps/admin-portal/package.json",
  "apps/admin-portal/src/app/api/health/route.ts",
  "apps/web-portal/src/app/api/health/route.ts",
  "config/env/local.example.env",
  "docs/engineering/DEVELOPER_WORKFLOW.md"
)

$missing = $required | Where-Object { -not (Test-Path $_) }
if ($missing.Count -gt 0) {
  Write-Error ("Missing foundation files: " + ($missing -join ", "))
  exit 1
}

Write-Host "Milestone 1 foundation file check passed."
