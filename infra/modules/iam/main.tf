# GitHub Actions 배포용 — 콘솔 확인 결과 Role이 아니라 User (OIDC 미설정, 정적 액세스 키 방식)
resource "aws_iam_user" "github_actions_deployer" {
  name = "qusign_github_actions_deployer"
}

# 액세스 키(aws_iam_access_key)는 시크릿 값이 생성 시점에만 노출되고 재조회 불가하므로
# Terraform으로 새로 만들지 않는다. 기존 GitHub Secrets(AWS_ACCESS_KEY_ID/AWS_SECRET_ACCESS_KEY)
# 값을 그대로 유지하고, 이 User 리소스는 이름만 코드로 관리한다.
# TODO(장기 개선): OIDC 기반 aws_iam_role + aws_iam_openid_connect_provider로 전환해
# 정적 액세스 키 자체를 제거하는 것을 8단계 이후 검토 (현재는 범위 밖)
