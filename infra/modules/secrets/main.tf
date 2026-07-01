# SecureString 파라미터는 value를 Terraform으로 관리하지 않는다.
# import 후 ignore_changes = [value]로 콘솔 값을 그대로 보존한다.

resource "aws_ssm_parameter" "db_password" {
  name  = "/qusign/prod/db-password"
  type  = "SecureString"
  value = "PLACEHOLDER"
  lifecycle { ignore_changes = [value] }
}

resource "aws_ssm_parameter" "jwt_secret" {
  name  = "/qusign/prod/jwt-secret"
  type  = "SecureString"
  value = "PLACEHOLDER"
  lifecycle { ignore_changes = [value] }
}

resource "aws_ssm_parameter" "admin_password" {
  name  = "/qusign/prod/admin-password"
  type  = "SecureString"
  value = "PLACEHOLDER"
  lifecycle { ignore_changes = [value] }
}

resource "aws_ssm_parameter" "s3_bucket" {
  name  = "/qusign/prod/s3-bucket"
  type  = "String"
  value = "qusign-documents-prod"
}

resource "aws_ssm_parameter" "cors_origins" {
  name  = "/qusign/prod/cors-origins"
  type  = "String"
  value = "https://qusign.link"
}

resource "aws_ssm_parameter" "server_url" {
  name  = "/qusign/prod/server-url"
  type  = "String"
  value = "https://qusign.link"
}

resource "aws_ssm_parameter" "admin_email" {
  name  = "/qusign/prod/admin-email"
  type  = "String"
  value = "PLACEHOLDER"
  lifecycle { ignore_changes = [value] }
}
