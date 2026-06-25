# Terraform — Infrastructure as Code (7단계)

> PLAN.md §7-1 대응 — AWS 인프라를 코드로 정의하고 재현한다

---

## 이론

### IaC (Infrastructure as Code) 가치

| 방식 | 문제점 |
|---|---|
| 콘솔(GUI) 클릭 | 재현 불가, 변경 이력 없음, 실수 잦음 |
| 셸 스크립트 | 멱등성 없음, 순서 의존성, 에러 처리 복잡 |
| **Terraform** | **선언형, 멱등성, 변경 계획 미리 확인 가능** |

```
개발자가 원하는 상태를 선언  →  Terraform이 현재 상태와 비교  →  차이만 적용
```

### HCL (HashiCorp Configuration Language)

```hcl
# main.tf
provider "aws" {
  region = "ap-southeast-1"
}

resource "aws_instance" "app" {
  ami           = "ami-0c02fb55956c7d316"   # Amazon Linux 2023
  instance_type = "t3.small"
  key_name      = aws_key_pair.qusign.key_name

  tags = {
    Name    = "qusign_app"
    Project = "qusign"
  }
}

# 출력값 — apply 후 확인 가능
output "ec2_public_ip" {
  value = aws_instance.app.public_ip
}
```

### Terraform 핵심 명령어

```bash
terraform init      # 프로바이더 다운로드, 백엔드 초기화
terraform plan      # 변경 계획 미리보기 (실제 변경 없음) ← 항상 먼저 실행
terraform apply     # 계획 적용 (실제 인프라 변경)
terraform destroy   # 인프라 삭제
terraform show      # 현재 state 확인
```

`terraform plan` 출력 예시:
```
+ aws_instance.app will be created        # + 생성
~ aws_security_group.main will be updated # ~ 수정
- aws_s3_bucket.old will be destroyed     # - 삭제
```

### State — Terraform이 관리하는 현실

```
terraform.tfstate  ← 현재 인프라 상태를 JSON으로 추적
```

로컬 state 파일은 팀 협업 시 충돌이 발생합니다.
S3 백엔드를 사용하면 state를 중앙에 저장하고 동시 수정을 방지합니다:

```hcl
terraform {
  backend "s3" {
    bucket         = "qusign-terraform-state"
    key            = "prod/terraform.tfstate"
    region         = "ap-southeast-1"
    dynamodb_table = "qusign-terraform-locks"  # 동시 apply 방지 (잠금)
  }
}
```

### 모듈 — 재사용 가능한 컴포넌트

```
modules/
  vpc/     main.tf, variables.tf, outputs.tf
  ec2/     main.tf, variables.tf, outputs.tf
  s3/      main.tf, variables.tf, outputs.tf

main.tf  ← 모듈 조합
```

```hcl
module "vpc" {
  source      = "./modules/vpc"
  cidr_block  = "10.0.0.0/16"
  name_prefix = "qusign"
}

module "ec2" {
  source    = "./modules/ec2"
  vpc_id    = module.vpc.vpc_id
  subnet_id = module.vpc.public_subnet_id
}
```

---

## QuSign 인프라 Terraform 코드화 계획

```hcl
# vpc.tf
resource "aws_vpc" "main" {
  cidr_block = "10.0.0.0/16"
  tags = { Name = "qusign_vpc" }
}

resource "aws_subnet" "public" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.1.0/24"
  availability_zone = "ap-southeast-1a"
}

# s3.tf
resource "aws_s3_bucket" "documents" {
  bucket = "qusign-documents-prod-285868221698"
}

resource "aws_s3_bucket_public_access_block" "documents" {
  bucket                  = aws_s3_bucket.documents.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# iam.tf
resource "aws_iam_role" "ec2" {
  name               = "qusign_ec2_role"
  assume_role_policy = data.aws_iam_policy_document.ec2_trust.json
}
```

### 현재(6단계)와 Terraform(7단계) 차이

| | 6단계 (AWS 콘솔) | 7단계 (Terraform) |
|---|---|---|
| 인프라 생성 | GUI 클릭 | `terraform apply` |
| 재현 | 불가 | 가능 |
| 변경 이력 | 없음 | Git 커밋 이력 |
| 실수 방지 | 없음 | `plan`으로 사전 확인 |
| 롤백 | 수동 | `terraform apply` (이전 상태 코드로) |

---

## 확인 질문 & 답변

**Q1. `terraform plan`을 `apply` 전에 항상 실행해야 하는 이유는?**

> `plan`은 실제 인프라를 변경하지 않고 "무엇이 생성·수정·삭제될지"를 보여줍니다. 예상치 못한 리소스가 삭제(`-`)로 표시되면 적용 전에 코드를 수정할 수 있습니다. 특히 운영 환경에서 DB나 S3가 삭제 계획에 포함되면 `apply` 전에 반드시 확인해야 합니다.

**Q2. S3 백엔드를 쓰지 않고 로컬 `terraform.tfstate`를 팀원과 공유하면 어떤 문제가 생기나?**

> 두 사람이 동시에 `terraform apply`를 실행하면 state가 충돌해 인프라가 불일치 상태가 됩니다. S3 + DynamoDB 조합을 사용하면 state를 S3에 중앙 저장하고 DynamoDB 잠금(lock)으로 동시 적용을 방지합니다.

**Q3. 6단계에서 콘솔로 만든 기존 인프라를 Terraform으로 가져올 수 있는가?**

> `terraform import` 명령으로 가능합니다. 예: `terraform import aws_instance.app i-0447a621521e4fc2d`. 단, 리소스별로 import 명령을 수동으로 실행해야 하고 HCL 코드도 직접 작성해야 합니다. 최근 Terraform은 `terraform import` 블록 선언 방식도 지원합니다.

**Q4. `terraform destroy`는 언제 사용하는가?**

> 더 이상 필요 없는 개발/스테이징 환경을 삭제할 때 사용합니다. 모든 리소스가 `plan` 확인 후 삭제됩니다. 운영 환경에서는 실수로 실행하지 않도록 별도 계정 분리와 IAM 제한이 필요합니다. QuSign에서는 포트폴리오 제출 후 비용 절감을 위해 비핵심 리소스에만 사용합니다.
