# Purpose: Exposes load balancer DNS metadata.
# Why it exists: Enables DNS module composition.
# Architecture fit: Supports Milestone 11 global deployment.
output "dns_name" {
  value       = aws_lb.this.dns_name
  description = "Load balancer DNS name."
}

output "zone_id" {
  value       = aws_lb.this.zone_id
  description = "Load balancer hosted zone ID."
}
