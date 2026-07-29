# Entity_Template.md

> **Template Version:** 1.0
> **Status:** Approved
> **Owner:** Database Engineering Team
> **Project:** AI Rural Root Cause Discovery System
> **Template Type:** JPA Entity Template

---

# Entity Template

---

# Template Information

| Field | Value |
|---------|---------|
| Entity Name | |
| Database Table | |
| Module | |
| Package | |
| Version | |
| Status | Draft / Review / Approved |
| Author | |
| Created Date | |
| Last Updated | |

---

# Purpose

Describe the purpose of this entity.

Example

> Represents a rural household survey submitted by a field officer.

---

# Business Context

Describe

- Business capability
- Domain responsibility
- Related business processes

---

# Entity Metadata

| Property | Value |
|----------|-------|
| Table Name | |
| Schema | public |
| Primary Key | UUID |
| Auditable | Yes / No |
| Soft Delete | Yes / No |

---

# Entity Definition

Example

```java
@Entity
@Table(name = "")
public class EntityName {

}
```

---

# Fields

| Field | Type | Nullable | Default | Description |
|---------|------|----------|----------|-------------|
| | | | | |

---

# Validation Constraints

| Field | Annotation | Description |
|---------|------------|-------------|
| | | |

Examples

- @NotNull
- @NotBlank
- @Email
- @Size
- @Pattern
- @Min
- @Max

---

# Primary Key

Type

- UUID

Generation Strategy

```java
@GeneratedValue
```

Describe

- Key generation strategy
- Identifier uniqueness
- Lifecycle

---

# Relationships

| Relationship | Target Entity | Type |
|--------------|---------------|------|
| | | OneToOne |
| | | OneToMany |
| | | ManyToOne |
| | | ManyToMany |

Example

```java
@OneToMany(mappedBy = "")
private List<ChildEntity> children;
```

---

# Cascade Strategy

Applicable Cascades

- PERSIST
- MERGE
- REMOVE
- REFRESH
- DETACH

Document the rationale for each cascade configuration.

---

# Fetch Strategy

Specify

- LAZY (preferred)
- EAGER (exception only)

Justification

-

---

# Constraints

Database Constraints

- NOT NULL
- UNIQUE
- CHECK
- FOREIGN KEY

Document all business constraints.

---

# Indexes

| Index Name | Columns | Purpose |
|------------|----------|----------|
| | | |

Example

```java
@Table(indexes = {
    @Index(name="", columnList="")
})
```

---

# Auditing

Audit Fields

```text
created_at

created_by

updated_at

updated_by
```

Framework

- Spring Data JPA Auditing

---

# Soft Delete

Enabled

Yes / No

Implementation

```text
is_deleted

deleted_at

deleted_by
```

Query Filtering

-

---

# Serialization

Ignore Fields

-

-

Use

```java
@JsonIgnore
```

Prevent circular references using

- @JsonManagedReference
- @JsonBackReference
- @JsonIdentityInfo

---

# Lifecycle Callbacks

Applicable Callbacks

- @PrePersist
- @PostPersist
- @PreUpdate
- @PostUpdate
- @PreRemove
- @PostLoad

Purpose

-

---

# Business Rules

Rule 1

-

Rule 2

-

Rule 3

-

---

# Security

Sensitive Fields

-

Encrypted Fields

-

Masked Fields

-

Do not expose sensitive entity fields directly through APIs.

---

# Repository Usage

Repository

-

Custom Queries

-

Specifications

-

---

# Performance Considerations

Evaluate

- Relationship loading
- Query efficiency
- Index usage
- Object size
- Batch fetching

Avoid

- N+1 queries
- Excessive eager loading

---

# Testing

Unit Tests

-

Persistence Tests

-

Repository Tests

-

Relationship Tests

-

Constraint Validation

-

---

# Migration Dependencies

Migration Script

-

Required Tables

-

Foreign Keys

-

---

# Monitoring

Monitor

- Query performance
- Relationship loading
- Constraint violations
- Entity growth

---

# Risks

| Risk | Mitigation |
|------|------------|
| N+1 queries | Fetch optimization |
| Missing indexes | Index review |
| Circular references | Serialization annotations |
| Invalid data | Validation constraints |

---

# Assumptions

-

-

-

---

# Open Issues

| ID | Description | Owner |
|----|-------------|-------|
| | | |

---

# References

- Database Implementation Standards
- Backend Implementation Standards
- Coding Standards
- JPA Documentation
- Hibernate Documentation
- Architecture Decision Records (ADRs)

---

# Approval

| Role | Name | Date |
|------|------|------|
| Backend Developer | | |
| Database Engineer | | |
| Technical Lead | | |
| Architect | | |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | YYYY-MM-DD | Initial Template | Database Engineering Team |