# AWS 핵심 서비스 — IAM · EC2 · S3 · Route53 (6단계)

> PLAN.md §6-1~6-7, 6-10 대응 — QuSign 배포 인프라의 핵심 AWS 서비스

---

## IAM (Identity and Access Management)

### AWS 보안 모델 — 공동 책임

```
AWS 책임:  물리 데이터센터 보안, 하이퍼바이저, 글로벌 네트워크
고객 책임: IAM 설정, 보안 그룹, 데이터 암호화, 애플리케이션 보안
```

### 최소 권한 원칙

모든 IAM 엔티티(사용자·역할)는 필요한 권한만 부여받아야 합니다.

```
루트 계정     ← 도메인 구매·결제·IAM 초기 설정만 사용. 평소 절대 사용 금지
IAM 사용자    ← 실제 작업 (EC2·S3·SES 등) — 사람이 직접 사용
IAM 역할      ← AWS 서비스가 다른 서비스에 접근할 때 사용 (예: EC2 → S3)
IAM 그룹      ← 여러 사용자에게 동일 정책 적용 편의 단위
```

### 신뢰 정책 vs 권한 정책

IAM 역할은 두 가지 정책으로 구성됩니다.

```json
// 신뢰 정책 — "누가 이 역할을 맡을 수 있는가"
{
  "Statement": [{
    "Effect": "Allow",
    "Principal": { "Service": "ec2.amazonaws.com" },
    "Action": "sts:AssumeRole"
  }]
}
```

```json
// 권한 정책 — "이 역할로 무엇을 할 수 있는가"
{
  "Statement": [
    { "Effect": "Allow", "Action": "s3:*", "Resource": "arn:aws:s3:::qusign-*" },
    { "Effect": "Allow", "Action": "ssm:GetParameter", "Resource": "/qusign/*" }
  ]
}
```

### QuSign IAM 구성

| 엔티티 | 권한 | 용도 |
|---|---|---|
| `qusign_ec2_role` (역할) | S3FullAccess, SESFullAccess, SSMReadOnly, ECRReadOnly | EC2 인스턴스가 AWS 서비스 접근 |
| `qusign_github_actions_deployer` (사용자) | ECRFullAccess, SSMFullAccess, EC2StartStop | GitHub Actions 배포 |
| `qusign_lambda_eventbridge_role` (역할) | EC2StartStop | Lambda가 EC2 제어 |

### EC2 인스턴스 프로파일 — IMDS 자동 자격증명

EC2에 IAM 역할을 붙이면 EC2 내부의 `169.254.169.254`(IMDS)에서 임시 자격증명을 자동 획득합니다.

```
EC2 내부 코드
  → AWS SDK: "자격증명이 필요합니다"
  → IMDS(http://169.254.169.254/latest/meta-data/iam/security-credentials/)
  → 임시 AccessKey + SecretKey + SessionToken (1시간 유효, 자동 갱신)
  → S3/SSM API 호출
```

```kotlin
// StorageConfig.kt — endpoint가 비어있으면 자격증명 제공자 체인이 EC2 IAM 역할까지 자동 탐색
val credentialsProvider = if (endpoint.isBlank()) {
    DefaultCredentialsProvider.create()  // 환경변수 → 시스템 프로퍼티 → ... → EC2 인스턴스 프로파일 순서로 탐색
} else {
    StaticCredentialsProvider.create(     // MinIO 로컬 개발
        AwsBasicCredentials.create(accessKey, secretKey)
    )
}
```
`DefaultCredentialsProvider`는 여러 자격증명 소스를 체인으로 시도하는 provider로, 그중 하나가 EC2 인스턴스 프로파일(IMDS)입니다. `InstanceProfileCredentialsProvider`를 직접 지정할 수도 있지만, QuSign은 로컬/EC2 어디서 실행되든 같은 코드가 동작하도록 `DefaultCredentialsProvider`(체인 방식)를 씁니다.

---

## SSM Parameter Store — 비밀값 관리

`.env` 파일을 서버에 두지 않고 AWS Systems Manager에 암호화 저장합니다.

```
/qusign/prod/db-password    SecureString  ← AES-256 암호화
/qusign/prod/jwt-secret     SecureString
/qusign/prod/s3-bucket      String
/qusign/prod/cors-origins   String
```

GitHub Actions 배포 시 SSM에서 값을 읽어 `.env`로 주입합니다.
EC2에 비밀 파일이 저장되지 않으므로 서버 탈취 시에도 비밀값이 노출되지 않습니다.

---

## EC2 (Elastic Compute Cloud)

### 인스턴스 선택 — t3.small

| 사양 | 값 |
|---|---|
| vCPU | 2 |
| RAM | 2GB |
| 스토리지 | EBS (별도 설정) |
| 비용 | $0.0230/h (ap-southeast-1) |

Spring Boot + MariaDB(Docker) + Redis(Docker)가 2GB RAM 안에서 실행됩니다.
`-Xmx512m` JVM 힙 제한으로 OOM 방지를 권장합니다.

### Elastic IP

EC2를 재시작하면 공인 IP가 바뀝니다. Elastic IP(EIP)는 고정 공인 IP입니다.
Route53 A 레코드에 EIP를 등록하면 재시작해도 도메인이 유지됩니다.

```
qusign.link → A → 3.0.193.52 (Elastic IP)
                   ↓
               EC2 i-0447a621521e4fc2d
```

주의: **EC2가 정지 중(stopped)인데 EIP가 EC2에 연결되어 있으면 과금됩니다** ($0.005/h).
QuSign은 EventBridge 스케줄러로 EC2를 정지하므로 월 ~$1.7의 EIP 요금이 발생합니다.

### 보안 그룹

방화벽 역할을 합니다. 인바운드 규칙에 없는 포트는 차단됩니다.

```
인바운드 22   (SSH)  ← 내 IP만 (보안 그룹에서 IP 직접 지정)
인바운드 80   (HTTP) ← 0.0.0.0/0 (→ 443 리다이렉트)
인바운드 443  (HTTPS)← 0.0.0.0/0
아웃바운드 전체      ← 나가는 트래픽 허용

MariaDB 3306은 127.0.0.1만 바인딩 → 보안 그룹 규칙 불필요
```

---

## S3 (Simple Storage Service)

### 주요 개념

- **버킷**: 파일 저장 공간 (전 세계 유일한 이름)
- **객체**: 저장된 파일 (키 = 경로 형태의 이름)
- **퍼블릭 액세스 차단**: 버킷을 비공개로 유지 (EC2 IAM 역할로만 접근)

### S3 요금 구조

```
저장 비용: $0.023/GB/월 (ap-southeast-1 Standard)
API 비용:  GET $0.0004/1,000건, PUT $0.005/1,000건
데이터 전송:
  └── 인터넷 → S3: 무료
  └── S3 → 인터넷: $0.09/GB
  └── S3 → EC2 (같은 리전, VPC Endpoint): 무료 ← QuSign 적용
```

### S3 일관성 모델 (2020년 이후)

```
PUT 후 GET:    강한 일관성 (즉시 최신 값 반환)
DELETE 후 HEAD: 즉시 404 반환
리스트 후 PUT:  강한 일관성
```

### VPC 엔드포인트 — Gateway vs Interface

```
EC2 → S3 API 호출 시
  ├── VPC 엔드포인트 없음: EC2 → 인터넷 → S3 (데이터 전송 비용 발생)
  └── VPC 엔드포인트 있음: EC2 → AWS 내부망 → S3 (비용 0원)

Gateway Endpoint (S3, DynamoDB 전용): 무료
Interface Endpoint (AWS PrivateLink): $0.01/h — 다른 서비스 지원하지만 비용 발생
```

### 버킷 정책 — EC2 역할만 허용

```json
{
  "Statement": [{
    "Effect": "Allow",
    "Principal": { "AWS": "arn:aws:iam::285868221698:role/qusign_ec2_role" },
    "Action": ["s3:GetObject", "s3:PutObject", "s3:DeleteObject"],
    "Resource": "arn:aws:s3:::qusign-documents-prod-285868221698/*"
  }]
}
```

---

## Route53 — DNS

### DNS 해석 과정

```
사용자: qusign.link 접속
  1. OS 캐시 확인
  2. 로컬 DNS 리졸버 (ISP 또는 8.8.8.8)
  3. 루트 NS → .link TLD NS → Route53 NS
  4. Route53: qusign.link → 3.0.193.52 (A 레코드)
  5. 브라우저: 3.0.193.52:443 TCP 연결
```

### DNS 레코드 타입

| 타입 | 용도 | QuSign 예 |
|---|---|---|
| A | 도메인 → IPv4 | qusign.link → 3.0.193.52 |
| CNAME | 도메인 → 다른 도메인 | www → qusign.link |
| MX | 이메일 수신 서버 | SES 메일 수신 |
| TXT | 문자열 메타데이터 | SES DKIM 인증 |

```
Route53 호스팅 영역 (qusign.link)
  ├── A 레코드:     qusign.link     → 3.0.193.52
  ├── CNAME:        www.qusign.link → qusign.link
  └── CNAME (SES):  _dkim._domainkey.qusign.link → ... (이메일 인증)
```

### TTL 전략

```
평소: TTL 300s (5분)
IP 변경 예정: 변경 24시간 전에 TTL → 60s로 낮춤
변경 완료 후: TTL 다시 300s

짧은 TTL: 빠른 변경 적용, DNS 조회 빈번
긴 TTL:   DNS 조회 감소, 변경 적용 느림
```

### 도메인 구매 → EC2 연결 흐름

```
1. Route53에서 도메인 구매 → 호스팅 영역 자동 생성
2. EC2 Elastic IP 발급
3. Route53 → A 레코드 → Elastic IP 등록
4. DNS 전파 (보통 수 분, 최대 48시간)
5. nslookup qusign.link 8.8.8.8 로 확인
```

---

## EventBridge + Lambda — 비용 절감 스케줄러

```
KST 09:00 → EventBridge → Lambda(start)  → EC2 시작
KST 21:30 → EventBridge → Lambda(stop)   → EC2 정지
```

EC2 하루 12.5시간 가동 → 월 375h → $8.6 (24시간 가동 대비 33% 절감)

```python
# Lambda 코드
def lambda_handler(event, context):
    action = event.get('action')
    if action == 'start':
        ec2.start_instances(InstanceIds=['i-0447a621521e4fc2d'])
    elif action == 'stop':
        ec2.stop_instances(InstanceIds=['i-0447a621521e4fc2d'])
```

---

## 확인 질문 & 답변

**Q1. EC2 인스턴스 역할(IAM Role)과 IAM 사용자의 차이는?**

> IAM 사용자는 Access Key/Secret Key로 인증하며 사람이 직접 사용합니다. IAM 역할은 신뢰 정책으로 특정 AWS 서비스(예: EC2)가 역할을 맡을 수 있게 설정합니다. EC2에 역할을 붙이면 IMDS(169.254.169.254)에서 1시간마다 자동 갱신되는 임시 자격증명을 발급받습니다. 장기 Access Key가 없으므로 키 유출 위험이 없습니다.

**Q2. S3 버킷명에 언더스코어(`_`)가 안 되는 이유는?**

> DNS 호스트명 규칙(RFC 952) 때문입니다. S3 버킷을 가상 호스팅 방식(`bucket-name.s3.amazonaws.com`)으로 접근할 때 버킷명이 DNS 레이블이 됩니다. DNS 레이블에는 하이픈(`-`)만 허용됩니다. 따라서 S3 버킷명에는 하이픈만 써야 합니다.

**Q3. SSM Parameter Store를 사용하는데 `db-url`과 `db-username`은 왜 SSM에 넣지 않았는가?**

> `docker-compose.prod.yml`에서 MariaDB가 컴포즈 내부 서비스(`mariadb:3306`)로 고정되어 있습니다. 호스트명과 사용자명이 코드에 고정되어 있어 환경마다 다를 값이 없습니다. SSM에 넣어야 할 것은 '환경마다 다른 민감 값'이고, 고정값은 `docker-compose.prod.yml`에 둡니다.

**Q4. EventBridge 시간대를 `Asia/Seoul`로 설정할 때 cron에 KST 값을 직접 쓰는 이유는?**

> EventBridge Scheduler에서 시간대를 `Asia/Seoul`로 선택하면, cron 표현식도 그 시간대 기준으로 해석됩니다. UTC cron(`cron(30 12 * * ? *)`)을 그대로 쓰면 시간대가 이중 적용되어 실제로는 UTC+9+9=UTC+18 시각에 실행되는 버그가 발생합니다. 시간대를 설정했으면 cron 값도 그 시간대(KST) 기준으로 작성해야 합니다.

**Q5. S3 VPC Gateway Endpoint와 Interface Endpoint의 차이는?**

> Gateway Endpoint는 S3와 DynamoDB 전용이며 무료입니다. 라우팅 테이블에 경로를 추가하여 트래픽을 AWS 내부망으로 보냅니다. Interface Endpoint(AWS PrivateLink)는 더 많은 서비스를 지원하지만 ENI(네트워크 인터페이스)를 생성하며 시간당 비용($0.01/h)이 발생합니다. S3 접근에는 Gateway Endpoint가 비용 최적 선택입니다.

**Q6. S3 버킷명에 언더스코어(`_`)가 안 되는 이유는?**

> DNS 호스트명 규칙(RFC 952) 때문입니다. S3 버킷을 가상 호스팅 방식(`bucket-name.s3.amazonaws.com`)으로 접근할 때 버킷명이 DNS 레이블이 됩니다. DNS 레이블에는 하이픈(`-`)만 허용됩니다. 따라서 S3 버킷명에는 하이픈만 써야 합니다.
