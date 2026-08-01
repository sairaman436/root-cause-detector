# Purpose: Creates encrypted object storage buckets for evidence, model artifacts, and audit archives.
# Why it exists: Provides durable storage for files, backups, SBOMs, reports, and MLOps artifacts.
# Architecture fit: Supports Milestone 11 object storage, backup, and compliance requirements.
resource "aws_s3_bucket" "evidence" {
  bucket = "${var.name}-evidence"
}

resource "aws_s3_bucket" "models" {
  bucket = "${var.name}-models"
}

resource "aws_s3_bucket" "audit" {
  bucket = "${var.name}-audit"
}

resource "aws_s3_bucket_lifecycle_configuration" "audit" {
  bucket = aws_s3_bucket.audit.id

  rule {
    id     = "archive"
    status = "Enabled"

    expiration {
      days = var.backup_retention_days
    }
  }
}
