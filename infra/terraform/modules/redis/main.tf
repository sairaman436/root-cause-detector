# Purpose: Creates managed Redis for cache and rate-limiting workloads.
# Why it exists: Provides HA cache infrastructure outside application containers.
# Architecture fit: Supports Milestone 11 Redis replication, caching, and performance requirements.
resource "aws_elasticache_subnet_group" "this" {
  name       = "${var.name}-redis"
  subnet_ids = var.subnet_ids
}

resource "aws_security_group" "this" {
  name   = "${var.name}-redis"
  vpc_id = var.vpc_id
}

resource "aws_elasticache_replication_group" "this" {
  replication_group_id       = "${var.name}-redis"
  description                = "AI Rural platform Redis replication group"
  engine                     = "redis"
  node_type                  = "cache.m7g.large"
  num_cache_clusters         = 2
  automatic_failover_enabled = true
  at_rest_encryption_enabled = true
  transit_encryption_enabled = true
  subnet_group_name          = aws_elasticache_subnet_group.this.name
  security_group_ids         = [aws_security_group.this.id]
}
