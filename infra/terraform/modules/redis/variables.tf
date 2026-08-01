# Purpose: Defines Redis module inputs.
# Why it exists: Keeps cache networking explicit.
# Architecture fit: Supports Milestone 11 performance and HA.
variable "name" { type = string }
variable "subnet_ids" { type = list(string) }
variable "vpc_id" { type = string }
