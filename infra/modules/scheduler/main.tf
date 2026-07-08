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
  # 실제 콘솔 값은 python3.14이지만, 설치된 aws provider(5.100.0)가 아직 이 값을 유효한
  # enum으로 인식하지 못해 HCL엔 유효한 최신값을 적고 실제 값과의 diff는 ignore_changes로 무시한다.
  runtime = "python3.13"
  handler = "lambda_function.lambda_handler"
  role    = data.aws_iam_role.lambda_exec.arn
  publish = false

  # 코드는 콘솔/배포 파이프라인 관리 — Terraform은 메타데이터만 관리
  filename         = "${path.module}/lambda_placeholder.zip"
  source_code_hash = filebase64sha256("${path.module}/lambda_placeholder.zip")

  # 실제 함수는 환경변수 없이, EventBridge Scheduler의 target input({"action":"start"/"stop"})으로 동작을 구분함
  lifecycle { ignore_changes = [filename, source_code_hash, runtime] }
}

resource "aws_cloudwatch_log_group" "lambda" {
  name              = "/aws/lambda/qusign_start_instances"
  retention_in_days = 0 # 실제 값: 만료 없음(Never expire). 계획서 초안의 14는 잘못된 가정이었음
}

# ⚠️ 실제 스케줄은 UTC로 환산한 값이 아니라 schedule_expression_timezone="Asia/Seoul"로
# KST 시각을 그대로 사용하고 있었다 (EventBridge Scheduler는 타임존 파라미터를 직접 지원 — 레거시
# CloudWatch Events Rule은 UTC cron만 지원해서 계획 초안 때 습관적으로 UTC 환산값을 적었던 실수).
resource "aws_scheduler_schedule" "morning_start" {
  name                         = "qusign_morning_start"
  group_name                   = "default"
  schedule_expression          = "cron(0 9 * * ? *)" # KST 09:00 (타임존 자체가 Asia/Seoul)
  schedule_expression_timezone = "Asia/Seoul"
  flexible_time_window { mode = "OFF" }

  target {
    arn      = aws_lambda_function.ec2_scheduler.arn
    role_arn = data.aws_iam_role.scheduler_exec.arn
    input    = "{\"action\": \"start\"}" # 실제 저장된 문자열과 공백까지 일치 (jsonencode()는 콜론 뒤 공백이 없어 diff 발생)

    retry_policy {
      maximum_retry_attempts = 0 # 실제 콘솔 값 (재시도 없음)
    }
  }
}

resource "aws_scheduler_schedule" "nightly_stop" {
  name                         = "qusign_nightly_stop"
  group_name                   = "default"
  schedule_expression          = "cron(30 21 * * ? *)" # KST 21:30
  schedule_expression_timezone = "Asia/Seoul"
  flexible_time_window { mode = "OFF" }

  target {
    arn      = aws_lambda_function.ec2_scheduler.arn
    role_arn = data.aws_iam_role.scheduler_exec.arn
    input    = "{\"action\": \"stop\"}"

    retry_policy {
      maximum_retry_attempts = 0
    }
  }
}
