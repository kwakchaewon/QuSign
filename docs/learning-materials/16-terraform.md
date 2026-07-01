# Terraform — Infrastructure as Code (7단계)

> PLAN.md §7-1 대응 — AWS 인프라를 코드로 정의하고 재현한다

---

## 이론

### IaC가 필요한 이유 — 콘솔 클릭의 한계

#### 콘솔(GUI) 방식의 문제

```
팀원 A가 콘솔에서 보안 그룹 규칙을 추가
  → 어디에도 기록 없음
  → 2주 후 팀원 B가 "왜 이 포트가 열려있지?" 모름
  → 재현 불가 (다른 환경에 동일하게 설정하려면 기억에 의존)
  → 실수로 삭제하면 복구 방법 없음
```

#### IaC의 핵심 가치

| 문제 | IaC 해결 방법 |
|---|---|
| 재현 불가 | 코드로 동일 환경 반복 생성 |
| 변경 이력 없음 | Git commit 이력 |
| 환경 불일치 (Dev/Staging/Prod) | 변수만 다른 동일 코드 |
| 실수에 의한 삭제 | `plan` 단계에서 사전 확인 |
| 협업 어려움 | PR 리뷰로 인프라 변경 검토 |

#### 선언형 vs 명령형 IaC

```
명령형 (Shell Script):
  "EC2를 만들어라"
  "보안 그룹을 붙여라"
  "탄력적 IP를 할당하라"
  → 현재 상태를 고려하지 않음 → 멱등성 없음

선언형 (Terraform):
  "EC2가 존재하고, 보안 그룹이 붙어 있고, EIP가 할당된 상태여야 한다"
  → Terraform이 현재 상태와 비교 후 필요한 것만 실행 → 멱등성 보장
```

---

### Terraform 실행 원리

#### 실행 3단계

```
1. terraform init
   ├── .terraform/ 디렉토리 생성
   ├── provider 플러그인 다운로드 (registry.terraform.io)
   └── 백엔드 초기화 (S3 state 연결)

2. terraform plan
   ├── 현재 state (tfstate) 읽기
   ├── 실제 AWS 리소스 상태 조회 (refresh)
   ├── HCL 코드와 비교 → diff 계산
   └── 변경 계획 출력 (실제 변경 없음)

3. terraform apply
   ├── plan 결과 확인 요청 ("yes" 입력)
   ├── 리소스 그래프(DAG) 기반 병렬 생성/수정/삭제
   └── state 파일 업데이트
```

#### 리소스 의존성 그래프(DAG)

Terraform은 리소스 간 의존성을 자동으로 분석하여 병렬 실행합니다.

```hcl
# Terraform이 분석하는 의존성
aws_vpc.main           ← 의존성 없음
aws_subnet.public      ← aws_vpc.main에 의존 (vpc_id 참조)
aws_security_group.ec2 ← aws_vpc.main에 의존
aws_instance.app       ← aws_subnet.public + aws_security_group.ec2에 의존
aws_eip.app            ← aws_instance.app에 의존
```

```
실행 순서 (Terraform이 자동 결정):
  Step 1: aws_vpc.main (병렬)
  Step 2: aws_subnet.public + aws_security_group.ec2 (병렬, VPC 완료 후)
  Step 3: aws_instance.app (서브넷+보안그룹 완료 후)
  Step 4: aws_eip.app (EC2 완료 후)
```

명시적 `depends_on`은 Terraform이 참조 관계를 추론하지 못할 때만 사용합니다.

---

### HCL (HashiCorp Configuration Language) 핵심 문법

#### 기본 구조

```hcl
# 블록 타입  레이블1    레이블2
  resource  "aws_instance"  "app" {
    # 인수 = 값
    ami           = "ami-0c02fb55956c7d316"
    instance_type = "t3.small"

    # 중첩 블록
    tags = {
      Name = "qusign_app"
    }
  }
```

#### 변수 (Variables)

```hcl
# variables.tf
variable "environment" {
  type        = string
  default     = "prod"
  description = "배포 환경"
}

variable "db_password" {
  type      = string
  sensitive = true   # terraform output에서 마스킹
}

# 사용
resource "aws_instance" "app" {
  tags = {
    Environment = var.environment
  }
}
```

```bash
# 값 전달 방법
terraform apply -var="db_password=secret123"
terraform apply -var-file="prod.tfvars"
export TF_VAR_db_password="secret123"  # 환경변수
```

#### 출력값 (Outputs)

```hcl
# outputs.tf
output "ec2_public_ip" {
  value       = aws_eip.app.public_ip
  description = "EC2 고정 IP"
}

output "s3_bucket_name" {
  value = aws_s3_bucket.documents.bucket
}
```

```bash
terraform output ec2_public_ip   # → 3.0.193.52
terraform output -json           # 전체 출력값 JSON
```

#### 데이터 소스 (Data Sources)

기존 리소스를 읽기 전용으로 참조합니다 (Terraform이 관리하지 않는 리소스 포함).

```hcl
# 이미 존재하는 AMI 조회
data "aws_ami" "amazon_linux_2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }
}

resource "aws_instance" "app" {
  ami = data.aws_ami.amazon_linux_2023.id   # 최신 AMI 자동 사용
}
```

---

### State — Terraform이 현실을 추적하는 방법

#### tfstate 파일 구조

```json
{
  "version": 4,
  "resources": [
    {
      "type": "aws_instance",
      "name": "app",
      "instances": [{
        "attributes": {
          "id": "i-0447a621521e4fc2d",
          "instance_type": "t3.small",
          "public_ip": "3.0.193.52"
        }
      }]
    }
  ]
}
```

State가 중요한 이유:
- Terraform은 실제 AWS를 직접 비교하지 않고 **state를 기준으로 diff를 계산**합니다
- State 없이 `apply`하면 이미 있는 리소스를 새로 만들려 해서 충돌이 발생합니다
- State가 손상되면 Terraform이 인프라를 올바르게 관리하지 못합니다

#### S3 원격 백엔드 + 잠금(locking)

state 잠금 방식은 두 가지가 있다. **DynamoDB 잠금**은 오랫동안 업계 표준이었고, **S3 자체 잠금**(`use_lockfile`)은 Terraform 1.10(2024-11)부터 지원되는 최신 방식이다.

```hcl
# 방식 1) DynamoDB 잠금 (전통적 방식)
terraform {
  backend "s3" {
    bucket         = "qusign-terraform-state"
    key            = "prod/terraform.tfstate"
    region         = "ap-southeast-1"
    encrypt        = true                      # SSE-S3 암호화
    dynamodb_table = "qusign-terraform-lock"   # 동시 apply 방지
  }
}
```

```hcl
# 방식 2) S3 자체 잠금 (Terraform 1.10+, QuSign 실제 채택)
terraform {
  backend "s3" {
    bucket       = "qusign-terraform-state"
    key          = "prod/terraform.tfstate"
    region       = "ap-southeast-1"
    encrypt      = true
    use_lockfile = true   # S3 조건부 쓰기(If-None-Match)로 잠금 — DynamoDB 불필요
  }
}
```

**잠금 동작 원리(두 방식 공통)**:
```
개발자 A: terraform apply 시작 → 잠금 항목 생성(DynamoDB 아이템 또는 S3 락파일)
개발자 B: terraform apply 시작 → 잠금 항목 발견 → "State locked" 에러
개발자 A: apply 완료 → 잠금 항목 삭제
개발자 B: 재시도 가능
```

잠금 해제가 필요하면 `terraform force-unlock <LOCK_ID>` (주의: 신중하게).

**QuSign 실전 판단**: 처음엔 계획서에 DynamoDB 방식으로 적었지만, 실제로 `terraform init`을 돌려보니 `dynamodb_table` 파라미터가 Deprecated라는 경고가 떴다. 자료(블로그·튜토리얼)는 대부분 DynamoDB 방식이라 실전 경험 없이는 이 변화를 몰랐을 것이다. 혼자 운영하는 프로젝트라 동시 apply 충돌 위험이 낮고, 관리할 AWS 리소스와 IAM 권한을 하나 줄일 수 있어 S3 자체 잠금으로 바꿨다. 팀 협업 환경이거나 오래된 Terraform 버전(1.10 미만)을 써야 한다면 DynamoDB 방식이 여전히 더 안전한 선택이다.

---

### 모듈 — 재사용 가능한 인프라 컴포넌트

#### 모듈 구조

```
terraform/
├── main.tf          ← 루트 모듈 (모듈 조합)
├── variables.tf
├── outputs.tf
└── modules/
    ├── vpc/
    │   ├── main.tf
    │   ├── variables.tf
    │   └── outputs.tf
    ├── ec2/
    │   ├── main.tf
    │   ├── variables.tf
    │   └── outputs.tf
    └── s3/
        ├── main.tf
        └── variables.tf
```

```hcl
# main.tf (루트)
module "vpc" {
  source      = "./modules/vpc"
  cidr_block  = "10.0.0.0/16"
  name_prefix = "qusign"
  environment = var.environment
}

module "ec2" {
  source         = "./modules/ec2"
  vpc_id         = module.vpc.vpc_id         # 모듈 출력값 참조
  subnet_id      = module.vpc.public_subnet_id
  instance_type  = "t3.small"
}
```

#### Terraform Registry 공개 모듈 활용

```hcl
# 검증된 AWS VPC 모듈 (Terraform Registry)
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "5.0.0"

  name = "qusign-vpc"
  cidr = "10.0.0.0/16"

  azs            = ["ap-southeast-1a"]
  public_subnets = ["10.0.1.0/24"]
}
```

---

### Terraform Workspace

환경별(dev/staging/prod) 독립 state를 관리합니다.

```bash
terraform workspace new dev     # dev 워크스페이스 생성
terraform workspace select prod # prod로 전환
terraform workspace list        # 목록 확인
```

```hcl
# 워크스페이스별 설정 분기
locals {
  instance_type = terraform.workspace == "prod" ? "t3.small" : "t3.micro"
}
```

---

## QuSign 인프라 Terraform 코드화 계획

> **진행 현황 (2026-07-01)**: 아래는 최초 학습 시점의 예시 코드다. 실제로는 `docs/exec-plans/7-1-terraform.md`(Phase 0~4 실행 계획)와 `infra/` 디렉토리에 전체 모듈 HCL을 작성 완료했고, `terraform init`/`validate`까지 통과시켰다. state 백엔드용 S3/DynamoDB 생성과 `terraform import`는 아직 진행 전이다.

### 콘솔 조사에서 계획과 실제가 달랐던 사례

이론만으로는 놓치기 쉬운, "직접 콘솔/CLI로 확인해야만 알 수 있는" 것들이다.

| 가정(계획 초안) | 실제 확인된 것 | 왜 중요한가 |
|---|---|---|
| EC2 정지/시작을 CloudWatch Events Rule로 스케줄링 | 실제로는 **EventBridge Scheduler**(`aws_scheduler_schedule`)로 되어 있었음 — 콘솔 "규칙" 탭엔 아무것도 없고 "Scheduler > 일정"에 있었음 | 두 리소스 타입은 완전히 다른 Terraform 리소스(`aws_cloudwatch_event_rule` vs `aws_scheduler_schedule`). import 대상 자체가 틀리면 `plan`에서 계속 diff가 남는다 |
| GitHub Actions 배포는 IAM Role(OIDC) | 실제로는 IAM **User** + 정적 액세스 키(`aws-actions/configure-aws-credentials` + Secrets) | `aws_iam_role`로 import를 시도하면 애초에 리소스가 없어 실패한다. `aws iam list-roles`에 없으면 `aws iam list-users`도 확인해야 함 |
| S3 게이트웨이 엔드포인트는 public 라우팅 테이블에 연결 | 실제로는 **private 라우팅 테이블**에 연결 | `aws_vpc_endpoint`의 `route_table_ids`를 잘못 넣으면 `plan`에서 계속 변경사항이 뜬다 |
| VPC에 퍼블릭 서브넷 1개만 존재 | 실제로는 public+private 서브넷 각 1개, 라우팅 테이블 3개(public/private/미사용 main) | 콘솔에서 "리소스 맵" 탭으로 서브넷·RT·IGW 연결 관계를 한눈에 봐야 놓치지 않는다 |

**교훈**: `terraform import`를 시작하기 전에 콘솔(또는 `aws ec2 describe-*` 같은 read-only CLI)로 리소스 구조를 먼저 전수조사해야 한다. 계획 문서나 기억에 의존하면 import 대상 리소스 타입 자체가 틀릴 수 있다.

```hcl
# vpc.tf
resource "aws_vpc" "main" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  tags = { Name = "qusign_vpc" }
}

resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id
}

resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = "ap-southeast-1a"
  map_public_ip_on_launch = true
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }
}

# s3.tf
resource "aws_s3_bucket" "documents" {
  bucket = "qusign-documents-prod"  # 실제 버킷명 (계정ID 접미사 없음)
}

resource "aws_s3_bucket_public_access_block" "documents" {
  bucket                  = aws_s3_bucket.documents.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_server_side_encryption_configuration" "documents" {
  bucket = aws_s3_bucket.documents.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# ec2.tf
resource "aws_eip" "app" {
  domain   = "vpc"
  instance = aws_instance.app.id
}

resource "aws_instance" "app" {
  ami                  = data.aws_ami.amazon_linux_2023.id
  instance_type        = "t3.small"
  subnet_id            = aws_subnet.public.id
  vpc_security_group_ids = [aws_security_group.ec2.id]
  iam_instance_profile = aws_iam_instance_profile.ec2.name
  key_name             = aws_key_pair.qusign.key_name

  tags = { Name = "qusign_app", Project = "qusign" }
}
```

### 현재(6단계)와 Terraform(7단계) 비교

| 항목 | 6단계 (AWS 콘솔) | 7단계 (Terraform) |
|---|---|---|
| 인프라 생성 | GUI 클릭 | `terraform apply` |
| 재현 | 불가 | 동일 코드로 재현 |
| 변경 이력 | CloudTrail만 | Git commit |
| 환경 복제 | 처음부터 다시 | 변수만 바꿔 apply |
| 코드 리뷰 | 불가 | PR 리뷰 |
| 사전 확인 | 없음 | `terraform plan` |
| 롤백 | 수동 | Git revert → apply |

---

## 확인 질문 & 답변

**Q1. Terraform이 의존성을 자동으로 파악하는 원리는?**

> HCL에서 한 리소스의 속성이 다른 리소스의 출력값을 참조할 때(`aws_instance.app.id` 같이) Terraform이 의존 관계를 인식합니다. 이를 기반으로 DAG(방향 비순환 그래프)를 구성하고, 의존성 없는 리소스는 병렬로 생성합니다. 참조 관계로 추론할 수 없는 암묵적 의존성은 `depends_on`으로 명시합니다.

**Q2. `terraform plan`을 `apply` 전에 항상 실행해야 하는 이유는?**

> `plan`은 실제 인프라를 변경하지 않고 "무엇이 생성·수정·삭제될지"를 보여줍니다. `-`(삭제)로 표시된 리소스 중 DB나 S3가 있으면 데이터 손실이 발생합니다. 특히 `count`나 `for_each`를 변경할 때 리소스가 삭제 후 재생성될 수 있어 반드시 확인해야 합니다. CI/CD 파이프라인에서는 `plan` 결과를 PR 코멘트로 올리는 패턴이 일반적입니다.

**Q3. S3 백엔드를 쓰지 않고 로컬 `terraform.tfstate`를 팀원과 공유하면 어떤 문제가 생기나?**

> 두 사람이 동시에 `terraform apply`를 실행하면 state가 충돌해 인프라와 state가 불일치 상태가 됩니다. 파일을 Git에 올리면 민감 정보(DB 비밀번호 등)가 노출됩니다. S3 백엔드를 사용하면 state를 중앙 암호화 저장할 수 있고, 동시 적용 방지를 위한 잠금은 DynamoDB(전통적 방식) 또는 Terraform 1.10+의 S3 자체 잠금(`use_lockfile`, QuSign이 실제 채택한 방식) 중 하나로 구성합니다.

**Q4. 6단계에서 콘솔로 만든 기존 인프라를 Terraform으로 가져올 수 있는가?**

> `terraform import`로 가능합니다: `terraform import aws_instance.app i-0447a621521e4fc2d`. 단, import는 state에 리소스를 등록하지만 HCL 코드는 자동 생성되지 않습니다. Terraform 1.5+에서는 `import {}` 블록 + `terraform plan`이 HCL 코드 초안을 생성합니다. 또는 `terraformer` 같은 도구로 기존 인프라를 역방향으로 HCL 코드화할 수 있습니다.

**Q5. `sensitive = true` 변수로 선언한 값이 state 파일에 저장되면 어떻게 처리되는가?**

> `sensitive = true`는 CLI 출력에서만 마스킹합니다. tfstate 파일에는 평문으로 저장됩니다. 따라서 S3 백엔드 + SSE 암호화 + 버킷 정책(EC2 역할만 접근)이 필수입니다. 더 안전하게는 AWS Secrets Manager나 SSM Parameter Store에서 직접 읽는 data source를 활용합니다.

**Q6. `terraform destroy`는 언제 사용하는가?**

> 더 이상 필요 없는 개발/스테이징 환경을 삭제할 때 사용합니다. 운영 환경에서는 실수로 실행하지 않도록 별도 AWS 계정 분리와 IAM 정책 제한이 필요합니다. 특정 리소스만 삭제하려면 `terraform destroy -target=aws_instance.app` 처럼 타겟을 지정합니다. QuSign에서는 포트폴리오 제출 후 비용 절감을 위해 비핵심 리소스에만 사용합니다.

**Q7. `import` 대상 리소스 타입을 콘솔에서 확인하지 않고 계획 문서만 믿고 진행하면 어떻게 되는가?**

> QuSign 실제 사례로 답할 수 있다: 계획 초안은 EC2 정지/시작 스케줄이 `aws_cloudwatch_event_rule`이라고 가정했지만, 콘솔을 열어보니 실제로는 `aws_scheduler_schedule`(EventBridge Scheduler)이었다. 존재하지 않는 타입으로 `terraform import`를 시도하면 AWS API가 "그런 리소스 없음" 에러를 내거나, 최악의 경우 이름이 우연히 겹쳐 엉뚱한 리소스를 잘못 흡수할 수 있다. 같은 이유로 GitHub Actions 배포 주체도 IAM Role이 아니라 IAM User였다. 그래서 Phase 2(import) 전에 반드시 Phase 0(콘솔·CLI 조사)로 리소스 타입까지 확정해야 하며, `aws iam list-roles`에 없다고 포기하지 말고 `list-users`처럼 인접한 리소스 종류도 함께 확인해야 한다.
