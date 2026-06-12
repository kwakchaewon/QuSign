# QuSign — 포트폴리오

## 프로젝트 한 줄 요약

> NIST PQC 표준 ML-DSA 기반 PDF 전자서명 SaaS — 양자 컴퓨터에도 안전한 서명을 웹에서 직접 만들었습니다

---

## 핵심 하이라이트

### 01 — PQC 암호화 직접 구현

NIST 2024 표준 ML-DSA(CRYSTALS-Dilithium)를 liboqs-java JNI로 직접 연동.

회원가입 → 키쌍 생성 → PBKDF2+AES-256 개인키 암호화 보관 → 서명 시 복호화 → ML-DSA 서명값 PDF 메타데이터 삽입 → 메모리 즉시 해제 전 파이프라인 구현.

### 02 — 법적 증거 수준 감사 로그

전자서명법 제31조(10년 보관 의무) 기반 Append-only 감사 테이블 설계.

- `UPDATE` · `DELETE` 메서드 미작성으로 불변성 강제
- 서명 이벤트 · IP · UTC 타임스탬프 불변 기록
- JSON 내보내기 API 제공 (법원 제출용)

### 03 — Redis Pub/Sub + SSE 실시간 알림

서명 완료 · 요청 · 취소 · 만료 이벤트를 Redis 채널 발행 → SseEmitter 푸시.

EC2 다중화 환경에서 인스턴스 간 알림 전달 구조 적용 (수평 확장 대비).

### 04 — AWS 풀스택 배포 + GitHub Actions CI/CD

VPC 퍼블릭/프라이빗 서브넷 분리, EC2 + RDS + S3 + ElastiCache 구성.

`push → main` 시 GitHub Actions 자동 빌드 · Docker 이미지 ECR 푸시 · EC2 롤링 배포. HTTPS + Route53 도메인 연결.

---

## 서비스 아키텍처

```
┌──────────────────────────────────────────────────────────────────┐
│                       Service Architecture                        │
│                                                                  │
│  [ 사용자 Browser / Vue 3 ]                                       │
│              │  HTTPS                                            │
│              ▼                                                   │
│       [ Route53 ]                                                │
│              │                                                   │
│              ▼                                                   │
│   [ EC2 — Nginx + Spring Boot 3 / Kotlin ]                       │
│         │              │              │                          │
│         ▼              ▼              ▼                          │
│  [ RDS MariaDB ]    [ S3 ]    [ ElastiCache (Redis) ]            │
│  (private subnet)  (PDF 저장)   (Pub/Sub · SSE 알림)             │
│         │                                                        │
│  ┌──────┴───────┐                                                │
│  ▼              ▼                                                │
│ [ liboqs-java ] [ AWS SES ]                                      │
│  ML-DSA 서명     서명 요청                                         │
│  /검증           이메일 발송                                        │
│  ▼                                                               │
│ [ PDFBox ]                                                       │
│  서명값 삽입/추출                                                   │
│                                                                  │
│  ─────────────────── CI/CD ──────────────────────────            │
│  GitHub push → GitHub Actions → Docker → ECR → EC2 배포          │
└──────────────────────────────────────────────────────────────────┘
```

---

## 인프라 레이어

| 레이어 | 기술 |
|---|---|
| **PQC 암호화** | liboqs-java (ML-DSA) · PDFBox · PBKDF2+AES-256 |
| **백엔드** | Kotlin + Spring Boot 3 · MariaDB · JWT |
| **프론트엔드** | Vue 3 + Vite + Pinia + TypeScript |
| **스토리지** | AWS S3 (PDF) · ElastiCache Redis (알림) |
| **이메일** | AWS SES (서명 요청 · 완료 알림) |
| **인프라** | AWS EC2 + RDS + VPC (퍼블릭/프라이빗 서브넷 분리) |
| **배포** | GitHub Actions · Docker · ECR · Route53 · HTTPS |

---

## 정량 수치

| 항목 | 수치 |
|---|---|
| ML-DSA 공개키 크기 | **2,528 byte** (RSA-2048 대비 양자 내성 확보) |
| 감사 로그 보존 기간 | **10년** (전자서명법 제31조 충족) |
| 구현 규모 | 4개 도메인 · 30+ API · 14개 Vue 화면 |
| 서명 상태 | PENDING · SIGNED · CANCELLED · EXPIRED 4종 |

---

## 이력서 요약

```
NIST PQC 표준 ML-DSA 기반 전자서명 SaaS 설계·개발 및 AWS 배포 (Kotlin + Spring Boot 3 / Vue 3)
- liboqs-java JNI 직접 연동, ML-DSA 서명·검증 파이프라인 구현
- EC2 + RDS + S3 + ElastiCache 인프라 구성 (VPC 서브넷 분리)
- GitHub Actions CI/CD 파이프라인 구축 (push → 자동 배포)
- 전자서명법 제31조 기반 Append-only 감사 로그 설계 (10년 보존)
- Redis Pub/Sub + SSE 실시간 인앱 알림 시스템 구현
```
