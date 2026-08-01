# Purpose: Defines production security controls and operational security procedures.

# Why it exists: Gives security engineers a source-controlled control map for DevSecOps and runtime governance.

# Architecture fit: Supports Milestone 11 security, supply chain, Vault, TLS, mTLS, and compliance requirements.

# Security Guide

## Runtime Controls

- TLS terminates at ingress and reverse proxy.
- mTLS is prepared through service mesh compatible Kubernetes boundaries.
- Network policies default-deny traffic and explicitly allow approved paths.
- Workloads use non-root containers, no privilege escalation, and resource limits.

## Supply Chain Controls

- TruffleHog scans secrets.
- Trivy scans container images.
- Anchore generates SBOM and performs license policy scans.
- Build provenance attestations are generated for published images.

## AI Security Controls

- Prompt and model artifacts require registry metadata, safety review, and rollback version.
- Prompt injection regression tests are mandatory before production promotion.
- Decision outputs remain explainable and traceable through decision traces.
