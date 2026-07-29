# UML_Standards.md

> **Version:** 1.0
> **Status:** Active
> **Owner:** System Design Team
> **Applies To:** All UML diagrams created within the project.

---

# Purpose

This document defines the standards, conventions, and best practices for creating Unified Modeling Language (UML) diagrams within the project.

The objectives are to:

- Standardize UML usage
- Improve readability
- Ensure consistency
- Support implementation
- Maintain traceability
- Reduce ambiguity

This document serves as the authoritative UML modeling standard.

---

# Table of Contents

1. UML Philosophy
2. Modeling Principles
3. Supported Diagram Types
4. General Modeling Standards
5. Diagram Layout Standards
6. Naming Standards
7. Relationship Standards
8. Diagram-Specific Standards
9. Tool Standards
10. Documentation Standards
11. Traceability
12. Common Anti-Patterns
13. Review Checklist

---

# UML Philosophy

UML diagrams are intended to communicate design decisions—not replace implementation.

Every diagram should answer one or more of the following:

- What components exist?
- How do they interact?
- How does data flow?
- What is the execution sequence?
- What are the system states?
- How are responsibilities distributed?

A diagram should simplify understanding, not increase complexity.

---

# Modeling Principles

All UML diagrams should follow these principles:

- Simplicity over completeness
- High readability
- Consistent notation
- Single responsibility per diagram
- Traceability to architecture
- Maintainability
- Implementation alignment

Avoid diagrams that attempt to explain the entire system at once.

---

# Supported UML Diagram Types

| Diagram | Purpose |
|----------|----------|
| Class Diagram | Static object structure |
| Sequence Diagram | Runtime interaction flow |
| Activity Diagram | Business and system workflows |
| State Diagram | Object lifecycle and state transitions |
| Component Diagram | Component relationships |
| Package Diagram | Logical organization |
| Deployment Diagram | Infrastructure mapping (reference only) |

---

# General Modeling Standards

Every UML diagram must include:

- Title
- Version
- Description
- Author
- Last Updated
- Related Requirements
- Related Architecture Documents

---

# Diagram Layout Standards

Arrange diagrams using a logical flow.

Preferred direction:

```
Left → Right
```

or

```
Top
↓

Bottom
```

Avoid:

- Crossing connectors
- Circular layouts
- Unnecessary diagonal relationships
- Excessive spacing
- Dense clusters

Maintain consistent spacing throughout the diagram.

---

# Naming Standards

Use PascalCase for:

- Classes
- Interfaces
- Components
- Packages

Examples

```
CitizenService

ComplaintRepository

RecommendationEngine

SurveyValidator
```

Methods

Use camelCase

```
validateSurvey()

generateRecommendation()

saveComplaint()

getVillageStatistics()
```

Attributes

Use camelCase

```
surveyId

villageName

confidenceScore

createdAt
```

Constants

Use UPPER_SNAKE_CASE

```
MAX_RETRY_COUNT

DEFAULT_TIMEOUT
```

---

# Relationship Standards

## Association

Use when one object knows another.

---

## Dependency

Use when one object temporarily depends on another.

---

## Aggregation

Use when objects have a "has-a" relationship without ownership.

---

## Composition

Use when lifecycle depends on the parent.

---

## Inheritance

Use only for genuine "is-a" relationships.

Avoid deep inheritance hierarchies.

---

## Interface Realization

Always model interfaces separately from implementations.

---

# Visibility Standards

| Symbol | Meaning |
|---------|----------|
| + | Public |
| - | Private |
| # | Protected |
| ~ | Package |

Default visibility should be **private** unless broader access is required.

---

# Class Diagram Standards

Every class should include:

- Name
- Attributes
- Operations

Optional

- Stereotypes
- Constraints
- Notes

Keep related classes together.

Avoid more than 25 classes in a single diagram.

---

# Sequence Diagram Standards

Participants should be ordered from left to right according to interaction flow.

Use:

- Activation bars
- Return messages (where meaningful)
- Guards
- Loops
- Alternatives

Label important business operations.

---

# Activity Diagram Standards

Activities should have:

- Initial node
- Final node
- Decision nodes
- Merge nodes
- Forks
- Joins

Clearly label every transition.

---

# State Diagram Standards

Every state diagram should contain:

- Initial state
- Intermediate states
- Final state
- Valid transitions
- Trigger events

Avoid unreachable states.

---

# Component Diagram Standards

Every component should expose:

- Responsibilities
- Interfaces
- Dependencies

Represent only architectural dependencies.

Do not include implementation-level details.

---

# Package Diagram Standards

Organize packages according to bounded contexts or functional domains.

Avoid circular package dependencies.

---

# Notes and Constraints

Use notes only when additional explanation is required.

Avoid embedding large blocks of text directly in diagrams.

---

# Color Usage

Use color sparingly.

Recommended usage:

- Blue → Services
- Green → Databases
- Yellow → External Systems
- Gray → Infrastructure

Ensure diagrams remain understandable in grayscale or black-and-white print.

---

# Tool Standards

Approved tools include:

- Mermaid
- PlantUML
- Draw.io (diagrams.net)
- Visual Paradigm
- StarUML

Generated diagrams should be version-controlled.

Source files should be committed alongside exported images.

---

# Documentation Standards

Each UML diagram should include:

- Purpose
- Scope
- Assumptions
- Dependencies
- Related Documents
- Revision History

---

# Traceability

Each diagram should reference:

- Requirement IDs
- Architecture Components
- ADRs
- Design Documents
- API Specifications

Example

```
Requirements

FR-001

FR-004

NFR-003

Related Architecture

API Architecture

Backend Design

Authentication Design
```

---

# Common Anti-Patterns

Avoid:

- God Class diagrams
- Excessive inheritance
- Circular dependencies
- Duplicate entities
- Unlabeled relationships
- Crossing connectors
- Missing visibility
- Missing multiplicities
- Mixed abstraction levels
- Diagram clutter

---

# Review Checklist

## General

- [ ] Purpose Defined
- [ ] Scope Defined
- [ ] Diagram Title Present

## Modeling

- [ ] Correct UML Notation
- [ ] Relationships Accurate
- [ ] Naming Standards Followed
- [ ] Visibility Defined

## Quality

- [ ] Readable Layout
- [ ] Minimal Crossing Lines
- [ ] Consistent Spacing
- [ ] Appropriate Level of Detail

## Traceability

- [ ] Requirement References Included
- [ ] Architecture References Added
- [ ] Related Design Documents Linked

## Documentation

- [ ] Version Updated
- [ ] Review Completed
- [ ] Source Diagram Stored

---

# Guiding Principle

> **UML diagrams are communication tools that bridge architecture and implementation. Every diagram should be clear, consistent, traceable, and focused on a single design concern, enabling engineers to understand, review, and implement the system with confidence.**