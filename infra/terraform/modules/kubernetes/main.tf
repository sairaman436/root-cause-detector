# Purpose: Creates managed Kubernetes control plane and node group.
# Why it exists: Provides the production runtime for all platform services.
# Architecture fit: Supports Milestone 11 Kubernetes, autoscaling, and high availability.
resource "aws_eks_cluster" "this" {
  name     = var.name
  role_arn = var.cluster_role_arn
  version  = var.kubernetes_version

  vpc_config {
    subnet_ids = var.subnet_ids
  }
}

resource "aws_eks_node_group" "general" {
  cluster_name    = aws_eks_cluster.this.name
  node_group_name = "${var.name}-general"
  node_role_arn   = var.node_role_arn
  subnet_ids      = var.subnet_ids

  scaling_config {
    desired_size = 3
    min_size     = 3
    max_size     = 12
  }
}
