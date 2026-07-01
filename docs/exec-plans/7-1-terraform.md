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
| VPC | qusign_vpc-vpc / 10.0.0.0/16 | |
| Subnet | Public(10.0.0.0/20) + Private(10.0.128.0/20), 둘 다 ap-southeast-1a | ⚠️ 최초 작성 시 Public만 기재됐던 오류 정정 |
| Route Table | public / private / main(미사용) 총 3개 | |
| Security Group | qusign_ec2_sg, 22(0.0.0.0/0), 80, 443 inbound | |
| S3 | qusign-documents-prod | SSE-S3, 퍼블릭 차단 |
| S3 VPC Endpoint | Gateway type, private RT에 연결 | 무료, 인터넷 미경유 |
| ECR | qusign_backend | |
| SSM (SecureString) | /qusign/prod/db-password, jwt-secret, admin-password | KMS 암호화 |
| SSM (String) | /qusign/prod/s3-bucket, cors-origins, server-url, admin-email | |
| Lambda | qusign_start_instances / Python 3.13 | |
| EventBridge Scheduler | qusign_morning_start / qusign_nightly_stop (일정 그룹: default) | ⚠️ CloudWatch Events Rule 아님, EventBridge Scheduler로 정정 |
| IAM Role | qusign_ec2_role | S3·SES·SSM·ECR 접근 |
| IAM User | qusign_github_actions_deployer | ⚠️ Role 아니라 User. 정적 액세스 키로 GitHub Actions 인증 |
| Route 53 | qusign.link → 3.0.193.52 (A), www.qusign.link (CNAME) | Hosted Zone ID: Z05035142H5MR6F494TUU |
| CloudWatch Logs | Lambda 실행 이력 | |

---

## Phase 0: 사전 정보 수집 (0.5일)

AWS 콘솔에서 아래 ID를 메모장에 기록한다. import 명령어에 직접 쓸 값이다.

```
# 수집 항목 (AWS Console → 각 서비스 페이지)

VPC   (아래 값은 aws cli describe-* 명령으로 전량 확인 완료 ✅)
  VPC ID:          vpc-0ad2925f2b2784bd1        ✅ (Name 태그: qusign_vpc-vpc — 상단 요약표의 "qusign_vpc"와 다름, 실제 이름 반영 필요)
  IGW ID:          igw-042a4e36b7532cc61        ✅ (Name: qusign_vpc-igw)
  Public  Subnet ID: subnet-0a0f2b0e687b5a6f7   ✅ (qusign_vpc-subnet-public1-ap-southeast-1a, 10.0.0.0/20)
  Private Subnet ID: subnet-033858f52fa1dcefb   ✅ (qusign_vpc-subnet-private1-ap-southeast-1a, 10.0.128.0/20)
  Public  RT ID:   rtb-09e26e5286e55bfed        ✅ (qusign_vpc-rtb-public — local + 0.0.0.0/0→IGW, public 서브넷에 연결)
  Private RT ID:   rtb-0e6db2d0bb93e45a8        ✅ (qusign_vpc-rtb-private1-ap-southeast-1a — local + S3 VPC 엔드포인트 라우트, private 서브넷에 연결)
  기본(Main) RT ID: rtb-031428bb52dc8216c       ✅ (이름 태그 없음, main=true, 서브넷 연결 없음 — local 라우트만 존재, 실사용 안 함)
  ⚠️ 계획서는 서브넷 1개/RT 1개만 가정했으나 실제는 public+private 서브넷 각 1개, RT 3개(public/private/main) 구조 — networking 모듈을 아래처럼 확장 필요

EC2
  Instance ID:     i-0447a621521e4fc2d          ✅ (qusign_app)
  Key Pair 이름:   qusign-keypair               ✅ (계획서 예시 "qusign-key"와 실제 다름 — 주의)
  AMI ID:          ami-0dfb1c86c34509daf        ✅ (al2023-ami-2023.12.20260611.0-kernel-6.1-x86_64)

Security Group
  SG ID:           sg-0d6159b3a82a45699          ✅ (qusign_ec2_sg, "EC2 security group for QuSign", 인바운드 3건)
  ※ vpc-0ad2925f2b2784bd1에 default SG(sg-0520217bac47de7c7)도 있으나 미사용으로 추정 — import 대상 아님

Elastic IP
  Allocation ID:   eipalloc-0d6803d4ca4534aaa    ✅ (Name: qusign_app_elasticip)
  Association ID:  eipassoc-00183371ee2d69e32    ✅
  Network Interface ID: eni-0855bac0dbe983c63    ✅ (참고용, HCL에는 불필요)

S3
  버킷 이름:       qusign-documents-prod         ⚠️ 정정: 이 문서 전체의 언더스코어 표기는 오탈자 — S3 버킷명은 언더스코어 사용 불가, 실제로는 하이픈 (아래 일괄 수정함)
  VPC Endpoint ID: vpce-0374390452c0b002c        ✅ (Name: qusign_vpc-vpce-s3, Gateway 타입)
  ⚠️ 연결된 라우팅 테이블: rtb-0e6db2d0bb93e45a8 (private RT) — 계획서는 storage 모듈에 route_table_id를 networking의 단일 public RT로 넘기고 있었으나, 실제로는 private RT에 연결되어 있음. Phase 3 storage 모듈 호출부 수정 필요

ECR
  Repository 이름: qusign_backend                ✅
  Repository URI:  285868221698.dkr.ecr.ap-southeast-1.amazonaws.com/qusign_backend  ✅
  Repository ARN:  arn:aws:ecr:ap-southeast-1:285868221698:repository/qusign_backend  ✅

IAM
  EC2 Role ARN:    arn:aws:iam::285868221698:role/qusign_ec2_role       ✅ (IAM 역할 13개 중 확인됨, 신뢰 대상: ec2)
  Instance Profile 이름: qusign_ec2_role (보통 역할명과 동일)
  ⚠️⚠️ 확정 정정: qusign_github_actions_deployer는 IAM Role이 아니라 IAM User! (aws iam list-users로 확인: kwakchaewon, qusign_cwkwak, qusign_github_actions_deployer)
     User ARN: arn:aws:iam::285868221698:user/qusign_github_actions_deployer
     인증 방식: `.github/workflows/deploy.yml`에서 aws-actions/configure-aws-credentials@v4 + secrets.AWS_ACCESS_KEY_ID (정적 액세스 키). OIDC provider도 미설정(aws iam list-open-id-connect-providers 결과 없음).
     → Phase 3 IAM은 aws_iam_role이 아니라 aws_iam_user로 작성. 액세스 키 시크릿 값은 재조회 불가하므로 aws_iam_access_key는 Terraform으로 관리하지 않고 기존 GitHub Secrets 값 그대로 유지 (import 대상에서 제외)
  ※ 추가 확인된 역할(EC2/GitHub 외): qusign_eventbridge_scheduler_role (신뢰 대상: scheduler), qusign_lambda_eventbridge_role (신뢰 대상: lambda) — 계획서 미기재, scheduler 모듈 IAM에 반영 필요

Lambda
  Function ARN:    arn:aws:lambda:ap-southeast-1:285868221698:function:qusign_start_instances  ✅ (실제 콘솔 값과 일치 확인)
  실행 Role:       qusign_lambda_eventbridge_role로 추정 (Lambda 콘솔 '구성 > 권한' 탭에서 재확인 필요)

EventBridge
  ⚠️⚠️ 확정: "버스 > 규칙"에는 규칙 없음(규칙 없음, 계정 예약 규칙은 Scheduler로 이동됨 배너 확인) — CloudWatch Events Rule 방식 아님!
  실제로는 EventBridge Scheduler(aws_scheduler_schedule) 사용 중:
    일정 이름: qusign_morning_start   ✅ (일정 그룹: default, 상태: 활성, 대상: qusign_start_instances Lambda, LAMBDA_Invoke)
    일정 이름: qusign_nightly_stop    ✅ (일정 그룹: default, 상태: 활성, 대상: qusign_start_instances Lambda, LAMBDA_Invoke)
  → Phase 3 scheduler 모듈 HCL을 aws_cloudwatch_event_rule/aws_cloudwatch_event_target에서 aws_scheduler_schedule로 전면 수정 필요 (아래 Phase 3 절 참고)

Route 53
  Hosted Zone ID:  Z05035142H5MR6F494TUU          ✅
  A 레코드 이름:   qusign.link                    ✅ (A → 3.0.193.52, TTL 300)
  ※ 실제 레코드 4개: A(qusign.link), NS(4개 네임서버), SOA, CNAME(www.qusign.link → qusign.link) — 계획서 dns 모듈에 www CNAME 레코드 import/코드화 누락, 추가 필요

계정 ID: 285868221698                            ✅ (kwakchaewon / qusign_cwkwak)
```

---

## Phase 1: Terraform 환경 세팅 (1일)

### 1-1. state 전용 S3 버킷 생성 (콘솔 1회)

```bash
# 앱 버킷(qusign-documents-prod)과 별도로 생성
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

aws s3api put-public-access-block \
  --bucket qusign-terraform-state \
  --public-access-block-configuration BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true
```

⚠️⚠️ **정정 (2026-07-01)**: 초안은 DynamoDB 락 테이블(`qusign-terraform-lock`)도 함께 만들도록 했으나, `terraform init` 실행 결과 `dynamodb_table` 파라미터가 **Deprecated**임을 확인했다. Terraform 1.10+(S3 backend)는 S3 자체 조건부 쓰기 기반 잠금(`use_lockfile = true`)을 지원해 DynamoDB 없이도 동일한 잠금 효과를 얻을 수 있다. 이 프로젝트는 혼자 운영하는 사이드 프로젝트라 DynamoDB의 실전 검증 이력이 큰 의미가 없고, 관리 리소스와 IAM 권한을 하나 줄일 수 있어 `use_lockfile` 방식으로 전환했다(아래 backend.tf 참고). DynamoDB 테이블을 이미 만들었다면 더 이상 쓰이지 않으니 삭제해도 무방하다.

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
        ├── iam/
        │   └── main.tf          ← github_actions_deployer User (변수/출력 불필요할 만큼 단순)
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
```
※ `.terraform.lock.hcl`은 provider 버전 고정을 위해 커밋하는 게 Terraform 표준 관행 — 초안의 gitignore 대상 지정은 정정함

**infra/backend.tf**
```hcl
terraform {
  backend "s3" {
    bucket       = "qusign-terraform-state"
    key          = "prod/terraform.tfstate"
    region       = "ap-southeast-1"
    encrypt      = true
    use_lockfile = true   # S3 조건부 쓰기 기반 잠금 — DynamoDB 불필요
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
  vpc_cidr = var.vpc_cidr
}

module "compute" {
  source = "./modules/compute"
  ami_id             = var.ami_id
  key_pair_name      = var.key_pair_name
  public_subnet_id   = module.networking.public_subnet_id
  sg_id              = module.networking.sg_id
}

module "iam" {
  source = "./modules/iam"   # qusign_github_actions_deployer는 Role이 아니라 User — 별도 모듈로 분리
}

module "storage" {
  source = "./modules/storage"
  vpc_id                = module.networking.vpc_id
  private_route_table_id = module.networking.private_route_table_id  # S3 엔드포인트는 private RT에 연결됨 (public 아님)
}

module "secrets" {
  source = "./modules/secrets"
}

module "scheduler" {
  source = "./modules/scheduler"
  ec2_instance_id = module.compute.instance_id
  # qusign_lambda_eventbridge_role / qusign_eventbridge_scheduler_role은 기존에 콘솔로 만들어진
  # 별도 관리 리소스라 이 Phase에서 import하지 않고, scheduler 모듈 내부에서 data source로 조회한다.
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
variable "ami_id"        { description = "EC2 AMI ID (콘솔 확인)" }
variable "key_pair_name" { description = "EC2 Key Pair 이름" }
```
※ `my_ip_cidr`는 초안에 있었으나 실제 SG는 SSH(22)를 `0.0.0.0/0`으로 하드코딩하고 있어 미사용 — 제거함 (추후 IP 제한 시 재도입)

**infra/terraform.tfvars** (gitignore됨 — 실제 값 기입)
```hcl
ami_id         = "ami-0dfb1c86c34509daf"  # 콘솔 확인값
key_pair_name  = "qusign-keypair"         # 콘솔 확인값 (초안의 "qusign-key"는 오타)
```

---

## Phase 2: terraform import 실행 (2일)

> **규칙**: 한 리소스 import → terraform plan → diff 0 확인 → 다음 리소스  
> diff가 남아 있으면 HCL을 콘솔 값에 맞게 수정하고 반복

```bash
cd infra
terraform init

# ── 1. networking 모듈 ──────────────────────────────────
terraform import module.networking.aws_vpc.main                       vpc-0ad2925f2b2784bd1
terraform import module.networking.aws_subnet.public                  subnet-0a0f2b0e687b5a6f7
terraform import module.networking.aws_subnet.private                 subnet-033858f52fa1dcefb
terraform import module.networking.aws_internet_gateway.main          igw-042a4e36b7532cc61
terraform import module.networking.aws_route_table.public             rtb-09e26e5286e55bfed
terraform import module.networking.aws_route_table.private            rtb-0e6db2d0bb93e45a8
terraform import module.networking.aws_route_table_association.public  rtbassoc-066d67498e883a475
terraform import module.networking.aws_route_table_association.private rtbassoc-0fe5f35b38830f8f5
terraform import module.networking.aws_security_group.main            sg-0d6159b3a82a45699

# ── 2. compute 모듈 ─────────────────────────────────────
terraform import module.compute.aws_instance.app                     i-0447a621521e4fc2d
terraform import module.compute.aws_eip.app                          eipalloc-0d6803d4ca4534aaa
terraform import module.compute.aws_eip_association.app              eipassoc-00183371ee2d69e32
terraform import module.compute.aws_iam_role.ec2_role                qusign_ec2_role
terraform import module.compute.aws_iam_instance_profile.ec2         qusign_ec2_role
# GitHub Actions 배포용은 Role이 아니라 User — compute 모듈이 아닌 iam 모듈(신규) 또는 별도 파일로 분리 권장
terraform import module.iam.aws_iam_user.github_actions_deployer     qusign_github_actions_deployer
# ※ aws_iam_access_key는 시크릿 값 재조회 불가 — Terraform 관리 대상에서 제외, 기존 GitHub Secrets 값 유지

# ── 3. storage 모듈 ─────────────────────────────────────
terraform import module.storage.aws_s3_bucket.documents              qusign-documents-prod
terraform import module.storage.aws_s3_bucket_versioning.documents   qusign-documents-prod
terraform import module.storage.aws_s3_bucket_server_side_encryption_configuration.documents qusign-documents-prod
terraform import module.storage.aws_s3_bucket_public_access_block.documents qusign-documents-prod
terraform import module.storage.aws_ecr_repository.backend           qusign_backend
terraform import module.storage.aws_vpc_endpoint.s3                  vpce-0374390452c0b002c

# ── 4. secrets 모듈 ─────────────────────────────────────
terraform import module.secrets.aws_ssm_parameter.db_password        /qusign/prod/db-password
terraform import module.secrets.aws_ssm_parameter.jwt_secret         /qusign/prod/jwt-secret
terraform import module.secrets.aws_ssm_parameter.s3_bucket          /qusign/prod/s3-bucket
terraform import module.secrets.aws_ssm_parameter.cors_origins       /qusign/prod/cors-origins
terraform import module.secrets.aws_ssm_parameter.server_url         /qusign/prod/server-url
terraform import module.secrets.aws_ssm_parameter.admin_email        /qusign/prod/admin-email
terraform import module.secrets.aws_ssm_parameter.admin_password     /qusign/prod/admin-password

# ── 5. scheduler 모듈 ───────────────────────────────────
# ⚠️ 확정: CloudWatch Events Rule이 아니라 EventBridge Scheduler(aws_scheduler_schedule) 사용 중. import 대상 전면 변경.
terraform import module.scheduler.aws_lambda_function.ec2_scheduler  qusign_start_instances
terraform import module.scheduler.aws_scheduler_schedule.morning_start default/qusign_morning_start
terraform import module.scheduler.aws_scheduler_schedule.nightly_stop  default/qusign_nightly_stop
terraform import module.scheduler.aws_cloudwatch_log_group.lambda    /aws/lambda/qusign_start_instances

# ── 6. dns 모듈 ─────────────────────────────────────────
terraform import module.dns.aws_route53_zone.main                    Z05035142H5MR6F494TUU
terraform import module.dns.aws_route53_record.root                  Z05035142H5MR6F494TUU_qusign.link_A
terraform import module.dns.aws_route53_record.www                   Z05035142H5MR6F494TUU_www.qusign.link_CNAME
```

---

## Phase 3: 모듈별 HCL (실제 값 반영)

### modules/networking/main.tf

```hcl
# ⚠️ 계획서 초안(단일 퍼블릭 서브넷)에서 실제 구조(public+private 각 1개, RT 3개)로 전면 수정됨.
# main 라우팅 테이블(rtb-031428bb52dc8216c)은 서브넷 연결 없이 방치 상태 — Terraform에서는
# aws_vpc의 default_route_table_id로 참조만 하고 별도 리소스로 관리하지 않는 것을 권장 (아래는 미포함).

resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true
  tags = { Name = "qusign_vpc-vpc" }
}

resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = "10.0.0.0/20"
  availability_zone       = "ap-southeast-1a"
  map_public_ip_on_launch = false   # 콘솔 확인값 그대로 — EIP를 통해서만 퍼블릭 접근
  tags = { Name = "qusign_vpc-subnet-public1-ap-southeast-1a" }
}

resource "aws_subnet" "private" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.128.0/20"
  availability_zone = "ap-southeast-1a"
  tags = { Name = "qusign_vpc-subnet-private1-ap-southeast-1a" }
}

resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id
  tags   = { Name = "qusign_vpc-igw" }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }
  tags = { Name = "qusign_vpc-rtb-public" }
}

# S3 게이트웨이 엔드포인트 라우트는 storage 모듈의 aws_vpc_endpoint.s3에서
# route_table_ids로 이 RT를 지정하는 방식으로 부여됨 (여기선 local 라우트만 암묵 포함)
resource "aws_route_table" "private" {
  vpc_id = aws_vpc.main.id
  tags = { Name = "qusign_vpc-rtb-private1-ap-southeast-1a" }
}

resource "aws_route_table_association" "public" {
  subnet_id      = aws_subnet.public.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "private" {
  subnet_id      = aws_subnet.private.id
  route_table_id = aws_route_table.private.id
}

resource "aws_security_group" "main" {
  name        = "qusign_ec2_sg" # 실제 콘솔명 확인됨 (하이픈 아님)
  description = "EC2 security group for QuSign" # ⚠️⚠️ 필수: description 생략 시 기본값("Managed by Terraform")으로 바뀌며 SG 강제 재생성(destroy+recreate) 유발 — import 직후 plan에서 실제로 겪음
  vpc_id      = aws_vpc.main.id

  # 실제 콘솔 인바운드 규칙엔 description이 설정돼 있지 않음 — 일치시키기 위해 생략(추가하면 diff 발생)
  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
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
  tags = { Name = "qusign_ec2_sg" }
}
```

⚠️⚠️ **실전에서 겪은 위험 사례 (2026-07-01)**: networking 모듈 import 직후 `terraform plan`에서 `aws_security_group.main`이 "must be replaced"로 나왔다. 원인은 `description`을 HCL에 안 적어서 Terraform 기본값("Managed by Terraform")으로 채우려 했기 때문 — `aws_security_group`의 `description`은 **변경 시 무조건 리소스 전체를 삭제 후 재생성(ForceNew)**하는 필드다. 이 상태로 `apply`했다면 운영 중인 EC2에 붙어있는 보안 그룹이 삭제되면서 SSH/HTTP/HTTPS 접속이 끊겼을 것이다. `aws ec2 describe-security-groups`로 실제 description(`"EC2 security group for QuSign"`)을 확인해 HCL에 반영한 뒤 `plan`이 "No changes"로 확인됐다. **교훈**: import 후 `plan`을 반드시 확인하고, diff에 `# forces replacement` 표시가 있으면 절대 그대로 apply하지 말 것.

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
  tags   = { Name = "qusign_app_elasticip" } # 실제 콘솔 태그명으로 정정
}

resource "aws_eip_association" "app" {
  instance_id   = aws_instance.app.id
  allocation_id = aws_eip.app.id
}

resource "aws_iam_role" "ec2_role" {
  name        = "qusign_ec2_role"
  description = "EC2 instance role for QuSign app" # ⚠️ import 후 plan diff로 확인된 실제 값 (없으면 in-place update 발생 — 강제 재생성은 아님)
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
      Action    = "sts:AssumeRole"
    }]
  })

  tags = {
    Environment = "prod"
    ManagedBy   = "manual" # 콘솔에서 붙인 기존 태그 그대로 유지 (import 시 값 일치 목적, 의미상 갱신은 별도 작업)
    Owner       = "kwakchaewon"
    Project     = "qusign"
  }
}

resource "aws_iam_instance_profile" "ec2" {
  name = "qusign_ec2_role"
  role = aws_iam_role.ec2_role.name
}
```

### modules/iam/main.tf

```hcl
# GitHub Actions 배포용 — 콘솔 확인 결과 Role이 아니라 User (OIDC 미설정, 정적 액세스 키 방식)
resource "aws_iam_user" "github_actions_deployer" {
  name = "qusign_github_actions_deployer"
}

# 액세스 키(aws_iam_access_key)는 시크릿 값이 생성 시점에만 노출되고 재조회 불가하므로
# Terraform으로 새로 만들지 않는다. 기존 GitHub Secrets(AWS_ACCESS_KEY_ID/AWS_SECRET_ACCESS_KEY)
# 값을 그대로 유지하고, 이 User 리소스는 IAM 정책/이름만 코드로 관리한다.
# TODO(장기 개선): OIDC 기반 aws_iam_role + aws_iam_openid_connect_provider로 전환해
# 정적 액세스 키 자체를 제거하는 것을 8단계 이후 검토 (현재는 범위 밖)
```

### modules/storage/main.tf

```hcl
resource "aws_s3_bucket" "documents" {
  bucket = "qusign-documents-prod"
  # 실제 버킷엔 태그가 없음 — 일치시키기 위해 생략 (태그 추가는 별도 의도적 변경으로 취급)
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

  image_scanning_configuration { scan_on_push = true } # 실제 콘솔 값 (초안의 false는 오탈자)
}

resource "aws_vpc_endpoint" "s3" {
  vpc_id            = var.vpc_id
  service_name      = "com.amazonaws.ap-southeast-1.s3"
  vpc_endpoint_type = "Gateway"
  route_table_ids   = [var.private_route_table_id]   # 실제 콘솔 확인: private RT에 연결됨 (public 아님)
  tags = { Name = "qusign_vpc-vpce-s3" }
}
```

⚠️ import 후 `plan`에서 확인된 정정: S3 버킷은 실제로 태그가 전혀 없어 `tags` 블록 제거, ECR `scan_on_push`는 실제로 `true`.

### modules/secrets/main.tf

```hcl
# SecureString 파라미터는 value를 Terraform으로 관리하지 않는다.
# import 후 ignore_changes = [value]로 콘솔 값 유지.

resource "aws_ssm_parameter" "db_password" {
  name        = "/qusign/prod/db-password"
  description = "QuSign 프로덕션 MariaDB 비밀번호" # import 후 plan에서 확인된 실제 값
  type        = "SecureString"
  value       = "PLACEHOLDER"   # import 후 아래 lifecycle이 실제 값 보호
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
```

### modules/scheduler/main.tf

```hcl
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
```

⚠️⚠️ **import 후 발견된 대규모 정정**: 계획 초안의 스케줄 시각(UTC 환산)·Lambda 런타임(3.13)·로그 보존기간(14일)·환경변수(INSTANCE_ID) 전부 실제와 달랐다. `ec2_instance_id` 변수는 이제 아무 데도 쓰이지 않아 모듈 변수 자체를 제거했다. 실제 Lambda는 환경변수가 아니라 EventBridge Scheduler의 target `input` JSON(`{"action":"start"}`/`{"action":"stop"}`)으로 동작을 구분한다.

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

# 콘솔에 이미 존재 (계획서 최초 작성 시 누락됨) — import 필수
resource "aws_route53_record" "www" {
  zone_id = data.aws_route53_zone.main.zone_id
  name    = "www.qusign.link"
  type    = "CNAME"
  ttl     = 300
  records = ["qusign.link"]
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
| `terraform init` | 에러 없음 | [x] ✅ (2026-07-01, backend 포함) |
| `terraform validate` | 통과 | [x] ✅ (2026-07-01, backend 제외 상태에서 확인) |
| `terraform plan` | **Changes: 0** | [x] ✅ (2026-07-01, 24개 리소스 전량 import 후 "No changes" 확인) |
| state 파일 위치 | S3 `qusign-terraform-state/prod/terraform.tfstate` | [x] ✅ (2026-07-01) |
| state 파일 암호화 | SSE 활성화 | [x] ✅ (2026-07-01, 버킷 생성 시 적용) |
| S3 자체 잠금(`use_lockfile`) | 정상 동작 | [x] ✅ (2026-07-01, import 전 구간 동안 정상 동작, 락 충돌 없음) |
| 운영 서버 | plan 전후 `curl https://qusign.link/actuator/health` 200 | [x] ✅ (2026-07-01, 200 확인) |

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
| Git Bash에서 `/qusign/prod/...` 같은 SSM 경로 import 시 `C:/Program Files/Git/qusign/...`로 깨짐 | MSYS/Git Bash의 자동 POSIX 경로 변환 때문. `export MSYS_NO_PATHCONV=1` 설정 후 재시도 |
| provider가 `runtime = "python3.14"` 같은 최신 값을 "expected ... got python3.14" 에러로 거부 | 설치된 aws provider 버전이 아직 그 enum을 모름. HCL엔 provider가 아는 유효값을 적고 `lifecycle { ignore_changes = [runtime] }`로 우회 |
| `aws_scheduler_schedule`의 `input` 필드가 `jsonencode()`로 써도 계속 "whitespace changes" diff | AWS가 저장한 원본 문자열의 공백(`{"action": "start"}`, 콜론 뒤 공백)과 `jsonencode()` 출력(`{"action":"start"}`)이 byte 단위로 다름 → 리터럴 문자열로 정확히 맞추면 해결 |
| `aws ec2 describe-security-groups` 등으로 얻은 실제 값과 diff 방향이 헷갈릴 때 | `plan` 출력은 `현재상태(state/실제) -> 원하는값(HCL)` 순서. 처음엔 반대로 착각해서 `maximum_retry_attempts`를 0→185로 잘못 고쳤다가 재확인 후 정정한 사례 있음 |

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

- [x] `terraform plan` → `No changes` (모든 모듈) ✅ (2026-07-01)
- [x] state 파일이 S3에 암호화 저장 ✅ (2026-07-01)
- [x] `terraform plan` 실행 후에도 `https://qusign.link` 정상 응답 ✅ (2026-07-01)
- [ ] `infra/` 디렉토리가 `develop` 브랜치에 커밋됨 (커밋 예정)
- [ ] PLAN.md 7-1 항목 전체 체크 (재현성 테스트 등 일부 남음)
