# Purpose: Creates IAM roles required by managed Kubernetes and worker nodes.
# Why it exists: Keeps cloud permissions scoped and auditable.
# Architecture fit: Supports Milestone 11 IAM, least privilege, and supply chain security.
data "aws_iam_policy_document" "eks_cluster_assume" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["eks.amazonaws.com"]
    }
  }
}

data "aws_iam_policy_document" "eks_node_assume" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "cluster" {
  name               = "${var.name}-cluster-role"
  assume_role_policy = data.aws_iam_policy_document.eks_cluster_assume.json
}

resource "aws_iam_role" "node" {
  name               = "${var.name}-node-role"
  assume_role_policy = data.aws_iam_policy_document.eks_node_assume.json
}
