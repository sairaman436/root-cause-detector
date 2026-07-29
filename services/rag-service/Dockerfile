# Purpose: Builds and runs the Python RAG service shell.
# Why it exists: Establishes deployment packaging, structured logging, and health checks before retrieval implementation.
# Architecture fit: Supports independent scaling and governance for the knowledge retrieval subsystem.
FROM python:3.11-slim

WORKDIR /app
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
COPY pyproject.toml ./
COPY src ./src
RUN python -m pip install --no-cache-dir --upgrade pip && python -m pip install --no-cache-dir .

EXPOSE 8102
CMD ["uvicorn", "rag_service.main:app", "--host", "0.0.0.0", "--port", "8102"]
