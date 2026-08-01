# Deployment

## Purpose

Contains Kubernetes deployment manifests, Helm chart boundaries, environment overlays, ingress, config maps, and secret templates.

## Why It Exists

Application deployment must be separated from infrastructure provisioning and from service source code.

## Architecture Fit

This directory implements the approved containerized deployment strategy.

## Production Assets

- `kubernetes/base`: reusable Kubernetes manifests for namespace, workloads, services, ingress, HPA, network policy, jobs, CronJobs, and storage claims.
- `kubernetes/overlays/production`: production Kustomize overlay for image promotion.
- `observability`: Prometheus, Grafana, Loki, Tempo, AlertManager, and OpenTelemetry configuration.
- `reverse-proxy`: production rehearsal reverse proxy configuration.
