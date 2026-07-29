# 💻 Coding Standards

Version: 1.0

Project: AI Rural Root Cause Discovery System

---

# Purpose

This document defines the coding standards that all contributors must follow.

The objective is to produce code that is:

- Readable
- Maintainable
- Consistent
- Secure
- Testable
- Scalable

These standards apply to frontend, backend, AI modules, scripts, and supporting utilities.

---

# General Principles

Every line of code should prioritize:

- Clarity over cleverness
- Simplicity over unnecessary complexity
- Maintainability over short-term convenience
- Explicit behavior over hidden logic
- Consistency across the codebase

---

# Project Structure

Source code should be organized by responsibility rather than file type.

Example:

src/
├── frontend/
├── backend/
├── ai/
├── database/
├── api/
├── services/
├── models/
├── utils/
├── config/
└── tests/

Each module should have a single, clearly defined purpose.

---

# Naming Conventions

## Variables

Use descriptive names.

Good:

surveyCount
clusterResult
rootCausePrediction

Avoid:

a
temp
value1
xyz

---

## Functions

Function names should describe actions.

Examples:

calculateSimilarity()

generateRecommendation()

verifyImageEvidence()

clusterComplaints()

---

## Classes

Use PascalCase.

Examples:

SurveyService

ComplaintAnalyzer

RecommendationEngine

ImageVerifier

---

## Constants

Use uppercase with underscores.

Examples:

MAX_IMAGE_SIZE

DEFAULT_RADIUS

API_TIMEOUT

---

## File Naming

Use consistent file names.

Examples:

survey_service.py

recommendation_engine.py

gps_cluster.js

image_verifier.ts

Avoid ambiguous names such as:

new.py

test2.js

sampleFinal.js

---

# Function Design

Functions should:

- Perform one responsibility.
- Be short and focused.
- Return predictable outputs.
- Avoid hidden side effects.

Prefer:

Small reusable functions

instead of

Large monolithic functions.

---

# Code Formatting

Maintain consistent formatting throughout the project.

Requirements:

- Consistent indentation
- Logical spacing
- Meaningful line breaks
- Organized imports
- No trailing whitespace

Formatting tools should be used where applicable.

---

# Comments

Write comments to explain **why**, not **what**.

Good:

// GPS radius chosen based on field survey observations.

Avoid:

// Increment i by 1.

Code should be self-explanatory whenever possible.

---

# Error Handling

Do not silently ignore errors.

Every exception should:

- Be logged.
- Provide meaningful messages.
- Avoid exposing sensitive information.
- Support debugging.

Example:

Instead of:

catch(Exception){}

Use:

Catch the exception, log it, and provide an appropriate response.

---

# Logging

Use structured logging.

Include:

- Timestamp
- Module
- Severity
- Message
- Request ID (where applicable)

Do not log:

- Passwords
- API keys
- Personal data
- Authentication tokens

---

# Input Validation

Validate all external input.

Examples:

- Survey responses
- Uploaded images
- API requests
- GPS coordinates
- Form submissions

Never trust client-side validation alone.

---

# Security

Follow secure coding practices.

Requirements:

- Sanitize user input.
- Validate uploaded files.
- Use parameterized database queries.
- Protect secrets using environment variables.
- Enforce authentication and authorization.
- Use HTTPS in production.

---

# Database Standards

Database operations should:

- Use transactions when appropriate.
- Avoid duplicate data.
- Enforce foreign key relationships.
- Use meaningful table names.
- Prefer indexed queries for performance.

---

# API Standards

APIs should:

- Follow REST principles.
- Return consistent response formats.
- Use proper HTTP status codes.
- Include meaningful error messages.
- Validate all incoming data.

---

# Testing Expectations

Every major feature should include:

- Unit tests
- Integration tests (where applicable)
- Edge case validation
- Error condition testing

Untested code should not be considered production-ready.

---

# Code Reviews

Every pull request should verify:

- Readability
- Correctness
- Security
- Performance
- Documentation updates
- Compliance with coding standards

Constructive feedback should focus on improving the code, not criticizing the contributor.

---

# Technical Debt

Technical debt should be:

- Identified
- Documented
- Prioritized
- Addressed incrementally

Short-term workarounds must not become permanent solutions without review.

---

# Definition of Clean Code

Code is considered clean when it is:

- Easy to understand
- Easy to modify
- Easy to test
- Properly documented
- Free of unnecessary complexity

---

# Final Principle

Every contributor leaves the codebase in a better state than they found it.

Small, continuous improvements lead to a high-quality engineering system.