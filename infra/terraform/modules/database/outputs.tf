# Purpose: Exposes managed database connection metadata.
# Why it exists: Allows secret automation and deployment wiring.
# Architecture fit: Supports Milestone 11 operational integration.
output "endpoint" {
  value       = aws_db_instance.this.endpoint
  description = "Database endpoint."
}
