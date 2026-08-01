"""
Purpose: Validates Milestone 11 production hardening assets exist and remain wired.
Why it exists: Gives CI a fast regression check for Kubernetes, Terraform, MLOps, observability, and runbook coverage.
Architecture fit: Supports enterprise readiness gates without requiring cloud credentials.
"""
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]


def test_production_hardening_assets_exist() -> None:
    required_paths = [
        "docker-compose.prod.yml",
        ".github/workflows/production-hardening.yml",
        "deploy/kubernetes/base/kustomization.yaml",
        "deploy/kubernetes/overlays/production/kustomization.yaml",
        "deploy/observability/prometheus/prometheus.yml",
        "deploy/observability/grafana/dashboards/platform-overview.json",
        "infra/terraform/environments/production/main.tf",
        "infra/terraform/modules/network/main.tf",
        "infra/terraform/modules/kubernetes/main.tf",
        "infra/terraform/modules/database/main.tf",
        "ml-platform/model-registry/model-registry.yaml",
        "ml-platform/drift-monitoring/drift-policy.yaml",
        "docs/operations/ENTERPRISE_READINESS_CHECKLIST.md",
        "docs/operations/DISASTER_RECOVERY_GUIDE.md",
        "tests/performance/k6-smoke.js",
        "tests/chaos/chaos-test-plan.md",
    ]

    missing = [path for path in required_paths if not (ROOT / path).exists()]
    assert missing == []


def test_kubernetes_base_references_required_manifest_types() -> None:
    kustomization = (ROOT / "deploy/kubernetes/base/kustomization.yaml").read_text(encoding="utf-8")
    for manifest in [
        "namespace.yaml",
        "configmap.yaml",
        "secret-template.yaml",
        "core-backend.yaml",
        "ingress.yaml",
        "hpa.yaml",
        "network-policy.yaml",
        "jobs.yaml",
        "cronjobs.yaml",
    ]:
        assert manifest in kustomization


def test_operations_migration_adds_platform_permissions() -> None:
    migration = (
        ROOT
        / "services/core-backend/src/main/resources/db/migration/V10__enterprise_production_hardening_operations.sql"
    ).read_text(encoding="utf-8")
    assert "operations.operational_dashboards" in migration
    assert "operations.backup_reports" in migration
    assert "PLATFORM_READ" in migration
    assert "PLATFORM_ADMIN" in migration
