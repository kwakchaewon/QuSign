# SecureString 파라미터는 value를 Terraform으로 관리하지 않는다.
# import 후 ignore_changes = [value]로 콘솔 값을 그대로 보존한다.

resource "aws_ssm_parameter" "db_password" {
  name        = "/qusign/prod/db-password"
  description = "QuSign 프로덕션 MariaDB 비밀번호"
  type        = "SecureString"
  value       = "PLACEHOLDER"
  lifecycle { ignore_changes = [value] }
}

resource "aws_ssm_parameter" "jwt_secret" {
  name        = "/qusign/prod/jwt-secret"
  description = "QuSign JWT 액세스 토큰 서명 시크릿"
  type        = "SecureString"
  value       = "PLACEHOLDER"
  lifecycle { ignore_changes = [value] }
}

resource "aws_ssm_parameter" "admin_password" {
  name  = "/qusign/prod/admin-password"
  type  = "SecureString"
  value = "PLACEHOLDER"
  lifecycle { ignore_changes = [value] }
}

resource "aws_ssm_parameter" "s3_bucket" {
  name        = "/qusign/prod/s3-bucket"
  description = "S3 버킷명"
  type        = "String"
  value       = "qusign-documents-prod"
}

resource "aws_ssm_parameter" "cors_origins" {
  name        = "/qusign/prod/cors-origins"
  description = "CORS 허용 출처"
  type        = "String"
  value       = "https://qusign.link"
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
