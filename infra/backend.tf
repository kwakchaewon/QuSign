terraform {
  backend "s3" {
    bucket       = "qusign-terraform-state"
    key          = "prod/terraform.tfstate"
    region       = "ap-southeast-1"
    encrypt      = true
    use_lockfile = true # S3 조건부 쓰기 기반 잠금 (Terraform 1.10+) — DynamoDB 불필요
  }
}
