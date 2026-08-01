# Purpose: Creates DNS alias for the production platform.
# Why it exists: Provides stable user and API entry points.
# Architecture fit: Supports Milestone 11 DNS and global deployment requirements.
data "aws_route53_zone" "this" {
  name         = var.domain_name
  private_zone = false
}

resource "aws_route53_record" "platform" {
  zone_id = data.aws_route53_zone.this.zone_id
  name    = var.domain_name
  type    = "A"

  alias {
    name                   = var.target_name
    zone_id                = var.target_zone
    evaluate_target_health = true
  }
}
