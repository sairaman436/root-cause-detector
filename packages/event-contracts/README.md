# Event Contracts

## Purpose

Stores canonical event envelopes, topic contracts, and schema documentation.

## Why It Exists

Kafka producers and consumers need versioned contracts to prevent schema drift.

## Architecture Fit

This package supports the approved event-driven architecture and outbox/idempotency strategy.
