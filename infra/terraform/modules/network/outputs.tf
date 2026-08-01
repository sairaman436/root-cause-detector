# Purpose: Exposes network identifiers to dependent modules.
# Why it exists: Enables cluster, database, Redis, and load balancer composition.
# Architecture fit: Supports Milestone 11 module dependency rules.
output "vpc_id" {
  value       = aws_vpc.this.id
  description = "VPC identifier."
}

output "public_subnet_ids" {
  value       = aws_subnet.public[*].id
  description = "Public subnet identifiers."
}

output "private_subnet_ids" {
  value       = aws_subnet.private[*].id
  description = "Private subnet identifiers."
}
