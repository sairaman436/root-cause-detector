# Purpose: Defines network module inputs.
# Why it exists: Keeps network topology explicit and reusable.
# Architecture fit: Supports Milestone 11 infrastructure as code.
variable "name" {
  type        = string
  description = "Deployment name."
}

variable "cidr_block" {
  type        = string
  description = "VPC CIDR."
}

variable "availability_zones" {
  type        = list(string)
  description = "Availability zones."
}

variable "public_subnet_cidrs" {
  type        = list(string)
  description = "Public subnet CIDRs."
}

variable "private_subnet_cidrs" {
  type        = list(string)
  description = "Private subnet CIDRs."
}
