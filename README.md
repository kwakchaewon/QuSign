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
| 인프라 | Docker Compose → AWS EC2/RDS → Terraform |

---

## 로컬 실행

**사전 준비**
- JDK 21
- MariaDB 10.11 (root/root, database: qusign)
- Docker (MinIO 실행용)

```bash
# 1. MinIO 기동
docker compose up -d

# 2. 앱 실행 (local 프로파일)
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'
```

**엔드포인트**
- API: http://localhost:8080
- MinIO 콘솔: http://localhost:9001 (minioadmin / minioadmin)

---

## 테스트

```bash
cd backend
./gradlew test
```

---

## 진행 현황

| 단계 | 내용 | 상태 |
|------|------|------|
| 1단계 | 환경 세팅 + PQC 핵심 검증 | 🔄 진행 중 |
| 2단계 | 백엔드 핵심 구현 | 🔄 진행 중 |
| 3단계 | 프론트엔드 + 이메일 연동 | ⬜ |
| 4단계 | AWS 배포 + GitHub Actions | ⬜ |
| 5단계 | Terraform + 수익화 | ⬜ |
| 6단계 | Loki + Grafana + 이직 준비 | ⬜ |

세부 계획 → [docs/exec-plans/PLAN.md](docs/exec-plans/PLAN.md)
