# Purpose: Creates monitoring workspace placeholders for managed metrics.
# Why it exists: Allows platform observability to be promoted to managed services where required.
# Architecture fit: Supports Milestone 11 monitoring and alerting architecture.
resource "aws_cloudwatch_log_group" "alerts" {
  name              = "/${var.name}/alerts"
  retention_in_days = 180
}
