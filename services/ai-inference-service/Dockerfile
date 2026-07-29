# Purpose: Builds and runs the Python AI inference service shell.
# Why it exists: Establishes runtime conventions, structured logging, and health checks before model workflows exist.
# Architecture fit: Supports independently scalable AI inference deployment in Kubernetes.
FROM python:3.11-slim

WORKDIR /app
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
COPY pyproject.toml ./
COPY src ./src
RUN python -m pip install --no-cache-dir --upgrade pip && python -m pip install --no-cache-dir .

EXPOSE 8101
CMD ["uvicorn", "ai_inference_service.main:app", "--host", "0.0.0.0", "--port", "8101"]
