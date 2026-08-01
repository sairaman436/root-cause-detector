# Purpose: Defines logging module inputs.
# Why it exists: Keeps retention and archive settings explicit.
# Architecture fit: Supports Milestone 11 centralized logging.
variable "name" { type = string }
variable "retention_days" { type = number }
variable "archive_bucket" { type = string }
