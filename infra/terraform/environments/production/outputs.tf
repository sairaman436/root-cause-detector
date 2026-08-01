# Purpose: Exposes production infrastructure outputs for deployment automation.
# Why it exists: Allows CI/CD, DNS validation, and operations runbooks to consume canonical resource identifiers.
# Architecture fit: Supports Milestone 11 release and operations integration.
output "cluster_name" {
  description = "Managed Kubernetes cluster name."
  value       = module.kubernetes.cluster_name
}

output "database_endpoint" {
  description = "Managed PostgreSQL endpoint."
  value       = module.database.endpoint
  sensitive   = true
}

output "platform_url" {
  description = "Primary platform URL."
  value       = "https://${var.domain_name}"
}
