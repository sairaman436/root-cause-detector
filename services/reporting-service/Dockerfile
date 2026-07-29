# Purpose: Builds and runs the Python reporting service shell.
# Why it exists: Provides an independently deployable runtime with structured logging and health checks before report workflows exist.
# Architecture fit: Supports separate scaling and operations for asynchronous reporting workloads.
FROM python:3.11-slim

WORKDIR /app
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
COPY pyproject.toml ./
COPY src ./src
RUN python -m pip install --no-cache-dir --upgrade pip && python -m pip install --no-cache-dir .

EXPOSE 8104
CMD ["uvicorn", "reporting_service.main:app", "--host", "0.0.0.0", "--port", "8104"]
