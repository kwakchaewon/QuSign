# 7-1. Terraform 인프라 코드화 — 실행 계획

> 작성일: 2026-06-30  
> 대상 환경: AWS ap-southeast-1 (싱가포르)  
> 현재 상태: 콘솔 수동 세팅으로 운영 중 → HCL 코드화  
> **핵심 원칙: 운영 서버를 절대 중단하지 않는다. import → plan no-changes → apply 순서 엄수.**

---

## 현재 운영 중인 AWS 리소스 목록

| 리소스 | 이름/값 | 비고 |
|---|---|---|
| Region | ap-southeast-1 | 싱가포르 |
| EC2 | qusign_app / t3.small | |
| Elastic IP | 3.0.193.52 | Route 53 A 레코드 연결 |
| VPC | qusign_vpc / 10.0.0.0/16 | |
| Subnet | Public / ap-southeast-1a | |
| Security Group | 22(0.0.0.0/0), 80, 443 inbound | |
| S3 | qusign_documents_prod | SSE-S3, 퍼블릭 차단 |
| S3 VPC Endpoint | Gateway type | 무료, 인터넷 미경유 |
| ECR | qusign_backend | |
| SSM (SecureString) | /qusign/prod/db-password, jwt-secret, admin-password | KMS 암호화 |
| SSM (String) | /qusign/prod/s3-bucket, cors-origins, server-url, admin-email | |
| Lambda | qusign_start_instances / Python 3.13 | |
| EventBridge | cron(0 9 \* \* ? \*) 시작 / cron(30 21 \* \* ? \*) 정지 | Asia/Seoul |
| IAM Role | qusign_ec2_role | S3·SES·SSM·ECR 접근 |
| IAM Role | qusign_github_actions_deployer | ECR push·SSM·EC2 제어 |
| Route 53 | qusign.link → 3.0.193.52 | A 레코드 |
| CloudWatch Logs | Lambda 실행 이력 | |

---

## Phase 0: 사전 정보 수집 (0.5일)

AWS 콘솔에서 아래 ID를 메모장에 기록한다. import 명령어에 직접 쓸 값이다.

```
# 수집 항목 (AWS Console → 각 서비스 페이지)

VPC
  VPC ID:          vpc-__________
  Subnet ID:       subnet-__________
  IGW ID:          igw-__________
  Route Table ID:  rtb-__________

EC2
  Instance ID:     i-__________________
  Key Pair 이름:   (예: qusign-key)
  AMI ID:          ami-__________________  ← EC2 상세 페이지에서 확인

Security Group
  SG ID:           sg-__________

Elastic IP
  Allocation ID:   eipalloc-__________

S3
  버킷 이름:       qusign_documents_prod
  VPC Endpoint ID: vpce-__________

ECR
  Repository URI:  (secrets.ECR_REGISTRY 값 확인)

IAM
  EC2 Role ARN:    arn:aws:iam::<계정ID>:role/qusign_ec2_role
  GitHub Role ARN: arn:aws:iam::<계정ID>:role/qusign_github_actions_deployer
  Instance Profile 이름: qusign_ec2_role (보통 역할명과 동일)

Lambda
  Function ARN:    arn:aws:lambda:ap-southeast-1:<계정ID>:function:qusign_start_instances

EventBridge
  시작 규칙 이름: (콘솔에서 확인)
  정지 규칙 이름: (콘솔에서 확인)

Route 53
  Hosted Zone ID:  Z__________________
  A 레코드 이름:   qusign.link

계정 ID: (AWS Console 우상단 계정명 클릭)
```

---

## Phase 1: Terraform 환경 세팅 (1일)

### 1-1. state 전용 S3 버킷 + DynamoDB 테이블 생성 (콘솔 1회)

```bash
# 앱 버킷(qusign_documents_prod)과 별도로 생성
# 콘솔 또는 AWS CLI로 직접 생성 — Terraform으로 관리하면 닭-달걀 문제 발생

aws s3api create-bucket \
  --bucket qusign-terraform-state \
  --region ap-southeast-1 \
  --create-bucket-configuration LocationConstraint=ap-southeast-1

aws s3api put-bucket-versioning \
  --bucket qusign-terraform-state \
  --versioning-configuration Status=Enabled

aws s3api put-bucket-encryption \
  --bucket qusign-terraform-state \
  --server-side-encryption-configuration '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"}}]}'

aws dynamodb create-table \
  --table-name qusign-terraform-lock \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region ap-southeast-1
```

### 1-2. 디렉토리 구조 생성

```
QuSign/
└── infra/
    ├── .gitignore
    ├── backend.tf
    ├── main.tf
    ├── variables.tf
    ├── terraform.tfvars        ← gitignore 대상
    ├── outputs.tf
    └── modules/
        ├── networking/
        │   ├── main.tf
        │   ├── variables.tf
        │   └── outputs.tf
        ├── compute/
        │   ├── main.tf
        │   ├── variables.tf
        │   └── outputs.tf
        ├── storage/
        │   ├── main.tf
        │   ├── variables.tf
        │   └── outputs.tf
        ├── secrets/
        │   ├── main.tf
        │   ├── variables.tf
        │   └── outputs.tf
        ├── scheduler/
        │   ├── main.tf
        │   ├── variables.tf
        │   └── outputs.tf
        └── dns/
            ├── main.tf
            ├── variables.tf
            └── outputs.tf
```

### 1-3. 핵심 루트 파일

**infra/.gitignore**
```
.terraform/
*.tfstate
*.tfstate.backup
*.tfvars
.terraform.lock.hcl
```

**infra/backend.tf**
```hcl
terraform {
  backend "s3" {
    bucket         = "qusign-terraform-state"
    key            = "prod/terraform.tfstate"
    region         = "ap-southeast-1"
    encrypt        = true
    dynamodb_table = "qusign-terraform-lock"
  }
}
```

**infra/main.tf**
```hcl
terraform {
  required_version = ">= 1.6"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

module "networking" {
  source = "./modules/networking"
  vpc_cidr       = var.vpc_cidr
  my_ip_cidr     = var.my_ip_cidr
}

module "compute" {
  source = "./modules/compute"
  ami_id             = var.ami_id
  key_pair_name      = var.key_pair_name
  public_subnet_id   = module.networking.public_subnet_id
  sg_id              = module.networking.sg_id
}

module "storage" {
  source = "./modules/storage"
  vpc_id           = module.networking.vpc_id
  route_table_id   = module.networking.route_table_id
}

module "secrets" {
  source = "./modules/secrets"
}

module "scheduler" {
  source = "./modules/scheduler"
  ec2_instance_id = module.compute.instance_id
}

module "dns" {
  source = "./modules/dns"
  elastic_ip = module.compute.elastic_ip
}
```

**infra/variables.tf**
```hcl
variable "aws_region"    { default = "ap-southeast-1" }
variable "vpc_cidr"      { default = "10.0.0.0/16" }
variable "my_ip_cidr"    { description = "SSH 허용 IP (본인 IP/32)" }
variable "ami_id"        { description = "EC2 AMI ID (콘솔 확인)" }
variable "key_pair_name" { description = "EC2 Key Pair 이름" }
```

**infra/terraform.tfvars** (gitignore됨 — 실제 값 기입)
```hcl
my_ip_cidr     = "X.X.X.X/32"       # 본인 IP
ami_id         = "ami-XXXXXXXXXX"    # 콘솔에서 확인
key_pair_name  = "qusign-key"        # 콘솔에서 확인
```

---

## Phase 2: terraform import 실행 (2일)

> **규칙**: 한 리소스 import → terraform plan → diff 0 확인 → 다음 리소스  
> diff가 남아 있으면 HCL을 콘솔 값에 맞게 수정하고 반복

```bash
cd infra
terraform init

# ── 1. networking 모듈 ──────────────────────────────────
terraform import module.networking.aws_vpc.main                      vpc-__________
terraform import module.networking.aws_subnet.public                 subnet-__________
terraform import module.networking.aws_internet_gateway.main         igw-__________
terraform import module.networking.aws_route_table.public            rtb-__________
terraform import module.networking.aws_route_table_association.public rtb-__________ # association ID 별도
terraform import module.networking.aws_security_group.main           sg-__________

# ── 2. compute 모듈 ─────────────────────────────────────
terraform import module.compute.aws_instance.app                     i-__________________
terraform import module.compute.aws_eip.app                          eipalloc-__________
terraform import module.compute.aws_eip_association.app              eipassoc-__________
terraform import module.compute.aws_iam_role.ec2_role                qusign_ec2_role
terraform import module.compute.aws_iam_instance_profile.ec2         qusign_ec2_role

# ── 3. storage 모듈 ─────────────────────────────────────
terraform import module.storage.aws_s3_bucket.documents              qusign_documents_prod
terraform import module.storage.aws_s3_bucket_versioning.documents   qusign_documents_prod
terraform import module.storage.aws_s3_bucket_server_side_encryption_configuration.documents qusign_documents_prod
terraform import module.storage.aws_s3_bucket_public_access_block.documents qusign_documents_prod
terraform import module.storage.aws_ecr_repository.backend           qusign_backend
terraform import module.storage.aws_vpc_endpoint.s3                  vpce-__________

# ── 4. secrets 모듈 ─────────────────────────────────────
terraform import module.secrets.aws_ssm_parameter.db_password        /qusign/prod/db-password
terraform import module.secrets.aws_ssm_parameter.jwt_secret         /qusign/prod/jwt-secret
terraform import module.secrets.aws_ssm_parameter.s3_bucket          /qusign/prod/s3-bucket
terraform import module.secrets.aws_ssm_parameter.cors_origins       /qusign/prod/cors-origins
terraform import module.secrets.aws_ssm_parameter.server_url         /qusign/prod/server-url
terraform import module.secrets.aws_ssm_parameter.admin_email        /qusign/prod/admin-email
terraform import module.secrets.aws_ssm_parameter.admin_password     /qusign/prod/admin-password

# ── 5. scheduler 모듈 ───────────────────────────────────
terraform import module.scheduler.aws_lambda_function.ec2_scheduler  qusign_start_instances
terraform import module.scheduler.aws_cloudwatch_event_rule.start    <시작규칙이름>
terraform import module.scheduler.aws_cloudwatch_event_rule.stop     <정지규칙이름>
terraform import module.scheduler.aws_cloudwatch_log_group.lambda    /aws/lambda/qusign_start_instances

# ── 6. dns 모듈 ─────────────────────────────────────────
terraform import module.dns.aws_route53_zone.main                    Z__________________
terraform import module.dns.aws_route53_record.root                  Z__________________.qusign.link.A
```

---

## Phase 3: 모듈별 HCL (실제 값 반영)

### modules/networking/main.tf

```hcl
resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true
  tags = { Name = "qusign_vpc" }
}

resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = "ap-southeast-1a"
  map_public_ip_on_launch = true
  tags = { Name = "qusign-public-subnet" }
}

resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id
  tags   = { Name = "qusign-igw" }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }
  tags = { Name = "qusign-public-rt" }
}

resource "aws_route_table_association" "public" {
  subnet_id      = aws_subnet.public.id
  route_table_id = aws_route_table.public.id
}

resource "aws_security_group" "main" {
  name   = "qusign-ec2-sg"
  vpc_id = aws_vpc.main.id

  ingress {
    description = "HTTPS"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    description = "HTTP (Nginx redirect)"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]   # GitHub Actions 러너 IP가 동적 → 현재 0.0.0.0/0 유지
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  tags = { Name = "qusign-ec2-sg" }
}
```

### modules/compute/main.tf

```hcl
resource "aws_instance" "app" {
  ami                    = var.ami_id
  instance_type          = "t3.small"
  subnet_id              = var.public_subnet_id
  vpc_security_group_ids = [var.sg_id]
  key_name               = var.key_pair_name
  iam_instance_profile   = aws_iam_instance_profile.ec2.name

  tags = { Name = "qusign_app" }

  lifecycle {
    # AMI 업데이트나 user_data 변경이 EC2 재생성 트리거하지 않도록
    ignore_changes = [ami, user_data]
  }
}

resource "aws_eip" "app" {
  domain = "vpc"
  tags   = { Name = "qusign-eip" }
}

resource "aws_eip_association" "app" {
  instance_id   = aws_instance.app.id
  allocation_id = aws_eip.app.id
}

resource "aws_iam_role" "ec2_role" {
  name = "qusign_ec2_role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })
}

resource "aws_iam_instance_profile" "ec2" {
  name = "qusign_ec2_role"
  role = aws_iam_role.ec2_role.name
}
```

### modules/storage/main.tf

```hcl
resource "aws_s3_bucket" "documents" {
  bucket = "qusign_documents_prod"
  tags   = { Name = "qusign-documents" }
}

resource "aws_s3_bucket_versioning" "documents" {
  bucket = aws_s3_bucket.documents.id
  versioning_configuration { status = "Disabled" }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "documents" {
  bucket = aws_s3_bucket.documents.id
  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_public_access_block" "documents" {
  bucket                  = aws_s3_bucket.documents.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_ecr_repository" "backend" {
  name                 = "qusign_backend"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration { scan_on_push = false }
}

resource "aws_vpc_endpoint" "s3" {
  vpc_id            = var.vpc_id
  service_name      = "com.amazonaws.ap-southeast-1.s3"
  vpc_endpoint_type = "Gateway"
  route_table_ids   = [var.route_table_id]
  tags = { Name = "qusign-s3-endpoint" }
}
```

### modules/secrets/main.tf

```hcl
# SecureString 파라미터는 value를 Terraform으로 관리하지 않는다.
# import 후 ignore_changes = [value]로 콘솔 값 유지.

resource "aws_ssm_parameter" "db_password" {
  name  = "/qusign/prod/db-password"
  type  = "SecureString"
  value = "PLACEHOLDER"   # import 후 아래 lifecycle이 실제 값 보호
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
  value = "qusign_documents_prod"
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
```

### modules/scheduler/main.tf

```hcl
resource "aws_lambda_function" "ec2_scheduler" {
  function_name = "qusign_start_instances"
  runtime       = "python3.13"
  handler       = "lambda_function.lambda_handler"
  role          = var.lambda_role_arn

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

resource "aws_cloudwatch_event_rule" "start" {
  name                = "qusign-ec2-start"
  schedule_expression = "cron(0 0 * * ? *)"   # UTC 00:00 = KST 09:00
  description         = "KST 09:00 EC2 시작"
}

resource "aws_cloudwatch_event_rule" "stop" {
  name                = "qusign-ec2-stop"
  schedule_expression = "cron(30 12 * * ? *)"  # UTC 12:30 = KST 21:30
  description         = "KST 21:30 EC2 정지"
}

resource "aws_cloudwatch_event_target" "start" {
  rule      = aws_cloudwatch_event_rule.start.name
  target_id = "StartEC2"
  arn       = aws_lambda_function.ec2_scheduler.arn
}

resource "aws_cloudwatch_event_target" "stop" {
  rule      = aws_cloudwatch_event_rule.stop.name
  target_id = "StopEC2"
  arn       = aws_lambda_function.ec2_scheduler.arn
}
```

### modules/dns/main.tf

```hcl
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
```

---

## Phase 4: 검증 체크리스트

### 중간 검증 (import 완료 후)

```bash
terraform validate          # 문법 오류 없음
terraform plan              # ← 반드시 "No changes. Your infrastructure matches the configuration." 확인
```

| 항목 | 기준 | 확인 |
|---|---|---|
| `terraform init` | 에러 없음 | [ ] |
| `terraform validate` | 통과 | [ ] |
| `terraform plan` | **Changes: 0** | [ ] |
| state 파일 위치 | S3 `qusign-terraform-state/prod/terraform.tfstate` | [ ] |
| state 파일 암호화 | SSE 활성화 | [ ] |
| DynamoDB lock | `qusign-terraform-lock` 테이블 존재 | [ ] |
| 운영 서버 | plan 전후 `curl https://qusign.link/actuator/health` 200 | [ ] |

### 최종 검증 (재현성 테스트)

```bash
# dev 계정 또는 별도 워크스페이스에서
terraform workspace new dev
terraform apply -var-file=dev.tfvars   # 새 인프라 생성
terraform destroy                      # 정리
```

---

## 주의사항 & 트러블슈팅

| 상황 | 대응 |
|---|---|
| `plan`에 diff 잔존 | 콘솔 값 vs HCL 1:1 대조. `terraform state show <resource>`로 현재 state 확인 |
| SecureString 값 diff | `ignore_changes = [value]` 추가 |
| EC2 재생성 위험 | `lifecycle { ignore_changes = [ami, user_data] }` 확인 |
| SG 22포트 diff | 현재 `0.0.0.0/0` 유지 (GitHub Actions IP 동적). 추후 IP 제한 개선 가능 |
| Lambda zip 없음 | `lambda_placeholder.zip` 빈 파일로 생성 후 `ignore_changes` 처리 |
| import 실패 (Not Found) | Phase 0에서 수집한 ID 재확인. 리소스 이름 ≠ ID |
| state lock 오류 | 이전 apply 비정상 종료 시 `terraform force-unlock <lock-id>` |

---

## 작업 일정

| 일차 | 작업 |
|---|---|
| Day 1 오전 | Phase 0 — 콘솔에서 ID 전수 수집 |
| Day 1 오후 | Phase 1 — 디렉토리 구조 + 파일 생성 + `terraform init` |
| Day 2 | Phase 2 — networking / compute 모듈 import + plan no-changes |
| Day 3 | Phase 2 — storage / secrets / scheduler / dns 모듈 import + plan no-changes |
| Day 4 | Phase 4 — 검증, PLAN.md 체크 완료 표시 |

---

## 완료 기준

- [ ] `terraform plan` → `No changes` (모든 모듈)
- [ ] state 파일이 S3에 암호화 저장
- [ ] `terraform plan` 실행 후에도 `https://qusign.link` 정상 응답
- [ ] `infra/` 디렉토리가 `develop` 브랜치에 커밋됨
- [ ] PLAN.md 7-1 항목 전체 체크
