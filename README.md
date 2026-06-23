# QuSign

> NIST PQC 표준 ML-DSA 기반 전자서명 SaaS

양자 컴퓨터 시대에 안전한 전자서명 서비스. 기존 RSA/ECDSA 대신 NIST 표준 ML-DSA(CRYSTALS-Dilithium)를 적용한다.

---

## 기술 스택

| 레이어 | 기술 |
|--------|------|
| 백엔드 | Kotlin + Spring Boot 3.5, Gradle Kotlin DSL |
| 암호화 | Bouncy Castle 1.84 (ML-DSA-65), PDFBox 3.x |
| DB | MariaDB 10.11 + Flyway |
| 스토리지 | MinIO (로컬) → AWS S3 (운영) |
| 메시지 | Redis 7 (Pub/Sub + 실시간 알림) |
| 프론트엔드 | Vue 3 + Vite + Pinia + Vue Router + TypeScript |
| 인프라 | Docker Compose → AWS EC2/RDS → Terraform |

---

## 로컬 실행

**사전 준비**
- JDK 21
- MariaDB 10.11 (root/root, database: qusign)
- Docker (MinIO + Redis 실행용)
- Node.js 20+

```bash
# 1. MinIO + Redis 기동
docker compose up -d

# 2. 백엔드 실행 (local 프로파일)
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'

# 3. 프론트엔드 실행
cd frontend
npm install && npm run dev
```

**엔드포인트**
- 프론트엔드: http://localhost:5173
- API: http://localhost:8080
- MinIO 콘솔: http://localhost:9001 (minioadmin / minioadmin)

---

## 테스트

```bash
cd backend
./gradlew test
```

---

## API 문서

**[Swagger UI → https://qusign.link/swagger-ui/index.html](https://qusign.link/swagger-ui/index.html)**

전체 엔드포인트 명세·요청·응답 스키마·Try it out 기능 제공.

---

## 진행 현황

| 단계 | 내용 | 상태 |
|------|------|------|
| 1단계 | 환경 세팅 + PQC 핵심 검증 | ✅ 완료 |
| 2단계 | 백엔드 핵심 구현 | ✅ 완료 |
| 3단계 | 프론트엔드 구현 | ✅ 완료 |
| 4단계 | 기능 고도화 & 품질 강화 | 🔄 진행 중 |
| 5단계 | 보안 취약점 개선 (OWASP Top 10) | ✅ 완료 |
| 6단계 | AWS 배포 + SES + GitHub Actions | ⬜ 진행 전 |
| 7단계 | Terraform + 수익화 | ⬜ 진행 전 |
| 8단계 | Loki + Grafana + 이직 준비 | ⬜ 진행 전 |

세부 계획 → [docs/exec-plans/PLAN.md](docs/exec-plans/PLAN.md)

---

## 아키텍처 다이어그램

### Application Layer Architecture
내부 요청 흐름 · 데이터 레이어 · PQC 암호 · 실시간 알림 · 관측성

![Application Layer Architecture](docs/diagrams/qusign_application_layer.png)

### CI/CD & Automation Pipeline
GitHub Actions 자동 배포 · EventBridge 야간 절전 스케줄러 · Loki + Grafana 모니터링

![CI/CD & Automation Pipeline](docs/diagrams/qusign_cicd_pipeline.png)

### AWS Cloud Infrastructure
ap-southeast-1 (싱가포르) · EC2 단일 구성 · 월 ~$12

![AWS Cloud Infrastructure](docs/diagrams/qusign_aws_infrastructure.png)

<sup>소스 프롬프트: [`harness/diagram-01-application.md`](harness/diagram-01-application.md) · [`harness/diagram-02-aws-infra.md`](harness/diagram-02-aws-infra.md) · [`harness/diagram-03-cicd.md`](harness/diagram-03-cicd.md)</sup>
