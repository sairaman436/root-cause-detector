"""Purpose: Verifies that Milestone 1 foundation files exist.

Why it exists: Gives the repository an executable Python smoke test
before business tests are introduced.
Architecture fit: Protects the approved monorepo bootstrap contract across CI and local development.
"""

from pathlib import Path


def test_required_foundation_files_exist() -> None:
    repo_root = Path(__file__).resolve().parents[2]
    required_paths = [
        "pom.xml",
        "package.json",
        "pyproject.toml",
        "docker-compose.yml",
        "services/core-backend/pom.xml",
        "apps/web-portal/package.json",
        "services/ai-inference-service/pyproject.toml",
        "services/rag-service/pyproject.toml",
        "services/agent-orchestrator/pyproject.toml",
        "docs/engineering/DEVELOPER_WORKFLOW.md",
    ]

    missing = [path for path in required_paths if not (repo_root / path).exists()]

    assert missing == []
