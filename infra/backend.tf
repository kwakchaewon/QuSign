terraform {
  backend "s3" {
    bucket         = "qusign-terraform-state"
    key            = "prod/terraform.tfstate"
    region         = "ap-southeast-1"
    encrypt        = true
    dynamodb_table = "qusign-terraform-lock"
  }
}
