# Purpose: Builds and runs the Python agent orchestrator service shell.
# Why it exists: Reserves a deployable runtime with structured logging and health checks before agents are implemented.
# Architecture fit: Supports controlled agent execution, safety policies, and human approval in later milestones.
FROM python:3.11-slim

WORKDIR /app
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
COPY pyproject.toml ./
COPY src ./src
RUN python -m pip install --no-cache-dir --upgrade pip && python -m pip install --no-cache-dir .

EXPOSE 8103
CMD ["uvicorn", "agent_orchestrator.main:app", "--host", "0.0.0.0", "--port", "8103"]
