data "aws_route53_zone" "main" {
  name         = "qusign.link."
  private_zone = false
}

resource "aws_route53_record" "root" {
  zone_id = data.aws_route53_zone.main.zone_id
  name    = "qusign.link"
  type    = "A"
  ttl     = 300
  records = [var.elastic_ip]
}

resource "aws_route53_record" "www" {
  zone_id = data.aws_route53_zone.main.zone_id
  name    = "www.qusign.link"
  type    = "CNAME"
  ttl     = 300
  records = ["qusign.link"]
}
