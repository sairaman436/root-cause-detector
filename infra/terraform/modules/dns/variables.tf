# Purpose: Defines DNS module inputs.
# Why it exists: Keeps platform DNS target explicit.
# Architecture fit: Supports Milestone 11 global routing.
variable "domain_name" { type = string }
variable "target_name" { type = string }
variable "target_zone" { type = string }
