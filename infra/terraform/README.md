# Terraform

## Purpose

Defines the future Infrastructure as Code root for cloud resources.

## Why It Exists

The platform requires reproducible provisioning for networks, Kubernetes, databases, cache, messaging, object storage, vector storage, monitoring, and IAM.

## Architecture Fit

This directory implements the approved Terraform layout. Milestone 11 adds production module definitions and an environment composition for cloud provisioning.

## Production Modules

- `environments/production`: primary production composition.
- `modules/network`: VPC and subnets.
- `modules/iam`: cluster and node IAM roles.
- `modules/kubernetes`: managed Kubernetes cluster and node group.
- `modules/database`: managed PostgreSQL with backup controls.
- `modules/redis`: managed Redis replication group.
- `modules/object-storage`: evidence, model, and audit buckets.
- `modules/load-balancer`: public load balancer.
- `modules/dns`: production DNS alias.
- `modules/monitoring`: managed monitoring integration points.
- `modules/logging`: centralized log retention.
