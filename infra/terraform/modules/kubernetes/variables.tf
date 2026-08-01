# Purpose: Defines Kubernetes module inputs.
# Why it exists: Keeps cluster version, IAM, and networking explicit.
# Architecture fit: Supports Milestone 11 Kubernetes architecture.
variable "name" {
  type        = string
  description = "Cluster name."
}

variable "subnet_ids" {
  type        = list(string)
  description = "Private subnet IDs."
}

variable "cluster_role_arn" {
  type        = string
  description = "Cluster IAM role ARN."
}

variable "node_role_arn" {
  type        = string
  description = "Node IAM role ARN."
}

variable "kubernetes_version" {
  type        = string
  description = "Kubernetes version."
}
