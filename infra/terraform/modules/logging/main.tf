# Purpose: Creates centralized log groups and archive policy links.
# Why it exists: Provides retention and audit evidence for production operations.
# Architecture fit: Supports Milestone 11 logging, compliance, and incident response.
resource "aws_cloudwatch_log_group" "application" {
  name              = "/${var.name}/application"
  retention_in_days = var.retention_days
}

resource "aws_cloudwatch_log_group" "audit" {
  name              = "/${var.name}/audit"
  retention_in_days = var.retention_days
}
