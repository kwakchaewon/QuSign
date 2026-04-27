# ARCHITECTURE.md

## 1. 한 문장 요약

> NIST PQC 표준 ML-DSA 기반 전자서명 SaaS — 양자 컴퓨터에도 안전한 서명을 웹에서 쉽게.

---

## 2. 도메인 맵

| 도메인 | 역할 | 주요 진입점 |
|---|---|---|
| **Auth** | 회원가입, 로그인, JWT, 개인키 암호화 저장 | `POST /auth/register`, `POST /auth/login` |
| **Document** | PDF 업로드, SHA3-256 해시, MinIO 저장 | `POST /documents`, `GET /documents` |
| **Signature** | 서명 요청 생성, 서명 실행, 무결성 검증 | `POST /signatures/request`, `POST /signatures/sign`, `GET /signatures/verify` |
| **Notification** | 이메일 발송 (AWS SES) | 내부 이벤트 기반 (3단계) |

---

## 3. 패키지 레이어링

```
[ Controller (Presentation) ]
        ↓
[ Service (Application) ]
        ↓
[ PqcSignatureService / PdfSignatureService / StorageService (Domain Infrastructure) ]
        ↓
[ liboqs-java / PDFBox / MinIO Client / MariaDB (External) ]
```

의존성 방향: 위→아래만 허용. Controller가 liboqs 직접 접근 금지.

### 핵심 서비스 책임 분리

| 클래스 | 책임 | 품질 등급 |
|---|---|---|
| `PqcSignatureService` | liboqs-java 래핑, ML-DSA 키생성/서명/검증 전담 | **S** |
| `PdfSignatureService` | PDFBox 래핑, 서명값 삽입/추출 전담 | **S** |
| `StorageService` | MinIO/S3 추상화 | B |

---

## 4. 외부 의존성

| 시스템 | 용도 | 장애 시 영향 |
|---|---|---|
| liboqs-java (JNI) | ML-DSA 암호 연산 | 서비스 전면 중단 |
| PDFBox | PDF 서명값 삽입/추출 | 서명 기능 불가 |
| MariaDB 10.11 | 사용자, 문서, 서명 메타데이터 | 서비스 전면 중단 |
| MinIO / AWS S3 | PDF 파일 원본 저장 | 업로드/다운로드 불가 |
| AWS SES | 서명 요청 이메일 | 이메일 발송 불가, 수동 링크 전달로 대체 |

---

## 5. 데이터 흐름

### 서명 플로우
```
사용자 → PDF 업로드 → SHA3-256 해시 생성 → MinIO 저장
       → 서명 요청 생성 (1회용 토큰, 24h 만료)
       → [이메일로 서명자에게 링크 전송 — 3단계]
       → 서명자 접속 → 토큰 검증
       → 개인키 복호화 (AES-256, PBKDF2 파생키)
       → ML-DSA 서명값 생성
       → PDF에 서명값 삽입 (PDFBox)
       → 서명된 PDF MinIO 저장
       → 개인키 즉시 메모리 해제
```

### 검증 플로우
```
PDF 업로드 → 서명값 추출 (PDFBox)
           → 공개키로 ML-DSA 검증
           → 결과 반환 (서명자 / 서명 시각 / 변조 여부)
```

---

## 6. 인프라 진화 경로

| 단계 | 환경 |
|---|---|
| 1~3단계 | Docker Compose (로컬) |
| 4단계 | AWS EC2 + RDS + S3 (콘솔 수동 세팅) |
| 5단계 | Terraform IaC 전환 |
| 6단계 | Loki + Grafana 모니터링 추가 |

---

## 7. 변경 시 영향 범위 매트릭스

| 변경 영역 | 영향 받는 영역 | 필요한 검증 |
|---|---|---|
| `PqcSignatureService` | 서명/검증 전체 | ML-DSA 단위 테스트 + 통합 테스트 |
| `PdfSignatureService` | 서명 삽입/추출 | PDF 왕복 테스트 |
| DB 스키마 | 모든 서비스 | 마이그레이션 스크립트 + 통합 테스트 |
| MinIO → S3 전환 | `StorageService` 구현체만 | 업로드/다운로드 E2E |
| JWT 설정 | Auth 전체 | 인증 통합 테스트 |
