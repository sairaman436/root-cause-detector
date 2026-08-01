# Purpose: Creates managed PostgreSQL with backups and deletion protection.
# Why it exists: Provides durable operational storage with high availability and recovery controls.
# Architecture fit: Supports Milestone 11 managed PostgreSQL, PITR, and DR requirements.
resource "aws_db_subnet_group" "this" {
  name       = "${var.name}-db"
  subnet_ids = var.subnet_ids
}

resource "aws_security_group" "this" {
  name   = "${var.name}-postgres"
  vpc_id = var.vpc_id
}

resource "aws_db_instance" "this" {
  identifier              = "${var.name}-postgres"
  engine                  = "postgres"
  engine_version          = "16"
  instance_class          = "db.m6g.large"
  allocated_storage       = 100
  max_allocated_storage   = 1000
  db_name                 = var.database_name
  username                = "airural"
  manage_master_user_password = true
  multi_az                = var.multi_az
  db_subnet_group_name    = aws_db_subnet_group.this.name
  vpc_security_group_ids  = [aws_security_group.this.id]
  backup_retention_period = var.retention_days
  backup_window           = var.backup_window
  deletion_protection     = var.deletion_protection
  storage_encrypted       = true
  skip_final_snapshot     = false
}
