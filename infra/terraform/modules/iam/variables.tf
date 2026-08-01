# Purpose: Defines IAM module inputs.
# Why it exists: Keeps role naming deterministic across environments.
# Architecture fit: Supports Milestone 11 IAM governance.
variable "name" {
  type        = string
  description = "Deployment name."
}
