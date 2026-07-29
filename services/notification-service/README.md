# Notification Service

## Purpose

Defines the future notification service boundary for email, SMS, push, and workflow alerts.

## Why It Exists

Notification delivery requires retry, provider isolation, auditability, and operational monitoring independent of core workflows.

## Architecture Fit

This service will later consume platform events and deliver auditable notifications. Milestone 1 reserves the boundary only.
