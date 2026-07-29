# Purpose: Runs repository build and quality checks for every operational foundation service.
# Why it exists: Gives engineers a single local command matching CI before feature development begins.
# Architecture fit: Coordinates Maven, Node, Python, and Docker validation for the approved monorepo.
$ErrorActionPreference = "Stop"

function Invoke-Checked {
  param(
    [Parameter(Mandatory = $true)]
    [scriptblock] $Command,
    [Parameter(Mandatory = $true)]
    [string] $Name
  )

  Write-Host "==> $Name"
  & $Command
  if ($LASTEXITCODE -ne 0) {
    throw "$Name failed with exit code $LASTEXITCODE"
  }
}

if (-not $env:JAVA_HOME) {
  $jdk = Get-ChildItem "C:\Program Files\Java" -Directory -ErrorAction SilentlyContinue |
    Sort-Object Name -Descending |
    Select-Object -First 1

  if ($jdk) {
    $env:JAVA_HOME = $jdk.FullName
  }
}

if (-not $env:JAVA_HOME) {
  throw "JAVA_HOME is required for Maven Wrapper. Install JDK 21+ or set JAVA_HOME."
}

Invoke-Checked { .\mvnw.cmd -B -pl services/core-backend -am -DskipTests=false verify } "Maven backend verify"
Invoke-Checked { npm run format:check } "Repository formatting"
Invoke-Checked { npm run lint } "Node lint"
Invoke-Checked { npm run typecheck --if-present } "Node typecheck"
Invoke-Checked { npm run test } "Node tests"
Invoke-Checked { npm run build:frontends } "Frontend production builds"
Invoke-Checked { python -m ruff check . } "Python lint"
Invoke-Checked { python -m ruff format --check . } "Python formatting"
Invoke-Checked { python -m pytest tests\foundation } "Repository foundation tests"

$pythonServices = @(
  "services/ai-inference-service",
  "services/rag-service",
  "services/agent-orchestrator",
  "services/reporting-service",
  "services/notification-service"
)

foreach ($service in $pythonServices) {
  Push-Location $service
  try {
    Invoke-Checked { python -m pip install -e ".[dev]" } "$service install"
    Invoke-Checked { python -m pytest } "$service tests"
  } finally {
    Pop-Location
  }
}

Invoke-Checked { docker compose config --quiet } "Docker Compose configuration"
