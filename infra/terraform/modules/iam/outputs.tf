# Purpose: Exposes IAM role ARNs to Kubernetes module.
# Why it exists: Allows explicit module wiring without implicit lookups.
# Architecture fit: Supports Milestone 11 IaC composition.
output "cluster_role_arn" {
  value       = aws_iam_role.cluster.arn
  description = "Kubernetes cluster role ARN."
}

output "node_role_arn" {
  value       = aws_iam_role.node.arn
  description = "Kubernetes node role ARN."
}
