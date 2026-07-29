# Purpose: Builds and runs the Python notification service shell.
# Why it exists: Provides an independently deployable runtime with structured logging and health checks before delivery workflows exist.
# Architecture fit: Supports separate scaling and provider isolation for notifications.
FROM python:3.11-slim

WORKDIR /app
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
COPY pyproject.toml ./
COPY src ./src
RUN python -m pip install --no-cache-dir --upgrade pip && python -m pip install --no-cache-dir .

EXPOSE 8105
CMD ["uvicorn", "notification_service.main:app", "--host", "0.0.0.0", "--port", "8105"]
