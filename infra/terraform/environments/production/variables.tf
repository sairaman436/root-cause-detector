# Purpose: Defines production infrastructure inputs.
# Why it exists: Keeps region, network, retention, and DNS choices explicit per environment.
# Architecture fit: Supports Milestone 11 environment-specific IaC.
variable "name" {
  description = "Platform deployment name."
  type        = string
  default     = "airural-platform"
}

variable "region" {
  description = "Primary cloud region."
  type        = string
  default     = "ap-south-1"
}

variable "cidr_block" {
  description = "VPC CIDR block."
  type        = string
  default     = "10.40.0.0/16"
}

variable "availability_zones" {
  description = "Availability zones for high availability."
  type        = list(string)
  default     = ["ap-south-1a", "ap-south-1b", "ap-south-1c"]
}

variable "public_subnet_cidrs" {
  description = "Public subnet CIDRs."
  type        = list(string)
  default     = ["10.40.0.0/20", "10.40.16.0/20", "10.40.32.0/20"]
}

variable "private_subnet_cidrs" {
  description = "Private subnet CIDRs."
  type        = list(string)
  default     = ["10.40.64.0/20", "10.40.80.0/20", "10.40.96.0/20"]
}

variable "kubernetes_version" {
  description = "Managed Kubernetes control plane version."
  type        = string
  default     = "1.30"
}

variable "domain_name" {
  description = "Production DNS name."
  type        = string
  default     = "platform.example.gov"
}

variable "backup_window" {
  description = "Managed database backup window."
  type        = string
  default     = "18:00-19:00"
}

variable "database_retention_days" {
  description = "Database PITR retention days."
  type        = number
  default     = 35
}

variable "object_retention_days" {
  description = "Object storage backup retention days."
  type        = number
  default     = 365
}

variable "log_retention_days" {
  description = "Operational log retention days."
  type        = number
  default     = 180
}
