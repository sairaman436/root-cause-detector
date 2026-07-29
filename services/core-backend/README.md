# Core Backend

## Purpose

Hosts the Spring Boot modular-monolith foundation for enterprise workflows.

## Why It Exists

The approved architecture assigns transactional platform responsibilities to a Java 21 Spring Boot backend while keeping AI, RAG, and agents in Python services.

## Architecture Fit

This module owns transactional platform responsibilities that belong in the Java backend. Milestone 2 adds the Identity & Access Management boundary while surveys, evidence metadata, recommendations, reports, AI, RAG, and agent workflows remain intentionally deferred.

## Current Scope

- Maven module configuration
- Spring Boot web, validation, security, JPA, Flyway, Actuator, and OpenAPI dependencies
- Actuator readiness for future health checks
- Structured logging configuration
- Identity REST controllers for registration, login, logout, refresh, current user, and admin identity catalogs
- JWT access tokens and hashed refresh-token persistence
- Organization, user, role, permission, and audit persistence through Flyway-managed PostgreSQL schemas
- Unit and integration tests for token behavior, authentication flows, RBAC, and schema startup

## Explicit Non-Scope

- Survey modules
- AI, RAG, and agent orchestration
- Kafka workflows
- Reporting and notification business features
