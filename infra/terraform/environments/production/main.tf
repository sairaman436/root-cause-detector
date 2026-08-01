# Purpose: Composes production infrastructure modules for the enterprise platform.
# Why it exists: Gives platform engineering a canonical IaC entry point for primary-region deployment.
# Architecture fit: Supports Milestone 11 Terraform, high availability, disaster recovery, and global deployment requirements.
terraform {
  required_version = ">= 1.8.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.60"
    }
  }
}

provider "aws" {
  region = var.region
}

module "network" {
  source               = "../../modules/network"
  name                 = var.name
  cidr_block           = var.cidr_block
  availability_zones   = var.availability_zones
  public_subnet_cidrs  = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs
}

module "iam" {
  source = "../../modules/iam"
  name   = var.name
}

module "kubernetes" {
  source             = "../../modules/kubernetes"
  name               = var.name
  subnet_ids         = module.network.private_subnet_ids
  node_role_arn      = module.iam.node_role_arn
  cluster_role_arn   = module.iam.cluster_role_arn
  kubernetes_version = var.kubernetes_version
}

module "database" {
  source             = "../../modules/database"
  name               = var.name
  subnet_ids         = module.network.private_subnet_ids
  vpc_id             = module.network.vpc_id
  database_name      = "airural"
  backup_window      = var.backup_window
  retention_days     = var.database_retention_days
  multi_az           = true
  deletion_protection = true
}

module "redis" {
  source     = "../../modules/redis"
  name       = var.name
  subnet_ids = module.network.private_subnet_ids
  vpc_id     = module.network.vpc_id
}

module "object_storage" {
  source               = "../../modules/object-storage"
  name                 = var.name
  backup_retention_days = var.object_retention_days
}

module "load_balancer" {
  source            = "../../modules/load-balancer"
  name              = var.name
  vpc_id            = module.network.vpc_id
  public_subnet_ids = module.network.public_subnet_ids
}

module "dns" {
  source      = "../../modules/dns"
  domain_name = var.domain_name
  target_name = module.load_balancer.dns_name
  target_zone = module.load_balancer.zone_id
}

module "monitoring" {
  source = "../../modules/monitoring"
  name   = var.name
}

module "logging" {
  source          = "../../modules/logging"
  name            = var.name
  retention_days  = var.log_retention_days
  archive_bucket  = module.object_storage.audit_bucket_name
}
