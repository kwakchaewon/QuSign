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
| 프론트엔드 | Vue 3 + Vite + Pinia + Vue Router + TypeScript |
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

## 구현된 기능

| 기능 | 엔드포인트 | 비고 |
|------|-----------|------|
| 회원가입 / 로그인 | `POST /api/auth/signup` `POST /api/auth/login` | JWT 인증, ML-DSA 키쌍 자동 생성 |
| PDF 업로드 | `POST /api/documents` | SHA3-256 해시, MinIO 저장 |
| 문서 목록 / 다운로드 | `GET /api/documents` `GET /api/documents/{id}/download` | |
| 서명 요청 생성 | `POST /api/signature-requests` | 1회용 토큰, 72시간 만료 |
| 서명 실행 | `POST /api/signature-requests/{token}/sign` | ML-DSA-65 서명, PDF 메타데이터 삽입 |
| 서명된 PDF 다운로드 | `GET /api/signature-requests/{token}/signed-document` | `파일명_qusigned.pdf` 형식 |
| 무결성 검증 (토큰) | `POST /api/verify` | 인증 불필요, 공개 API |
| 무결성 검증 (파일) | `POST /api/verify/file` | 서명된 PDF 업로드로 직접 검증 |

---

## 로컬 실행

**사전 준비**
- JDK 21
- MariaDB 10.11 (root/root, database: qusign)
- Docker (MinIO 실행용)
- Node.js 20+

```bash
# 1. MinIO 기동
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

## 진행 현황

| 단계 | 내용 | 상태 |
|------|------|------|
| 1단계 | 환경 세팅 + PQC 핵심 검증 | 🔄 진행 중 (로컬 실행 확인 남음) |
| 2단계 | 백엔드 핵심 구현 | ✅ 완료 |
| 3단계 | 프론트엔드 구현 | ✅ 완료 |
| 4단계 | 기능 고도화 & 품질 강화 | 🔄 진행 중 |
| 5단계 | AWS 배포 + SES + GitHub Actions | ⬜ 진행 전 |
| 6단계 | Terraform + 수익화 | ⬜ 진행 전 |
| 7단계 | Loki + Grafana + 이직 준비 | ⬜ 진행 전 |

세부 계획 → [docs/exec-plans/PLAN.md](docs/exec-plans/PLAN.md)
