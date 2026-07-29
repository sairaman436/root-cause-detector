# Terraform Provisioning Guide

> **Document Version:** 1.0  
> **Status:** Approved  
> **Owner:** Infrastructure as Code (IaC) Engineering Team  
> **Project:** AI Rural Root Cause Discovery System  
> **Document Type:** Infrastructure Provisioning Architecture Guide  

---

# Terraform Provisioning Guide

---

# Document Information

| Field | Value |
|---------|---------|
| Document Name | Terraform Provisioning Guide |
| Domain | Infrastructure Engineering |
| Version | 1.0 |
| Status | Approved |
| Owner | Infrastructure Team |
| Created Date | 2026-07-28 |
| Last Updated | 2026-07-28 |

---

# Purpose

This document provides the operational guide and module structure for provisioning cloud infrastructure (VPCs, EKS clusters, PostgreSQL RDS, Redis ElastiCache, S3 buckets, IAM roles) via Terraform for the AI Rural Root Cause Discovery System.

---

# Terraform Module Topology

```text
terraform/
├── main.tf                  # Main Entry point & Provider definitions
├── variables.tf             # Input Variable Schemas
├── outputs.tf               # Infrastructure Output Resource IDs
├── modules/
│   ├── vpc/                 # Multi-AZ VPC, Subnets, Route Tables
│   ├── eks/                 # Amazon EKS Kubernetes Worker Cluster & Node Groups
│   ├── rds/                 # PostgreSQL Multi-AZ Database & Security Groups
│   ├── redis/               # ElastiCache Redis Feature Store Cluster
│   └── s3/                  # MinIO / S3 Encrypted Storage Buckets & Policies
└── environments/
    ├── dev/                 # Development Environment TFVars
    ├── staging/             # Staging Environment TFVars
    └── prod/                # Production Multi-Region TFVars
```

---

# Mandatory Command Execution Sequence

```bash
# Initialize Remote S3 State & DynamoDB Locks
terraform init -backend-config="environments/prod/backend.hcl"

# Validate HCL Code Formatting & Syntax
terraform fmt -check
terraform validate

# Generate Execution Plan
terraform plan -var-file="environments/prod/terraform.tfvars" -out=tfplan

# Apply Infrastructure Plan (Requires CCB Approval Token)
terraform apply tfplan
```

---

# Security & State Management Rules

1. **Remote State Locking**: Terraform state files **MUST** be stored in encrypted S3 buckets (`aws_s3_bucket.terraform_state`) with state locking enforced via AWS DynamoDB (`aws_dynamodb_table.tf_locks`). Local state files are **PROHIBITED**.
2. **KMS State Encryption**: State files **MUST** be encrypted at rest using a dedicated AWS KMS Customer Managed Key.

---

# Approval

| Role | Name | Date |
|------|------|------|
| Infrastructure Lead | Robert Sterling | 2026-07-28 |
| Lead DevOps Architect | Samantha Chen | 2026-07-28 |
| Solution Architect | Marcus Vance | 2026-07-28 |

---

# Revision History

| Version | Date | Description | Author |
|----------|------|-------------|--------|
| 1.0 | 2026-07-28 | Initial Release of Terraform Provisioning Guide | Infrastructure Team |

---

# End of Document
