# Purpose: Exposes object storage bucket names to dependent modules.
# Why it exists: Allows logging, backup, and MLOps modules to archive evidence.
# Architecture fit: Supports Milestone 11 module composition.
output "audit_bucket_name" {
  value       = aws_s3_bucket.audit.bucket
  description = "Audit archive bucket."
}
