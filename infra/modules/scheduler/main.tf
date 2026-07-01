# qusign_lambda_eventbridge_role / qusign_eventbridge_scheduler_role은 기존에 콘솔로
# 생성된 리소스 — 이 모듈에서 import/생성하지 않고 이름으로 조회만 한다.
data "aws_iam_role" "lambda_exec" {
  name = "qusign_lambda_eventbridge_role"
}

data "aws_iam_role" "scheduler_exec" {
  name = "qusign_eventbridge_scheduler_role"
}

resource "aws_lambda_function" "ec2_scheduler" {
  function_name = "qusign_start_instances"
  runtime       = "python3.13"
  handler       = "lambda_function.lambda_handler"
  role          = data.aws_iam_role.lambda_exec.arn

  # 코드는 콘솔/배포 파이프라인 관리 — Terraform은 메타데이터만 관리
  filename         = "${path.module}/lambda_placeholder.zip"
  source_code_hash = filebase64sha256("${path.module}/lambda_placeholder.zip")

  environment {
    variables = {
      INSTANCE_ID = var.ec2_instance_id
    }
  }

  lifecycle { ignore_changes = [filename, source_code_hash] }
}

resource "aws_cloudwatch_log_group" "lambda" {
  name              = "/aws/lambda/qusign_start_instances"
  retention_in_days = 14
}

resource "aws_scheduler_schedule" "morning_start" {
  name                = "qusign_morning_start"
  group_name          = "default"
  schedule_expression = "cron(0 0 * * ? *)" # UTC 00:00 = KST 09:00
  flexible_time_window { mode = "OFF" }

  target {
    arn      = aws_lambda_function.ec2_scheduler.arn
    role_arn = data.aws_iam_role.scheduler_exec.arn
  }
}

resource "aws_scheduler_schedule" "nightly_stop" {
  name                = "qusign_nightly_stop"
  group_name          = "default"
  schedule_expression = "cron(30 12 * * ? *)" # UTC 12:30 = KST 21:30
  flexible_time_window { mode = "OFF" }

  target {
    arn      = aws_lambda_function.ec2_scheduler.arn
    role_arn = data.aws_iam_role.scheduler_exec.arn
  }
}
