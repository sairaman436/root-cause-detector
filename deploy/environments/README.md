# Deployment Environments

## Purpose

Defines deployment overlays for Kubernetes and runtime configuration.

## Why It Exists

Application deployment settings vary by environment without changing service source code. This directory keeps those overlays separate from reusable Helm charts and base manifests.

## Architecture Fit

Environment overlays bind container images, resource profiles, autoscaling rules, config maps, service routing, and observability annotations for each deployment target.

## Implementation Notes

Never store secrets here. Use references to the approved secrets provider and keep production changes behind CI/CD approval gates.
