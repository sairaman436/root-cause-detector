# Purpose: Builds and runs the Python AI inference service.
# Why it exists: Hosts the provider-neutral local LLM boundary, structured output validation, health checks, and streaming APIs for Ollama/Qwen integration.
# Architecture fit: Supports independently scalable AI inference deployment in Docker Compose and Kubernetes while keeping model providers outside backend business logic.
FROM python:3.11-slim

WORKDIR /app
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
COPY pyproject.toml ./
COPY src ./src
RUN python -m pip install --no-cache-dir --upgrade pip && python -m pip install --no-cache-dir .

EXPOSE 8101
CMD ["uvicorn", "ai_inference_service.main:app", "--host", "0.0.0.0", "--port", "8101"]
