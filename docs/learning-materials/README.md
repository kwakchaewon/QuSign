# QuSign 학습 자료

PLAN.md 각 단계에서 필요한 핵심 개념을 **이론 → 현재 코드 적용 예시 → 확인 질문 & 답변** 형식으로 정리합니다.

## 1단계: 환경 세팅 + PQC 핵심 검증

| # | 주제 | 파일 | PLAN.md |
|---|---|---|---|
| 1 | Null Safety (`?.` `!!` `?:`) | [01-null-safety.md](01-null-safety.md) | §1-1 |
| 2 | Data Class | [02-data-class.md](02-data-class.md) | §1-1 |
| 3 | Extension Function | [03-extension-function.md](03-extension-function.md) | §1-1 |
| 4 | when Expression | [04-when-expression.md](04-when-expression.md) | §1-1 |
| 5 | Coroutines 기초 (`suspend`, `launch`) | [05-coroutines-basics.md](05-coroutines-basics.md) | §1-1 |
| 6 | **PQC & ML-DSA** — liboqs-java, 키 구조, 서명 흐름 | [06-pqc-mldsa.md](06-pqc-mldsa.md) | §1-2 |
| 7 | **PDFBox** — 메타데이터 삽입·추출, 무결성 검증 | [07-pdfbox.md](07-pdfbox.md) | §1-3 |
| 8 | **Docker Compose** — 로컬 환경, MinIO, 프로파일 분리 | [08-docker-compose.md](08-docker-compose.md) | §1-4 |

## 2단계: 백엔드 핵심 구현

| # | 주제 | 파일 | PLAN.md |
|---|---|---|---|
| 9 | **백엔드 아키텍처** — JWT, JPA, 레이어 분리, @Transactional | [09-backend-architecture.md](09-backend-architecture.md) | §2 |

## 3단계: 프론트엔드 구현

| # | 주제 | 파일 | PLAN.md |
|---|---|---|---|
| 10 | **Vue 3 + Pinia** — Composition API, 상태 관리, Router 가드, Axios 인터셉터 | [10-vue3-pinia.md](10-vue3-pinia.md) | §3 |

## 4단계: 기능 고도화 & 품질 강화

| # | 주제 | 파일 | PLAN.md |
|---|---|---|---|
| 11 | **Redis Pub/Sub + SSE** — 실시간 알림, SseEmitter, 동시성 자료구조 | [11-redis-sse.md](11-redis-sse.md) | §4-5 |
| 12 | **Spring Security RBAC** — 역할 분리, @PreAuthorize, AdminInitializer | [12-spring-security-rbac.md](12-spring-security-rbac.md) | §4-9 |

## 5단계: 보안 취약점 개선

| # | 주제 | 파일 | PLAN.md |
|---|---|---|---|
| 13 | **OWASP Top 10** — A01~A09, 감사 로그, XSS/CSRF | [13-owasp-top10.md](13-owasp-top10.md) | §5 |

## 6단계: AWS 배포 + GitHub Actions

| # | 주제 | 파일 | PLAN.md |
|---|---|---|---|
| 14 | **AWS 핵심** — IAM, EC2, S3, Route53, SSM, EventBridge | [14-aws-core.md](14-aws-core.md) | §6-1~6-10 |
| 15 | **GitHub Actions CI/CD** — ECR, Multi-Stage Dockerfile, 경로 기반 분기 | [15-cicd-github-actions.md](15-cicd-github-actions.md) | §6-11 |

## 7단계: Terraform + 수익화

| # | 주제 | 파일 | PLAN.md |
|---|---|---|---|
| 16 | **Terraform** — IaC, HCL, State, 모듈, S3 백엔드 | [16-terraform.md](16-terraform.md) | §7-1 |

## 8단계: 모니터링 + PQC TLS

| # | 주제 | 파일 | PLAN.md |
|---|---|---|---|
| 17 | **Loki + Grafana** — 로그 수집, LogQL, 이상 접근 탐지 | [17-loki-grafana.md](17-loki-grafana.md) | §8-1 |
| 18 | **PQC 하이브리드 TLS** — X25519MLKEM768, ML-DSA 사설 CA, crypto-agility | [18-pqc-tls.md](18-pqc-tls.md) | §8-4 |

---

## 학습 방법

1. 각 파일의 **이론** 섹션을 읽습니다.
2. **현재 코드 예시** 섹션을 실제 파일과 함께 확인합니다.
3. **확인 질문**을 먼저 스스로 답해본 뒤 답변과 비교합니다.
4. PLAN.md에서 해당 항목을 체크 표시합니다.

## 코드 참조 경로

| 파일 | 주요 패턴 |
|---|---|
| `backend/src/main/kotlin/com/qusign/auth/service/AuthService.kt` | null safety (elvis + throw) |
| `backend/src/main/kotlin/com/qusign/auth/entity/User.kt` | nullable 필드, soft delete |
| `backend/src/main/kotlin/com/qusign/auth/dto/AuthDtos.kt` | data class |
| `backend/src/main/kotlin/com/qusign/signature/service/PqcSignatureServiceImpl.kt` | ML-DSA 키 생성·서명·검증 |
| `backend/src/main/kotlin/com/qusign/signature/service/PdfBoxSignatureService.kt` | PDFBox 메타데이터 삽입·추출 |
| `backend/src/main/kotlin/com/qusign/notification/service/NotificationService.kt` | Redis Pub/Sub |
| `backend/src/main/kotlin/com/qusign/notification/service/SseEmitterRegistry.kt` | SSE, ConcurrentHashMap |
| `backend/src/main/kotlin/com/qusign/signature/service/SignatureFlowService.kt` | 트랜잭션, 감사 로그 |
| `backend/src/main/kotlin/com/qusign/config/SecurityConfig.kt` | Spring Security, RBAC |
| `frontend/src/stores/authStore.ts` | Pinia, JWT 관리 |
| `frontend/src/lib/api.ts` | Axios 인터셉터 |
| `frontend/src/router/index.ts` | Vue Router 가드 |
| `.github/workflows/deploy.yml` | GitHub Actions CI/CD |
