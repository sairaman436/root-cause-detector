# Purpose: Creates public load balancer entry point.
# Why it exists: Provides HA ingress target for DNS and Kubernetes ingress controllers.
# Architecture fit: Supports Milestone 11 load balancing and global deployment.
resource "aws_lb" "this" {
  name               = var.name
  load_balancer_type = "application"
  subnets            = var.public_subnet_ids
  security_groups    = [aws_security_group.this.id]
}

resource "aws_security_group" "this" {
  name   = "${var.name}-lb"
  vpc_id = var.vpc_id
}
