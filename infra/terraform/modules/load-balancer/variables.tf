# Purpose: Defines load balancer module inputs.
# Why it exists: Keeps public entry point dependencies explicit.
# Architecture fit: Supports Milestone 11 API gateway and load balancing.
variable "name" { type = string }
variable "vpc_id" { type = string }
variable "public_subnet_ids" { type = list(string) }
