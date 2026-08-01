# Purpose: Defines production performance targets and benchmark evidence expectations.

# Why it exists: Provides baseline thresholds for load, stress, and regression testing.

# Architecture fit: Supports Milestone 11 performance reports, dashboards, and readiness review.

# Performance Report

## Initial Targets

- Non-AI API p95 latency: under 500 ms at 100 requests per second.
- AI decision analysis p95 latency: under 15 seconds at 10 concurrent requests.
- Error rate: under 1 percent during expected load.
- Database connection pool saturation: under 80 percent.

## Required Benchmarks

- Load test: expected production traffic for 60 minutes.
- Stress test: 2x expected traffic for 15 minutes.
- Soak test: expected traffic for 8 hours.
- Recovery test: restart one replica during load and verify no user-visible outage.
