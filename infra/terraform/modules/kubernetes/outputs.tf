# Purpose: Exposes managed Kubernetes identifiers.
# Why it exists: Supports deployment automation and runbooks.
# Architecture fit: Supports Milestone 11 release management.
output "cluster_name" {
  value       = aws_eks_cluster.this.name
  description = "Cluster name."
}
