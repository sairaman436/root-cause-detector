# Purpose: Defines database module inputs.
# Why it exists: Keeps storage, retention, and topology explicit.
# Architecture fit: Supports Milestone 11 database HA and backup strategy.
variable "name" { type = string }
variable "subnet_ids" { type = list(string) }
variable "vpc_id" { type = string }
variable "database_name" { type = string }
variable "backup_window" { type = string }
variable "retention_days" { type = number }
variable "multi_az" { type = bool }
variable "deletion_protection" { type = bool }
