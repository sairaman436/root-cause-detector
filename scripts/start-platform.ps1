# Purpose: Starts the complete local platform and waits for its public health checks.
# Why it exists: Developers should be able to start the stack with one repeatable command.
# Architecture fit: Uses the existing Docker Compose topology without changing service behavior.

[CmdletBinding()]
param(
    [switch]$NoBuild,
    [switch]$NoBrowser,
    [int]$TimeoutSeconds = 180
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw 'Docker is not installed or is not available on PATH.'
}

docker info *> $null
if ($LASTEXITCODE -ne 0) {
    throw 'Docker Desktop is not running. Start Docker Desktop and run this script again.'
}

Write-Host 'Starting the Rural Intelligence platform...' -ForegroundColor Cyan
if ($NoBuild) {
    docker compose up -d
} else {
    docker compose up -d --build
}
if ($LASTEXITCODE -ne 0) {
    throw 'Docker Compose could not start the platform.'
}

$healthChecks = @(
    @{ Name = 'Web portal'; Url = 'http://localhost:3000/api/health' },
    @{ Name = 'Core backend'; Url = 'http://localhost:8080/actuator/health' },
    @{ Name = 'AI service'; Url = 'http://localhost:8101/health/ready' },
    @{ Name = 'RAG service'; Url = 'http://localhost:8102/health/ready' }
)

$deadline = (Get-Date).AddSeconds($TimeoutSeconds)
foreach ($check in $healthChecks) {
    $healthy = $false
    while ((Get-Date) -lt $deadline) {
        try {
            $response = Invoke-WebRequest -Uri $check.Url -UseBasicParsing -TimeoutSec 5
            if ($response.StatusCode -eq 200) {
                $healthy = $true
                break
            }
        } catch {
            Start-Sleep -Seconds 2
        }
    }
    if (-not $healthy) {
        throw "$($check.Name) did not become healthy within $TimeoutSeconds seconds. Check: $($check.Url)"
    }
    Write-Host "  [OK] $($check.Name)" -ForegroundColor Green
}

Write-Host ''
Write-Host 'Platform is ready:' -ForegroundColor Green
Write-Host '  Portal:  http://localhost:3000'
Write-Host '  Backend: http://localhost:8080/actuator/health'
Write-Host '  AI:      http://localhost:8101/health/ready'
Write-Host '  RAG:     http://localhost:8102/health/ready'

if (-not $NoBrowser) {
    Start-Process 'http://localhost:3000'
}
