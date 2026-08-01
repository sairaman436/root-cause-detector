# Purpose: Defines object storage module inputs.
# Why it exists: Keeps retention controls explicit.
# Architecture fit: Supports Milestone 11 storage governance.
variable "name" { type = string }
variable "backup_retention_days" { type = number }
