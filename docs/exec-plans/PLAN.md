# PQC 전자서명 서비스 — 개발 & 학습 체크리스트

> 재직 중 사이드 프로젝트 / 12개월 목표  
> 학습 직후 바로 구현 — 배운 것을 즉시 코드로 굳힌다

---

## 진행 현황

| 단계 | 내용 | 상태 |
|------|------|------|
| 1단계 | 환경 세팅 + PQC 핵심 검증 | ✅ 완료 |
| 2단계 | 백엔드 핵심 구현 | ✅ 완료 |
| 3단계 | 프론트엔드 구현 | ✅ 완료 |
| 4단계 | 기능 고도화 & 품질 강화 | 🔄 진행 중 (다음 단계) |
| 5단계 | 보안 취약점 개선 (OWASP Top 10) | ✅ 완료 |
| 6단계 | AWS 배포 + SES + GitHub Actions | 🔄 진행 중 |
| 7단계 | Terraform + 수익화 | ⬜ 진행 전 |
| 8단계 | Loki + Grafana + 이직 준비 | ⬜ 진행 전 |

---

## 1단계: 환경 세팅 + PQC 핵심 검증
> 기간: 0~6주 / ⚠️ 가장 중요한 단계 — 여기서 막히면 전체가 흔들림

### 1-1. Kotlin 기초 → Spring Boot 프로젝트 세팅 (2주)

**목표:** Kotlin 문법을 익히고 바로 프로젝트 뼈대를 만든다

- [ ] null safety 이해 (`?.` `!!` `?:`) → [`docs/learning-materials/01-null-safety.md`](../learning-materials/01-null-safety.md)
- [ ] data class 개념 및 사용법 → [`docs/learning-materials/02-data-class.md`](../learning-materials/02-data-class.md)
- [ ] extension function 개념 및 사용법 → [`docs/learning-materials/03-extension-function.md`](../learning-materials/03-extension-function.md)
- [ ] when expression 사용법 → [`docs/learning-materials/04-when-expression.md`](../learning-materials/04-when-expression.md)
- [ ] Kotlin 공식 문서 Basics 섹션 완독
- [ ] coroutine 기초 개념 파악 (suspend, launch) — 깊이는 나중에 → [`docs/learning-materials/05-coroutines-basics.md`](../learning-materials/05-coroutines-basics.md)
- [x] **→ 즉시 적용:** Gradle Kotlin DSL 기반 Spring Boot 3.5 프로젝트 생성
- [x] **→ 즉시 적용:** Logback JSON 구조화 로그 설정
- [x] **→ 즉시 적용:** GitHub 레포지토리 생성 + 초기 커밋

**완료 기준:** Kotlin으로 Spring Boot REST API 하나 혼자 짤 수 있으면 됨

---

### 1-2. liboqs-java + ML-DSA → 암호화 코드 구현 (2주)

**목표:** ML-DSA 개념을 이해하고 바로 키 생성·서명·검증 코드를 작성한다

- [ ] 기존 RSA/ECDSA 서명 동작 원리 복습
- [ ] ML-DSA(Dilithium)가 RSA와 다른 점 이해
- [ ] 격자 암호(Lattice Cryptography) 개념 수준 파악 (깊이 X, 방향만)
- [ ] NIST PQC 표준 4종 이름과 용도 파악 (ML-KEM, ML-DSA, SLH-DSA, FALCON)
- [ ] ML-DSA 키쌍 구조 이해 (공개키/개인키/서명값 각각 크기)
- [x] **→ 즉시 적용:** liboqs 공식 Docker 이미지 pull
- [x] **→ 즉시 적용:** liboqs-java 네이티브 빌드 Dockerfile 작성
- [x] **→ 즉시 적용:** Spring Boot 프로젝트에 ML-DSA 의존성 연동 (Bouncy Castle 1.84)
- [x] **→ 즉시 적용:** ML-DSA 키쌍 생성 / 서명 / 검증 코드 작성
- [x] ✅ 단위 테스트 통과 ← **전체 프로젝트 핵심 관문**

**완료 기준:** ML-DSA 서명 검증 단위 테스트 통과

---

### 1-3. PDFBox → PDF 서명 삽입·추출 구현 (1주)

**목표:** PDFBox 구조를 익히고 바로 ML-DSA 서명값을 PDF에 넣고 꺼낸다

- [ ] PDFBox 기본 구조 파악 (PDDocument, PDPage 등)
- [ ] PDF 메타데이터 구조 이해
- [x] **→ 즉시 적용:** PDFBox 의존성 추가 (Gradle)
- [x] **→ 즉시 적용:** ML-DSA 서명값 Base64 인코딩 후 PDF 메타데이터에 삽입
- [x] **→ 즉시 적용:** PDF에서 서명값 추출 및 디코딩
- [x] **→ 즉시 적용:** 삽입 → 추출 → 검증 연동 테스트

**완료 기준:** PDF에 서명값 넣고 꺼내서 ML-DSA 검증 통과

---

### 1-4. 로컬 Docker Compose 환경 완성 (1주)

- [x] Docker Compose 작성 (MinIO 컨테이너)
- [x] 로컬 환경 전체 실행 확인
  ```bash
  docker compose up -d     # MinIO 기동
  ./gradlew bootRun        # 백엔드 (local 프로파일)
  cd frontend && npm run dev   # 프론트엔드
  ```

**1단계 완료 기준**
- [x] ML-DSA 단위 테스트 통과
- [x] PDF 서명값 삽입/추출 성공
- [x] 로컬 전체 스택 실행 확인 (백엔드 + MinIO + 프론트엔드)

---

## 2단계: 백엔드 핵심 구현
> 기간: 6주~3개월 / ✅ 완료

### 구현 완료 목록

- [x] 회원가입 API — ML-DSA 키쌍 생성, PBKDF2+AES-256 개인키 암호화, DB 저장
- [x] JWT 로그인 / 인증 필터
- [x] PDF 업로드 API — SHA3-256 해시, MinIO 저장
- [x] 문서 목록 / 다운로드 API
- [x] 서명 요청 API — 1회용 토큰(UUID), 72시간 만료
- [x] 서명 실행 API — 개인키 복호화, ML-DSA 서명, PDF 메타데이터 삽입, 서명 완료 PDF 저장
- [x] 무결성 검증 API (토큰 기반) — 공개 API, 인증 불필요
- [x] 무결성 검증 API (파일 기반) — 서명된 PDF 업로드로 직접 검증
- [x] 레이어 분리: `PqcSignatureService` / `PdfSignatureService` / `StorageService` 인터페이스화
- [x] 로컬 이메일 서비스 — 콘솔 로그로 서명 링크 출력 (`@Profile("local")`)
- [x] 전체 통합 테스트 통과

**미완 (2단계 범위)**
- [x] Postman으로 전체 플로우 수동 확인 (회원가입 → 업로드 → 서명 → 검증) — `docs/test-scenarios/service-scenario.md` 브라우저 수동 테스트로 대체

---

## 3단계: 프론트엔드 구현
> 기간: 3~5개월 / ✅ 완료

### 구현 완료 목록

- [x] Vue 3 + Vite + Pinia + Vue Router + TypeScript 프로젝트 세팅
- [x] Axios 인스턴스 + JWT 인터셉터
- [x] 인증 스토어 (Pinia) — 로그인 상태 관리, 네비게이션 가드
- [x] 디자인 시스템 — Claude 디자인 활용, CSS 변수 토큰 (`tokens.css`)
- [x] 6개 핵심 화면 구현
  - [x] 로그인 / 회원가입
  - [x] 대시보드 (문서 목록, 검색, 다운로드)
  - [x] 서명 요청 (`/request`) — 3단계 업로드·서명자 설정·완료
  - [x] 서명자 화면 (`/sign/:token`) — 본인 확인, PDF 미리보기, 약관 동의, 서명 실행
  - [x] 무결성 검증 (`/verify`) — 토큰 입력 | 파일 업로드 탭 전환
- [x] 서명 완료 파일명 `파일명_qusigned.pdf` 형식 적용

**추가 구현 완료**
- [x] `npm run dev` 브라우저 렌더링 + 전체 시나리오 수동 테스트 완료 (`service-scenario.md` 전 항목 `[V]`)
- [x] 문서 미리보기 PDF.js 연동 — `PdfViewer.vue` 컴포넌트 구현, 서명자 화면 "크게 보기" 모달 포함
- [x] SVG 파비콘 적용 + 라우터 meta 기반 페이지별 탭 타이틀 적용

---

## 4단계: 기능 고도화 & 품질 강화
> 기간: 진행 중
>
> 수동 테스트 시나리오: [`docs/test-scenarios/service-scenario.md`](../test-scenarios/service-scenario.md)

### 4-1. 유효성 검사 및 예외처리 강화

**목표:** 미처리 예외·입력 검증 항목을 일괄 정리한다

#### 완료된 항목

- [x] 파일 업로드 50MB 초과 — 프론트 사전 차단 + `MaxUploadSizeExceededException` → 413
- [x] 서명 비밀번호 오류 — `AEADBadTagException` → 400
- [x] 한글 파일명 다운로드 — RFC 5987 `filename*=UTF-8''` 인코딩
- [x] Spring multipart 한도 1MB → 50MB 상향

#### 백엔드 — 미처리

- [x] 비밀번호 최소 길이 / 복잡도 규칙 (`@Size(min=8)` + `@Pattern` 영문자·숫자 필수)
- [x] 이메일 형식 에러 메시지 한국어화
- [x] PDF 파일 형식 검증 — 매직 바이트 확인 (Content-Type 위조 방지)
- [x] 서명 요청 중복 방지 — 동일 문서·서명자 조합 처리 정책 결정
- [x] 만료 토큰 재사용 시 명확한 에러 메시지 검증
- [x] 서명 토큰 형식 비정상 입력 시 500 → 400 처리 (404로 처리 중, 500 미발생)

#### 프론트엔드 — 미처리

- [x] 회원가입 비밀번호 규칙 실시간 피드백
- [x] 서명 요청 페이지 이메일 중복 입력 방지
- [x] axios 인터셉터 — 500/네트워크 오류 시 토스트 표시
- [x] 401 응답 시 자동 로그아웃
- [x] 업로드 중 페이지 이탈 방지 (`beforeunload`)

**완료 기준:** 위 항목 전체 처리 + `./gradlew test` 통과

---

### 4-3. 서명 요청 상세 조회 페이지

**목표:** 요청자가 서명 요청 현황(서명자별 완료 여부, 서명된 PDF)을 한눈에 파악

#### Step 1. Claude Design → UI 설계

> **⚠️ 구현 전 반드시 Claude Design에서 목업을 완성한다**

- [x] claude.ai/design에서 상세 화면 목업 생성
  - 상단: 문서 정보 (파일명, SHA3-256, 업로드 일시)
  - 중단: 서명자 목록 (이메일 / 상태 배지 / 서명 일시 / 서명된 PDF 다운로드)
  - 하단: 요청 메타데이터 (요청자, 요청 일시, 만료 일시)
- [x] `detail.html` + `detail.css` 다운로드
- [x] `harness/DESIGN_PROMPTS.md`에 프롬프트 기록

#### Step 2. 백엔드

- [x] `GET /api/signature-requests/{id}` — 요청자 본인만 조회 가능
- [x] `SignatureRequestDetailResponse` DTO 추가
- [x] `./gradlew test` 통과

#### Step 3. 프론트엔드

- [x] `detail.html` + `detail.css` → `DocumentDetailView.vue` 변환
- [x] 라우트 추가: `/documents/:id` (인증 필수)
- [x] 대시보드에서 문서 클릭 시 상세 페이지 이동
- [x] 서명자별 상태 배지 (PENDING / SIGNED / EXPIRED)
- [x] 서명된 PDF 다운로드 버튼 (SIGNED일 때만 활성)
- [x] 서명 링크 복사 버튼 (PENDING일 때만 표시)

**완료 기준:** 목업 완성 → 상세 페이지 동작 → 테스트 통과 (테스트만 남음)

---

### 4-4. PDF 멀티 파일 업로드 (최대 5개)

**목표:** 서명 요청 생성 시 PDF를 한 번에 최대 5개 일괄 처리

#### Step 1. Claude Design → UI 설계

> **⚠️ 구현 전 반드시 Claude Design에서 목업을 완성한다**

- [x] claude.ai/design에서 멀티 업로드 화면 목업 생성
  - 드롭존: 복수 파일 목록 (파일명 / 크기 / 해시 / 삭제 버튼)
  - 파일 추가 버튼 (5개 미만일 때만 활성)
  - 파일별 업로드 진행 바
  - 5개 초과 경고 메시지
- [x] `request-multi.jsx` + `request-multi.css` 다운로드
- [x] `harness/DESIGN_PROMPTS.md`에 프롬프트 기록

#### Step 2. 백엔드

> **구현 방식 변경:** 초기 계획(배치 API)에서 **DocumentBundle 엔티티 기반 번들 방식**으로 변경됨

- [x] `DocumentBundle` / `DocumentBundleItem` 엔티티 추가
- [x] 번들 서명 요청 생성 — 복수 파일을 하나의 번들로 묶어 서명자별 링크 1개 발급
- [x] 번들 서명 실행 — 번들 내 모든 문서에 ML-DSA 서명 일괄 처리
- [x] `./gradlew test` 통과 (+ 테스트 `@MockitoBean EmailService` 누락 일괄 수정)

#### Step 3. 프론트엔드

- [x] RequestView Step 1 멀티 업로드로 교체
- [x] 드롭존 복수 파일 드래그앤드롭 (full → compact → max-reached 전환)
- [x] 파일 5개 초과 시 클라이언트 사전 차단
- [x] 완료 화면: 번들 표시 ("파일명.pdf 외 N건") + 서명자별 링크 1개 생성
- [x] Step 2 서명자 입력 — 이메일 pill 방식으로 교체
- [x] `BundleDetailView.vue` — 번들 서명 상세 화면 (번들 내 파일별 개별 다운로드)
- [x] 번들 서명 API 연동 (DocumentBundle 방식)

**완료 기준:** 목업 완성 → 멀티 업로드 동작 → 테스트 통과

---

### 4-5. Redis 기반 실시간 인앱 알림 시스템

**목표:** 주요 서명 이벤트를 Redis Pub/Sub + SSE로 실시간 헤더 알림 센터에 표시한다

> 이메일 알림은 6단계 SES 연동 시 추가. 이 단계는 인앱 알림에 집중한다.

#### 사전 학습 (구현 전 필수)

| 주제 | 핵심 개념 | 학습 포인트 |
|---|---|---|
| Redis Pub/Sub | PUBLISH / SUBSCRIBE 패턴, 채널 구조 | 메시지는 영속되지 않음 (fire-and-forget) |
| Spring Data Redis | `RedisTemplate`, `MessageListener`, `RedisMessageListenerContainer` | Lettuce 클라이언트, 연결 풀 동작 방식 |
| SSE (Server-Sent Events) | HTTP 단방향 스트림, `text/event-stream` MIME | `EventSource` 브라우저 API, 자동 재연결 |
| `SseEmitter` (Spring) | 비동기 응답 유지, `onCompletion` / `onTimeout` | 서버 스레드 점유 없이 연결 유지하는 원리 |
| 동시성 자료구조 | `ConcurrentHashMap`, `CopyOnWriteArrayList` | 다중 SSE 연결을 스레드 안전하게 관리해야 하는 이유 |
| Redis vs DB 알림 | Redis = 실시간 push / DB = 영속 저장 | 둘의 역할을 분리하는 이유 |

- [ ] Redis Pub/Sub 개념 및 `PUBLISH` / `SUBSCRIBE` 명령어 실습
- [ ] `spring-boot-starter-data-redis` 의존성 구조 파악 (Lettuce vs Jedis)
- [ ] MDN `EventSource` API 문서 읽기 (SSE 재연결, Last-Event-ID 헤더)
- [ ] `SseEmitter` Spring 공식 문서 읽기

#### 사후 학습 (구현 후 심화)

| 주제 | 핵심 개념 | 학습 포인트 |
|---|---|---|
| Redis Pub/Sub vs Streams | At-most-once(Pub/Sub) vs At-least-once(Streams) | SSE 연결 공백 시 메시지 누락 가능성 |
| SSE vs WebSocket | 단방향 vs 양방향, 프로토콜 비교 | 알림처럼 단방향이면 SSE가 더 단순·효율적 |
| 수평 확장 (Scale-out) | 다중 서버 인스턴스 → Redis가 브로커로 동작 | 인스턴스 A가 발행 → Redis → 인스턴스 B의 SSE로 전달 |
| SSE 재연결 누락 방지 | `Last-Event-ID` 헤더 + 이벤트 ID 전략 | 끊김 동안 발생한 알림을 재연결 시 replay |
| Redis Cluster / Sentinel | 고가용성 Redis 구성 | 6단계 AWS 배포 시 ElastiCache 적용 |

- [ ] Redis Streams (`XADD`, `XREAD`) 개념 학습 — Pub/Sub과 차이점 정리
- [ ] SSE + `Last-Event-ID` 패턴 실습 (재연결 시 누락 알림 복구)
- [ ] AWS ElastiCache (Redis 호환) 요금 구조 파악 — 6단계 배포 준비

#### 알림 타입

| 타입 | 수신자 | 트리거 |
|---|---|---|
| `SIGN_DONE` | 요청자 | 서명자가 서명 완료 |
| `SIGN_REQUEST` | 서명자 (가입자인 경우) | 서명 요청 수신 |
| `SIGN_CANCELLED` | 서명자 (가입자인 경우) | 요청자가 서명 취소 |
| `SIGN_EXPIRING_SOON` | 서명자 (가입자인 경우) | 만료 D-1 스케줄러 |
| `SIGN_EXPIRED` | 요청자 | 서명 기한 만료 (스케줄러) |

#### Step 1. 백엔드 — DB + Redis 기반

- [x] Flyway `V4__add_notifications.sql` — `notifications` 테이블
  - `id`, `user_id` (FK), `type` (ENUM), `title`, `message`, `reference_id` (signature_request.id), `is_read`, `created_at`
- [x] `Notification` 엔티티 + `NotificationRepository`
- [x] Redis 의존성 추가 (`spring-boot-starter-data-redis`) + `RedisConfig`
- [x] `NotificationService`
  - `createAndPublish(userId, type, title, message, referenceId)` — DB 저장 → Redis 채널 발행
  - `getNotifications(userId)` — 최근 50건 조회
  - `markAsRead(id, userId)` — 읽음 처리
  - `markAllAsRead(userId)` — 전체 읽음
  - `getUnreadCount(userId)` — 미읽음 개수
- [x] SSE 엔드포인트: `GET /api/notifications/stream` — Redis 구독 → `SseEmitter` push
- [x] REST 엔드포인트:
  - `GET /api/notifications` — 알림 목록
  - `PUT /api/notifications/{id}/read` — 읽음 처리
  - `PUT /api/notifications/read-all` — 전체 읽음
  - `GET /api/notifications/unread-count` — 미읽음 개수

#### Step 2. 알림 발생 지점 연결

- [x] `SignatureFlowService.sign()` 완료 → `SIGN_DONE` (요청자)
- [x] `SignatureFlowService.requestSignature()` → `SIGN_REQUEST` (서명자가 가입자인 경우)
- [x] 취소 서비스 → `SIGN_CANCELLED` (서명자가 가입자인 경우)
- [x] `@Scheduled` 스케줄러 — 만료 D-1 → `SIGN_EXPIRING_SOON` / 만료 → `SIGN_EXPIRED`

#### Step 3. 프론트엔드

- [x] `notificationStore` (Pinia) — `notifications`, `unreadCount`, SSE 연결 관리
- [x] SSE 클라이언트 (`EventSource /api/notifications/stream`) — 새 알림 실시간 수신
- [x] `AppTopbar.vue` — 벨 아이콘 + 미읽음 배지 (숫자)
- [x] `NotificationDropdown.vue` — 알림 목록, 클릭 시 해당 상세 페이지 이동, 전체 읽음 버튼
- [x] Docker Compose에 Redis 컨테이너 추가

#### 인프라 변경

- [x] `docker-compose.yml` — Redis 컨테이너 추가 (`redis:7-alpine`, 포트 6379)
- [x] `application-local.yml` — Redis 연결 설정
- [x] `./gradlew test` 통과

**완료 기준:** 서명 완료 시 헤더 벨 아이콘에 미읽음 배지 표시 + 드롭다운에서 알림 확인 + 클릭 시 상세 페이지 이동

---

### 4-6. 서명 요청 취소

**목표:** 요청자가 PENDING 상태의 서명 요청을 철회할 수 있게 한다

#### Step 1. Claude Design → UI 설계

> **⚠️ 구현 전 반드시 Claude Design에서 목업을 완성한다**

- [x] claude.ai/design에서 목업 생성
  - 상세 페이지 PENDING 서명자 행 — 취소 버튼 위치·스타일
  - 취소 확인 모달 ("취소 후 되돌릴 수 없습니다" + 확인/취소 버튼)
  - 서명자가 취소된 링크 접근 시 안내 화면 (SignerView 내 취소 상태)
- [x] `harness/DESIGN_PROMPTS.md`에 프롬프트 기록

#### Step 2. 백엔드

- [x] `POST /api/signature-requests/{id}/signers/{email}/cancel` — 요청자 본인 + PENDING 상태만 허용
- [x] 취소된 토큰으로 서명 시도 시 410 Gone 반환
- [x] `./gradlew test` 통과

#### Step 3. 프론트엔드

- [x] 상세 페이지 취소 버튼 (PENDING 상태일 때만 표시)
- [x] 취소 확인 모달 구현
- [x] SignerView — 취소된 요청 접근 시 안내 화면 표시

**완료 기준:** 취소 후 서명 링크 접근 시 "취소된 요청" 안내 화면 표시

---

### 4-7. 서명 링크 재발송 & 만료 D-1 리마인더

**목표:** 만료된 요청을 재발송하고, 만료 24시간 전 자동 리마인더를 보낸다

- [ ] `POST /api/signature-requests/{id}/resend` — 새 토큰 발급 + 기존 토큰 무효화
- [ ] 상세 페이지 재발송 버튼 (EXPIRED 상태일 때만 표시)
- [ ] `@Scheduled` — 매일 정각 만료 D-1 토큰 조회 → 리마인더 이메일 발송
- [ ] 로컬: 콘솔 로그 출력
- [ ] `./gradlew test` 통과

**완료 기준:** 만료 요청 재발송 동작 + 스케줄러 로그 확인

---

### 4-8. 감사 로그 (Audit Trail)

**목표:** 서명 이벤트(요청·서명·취소·다운로드)를 법적 증거 수준으로 기록한다

> **적용 법령**
> - 전자서명법 제31조 — 인증 관련 기록 **10년** 보관 의무
> - 전자문서법 제5조 — 진본성 보존 (작성자·수신자·일시 필수 기록)
> - 개인정보보호법 제6·21조 — `actor_email + ip_address` 결합 시 개인정보, 수집 동의·암호화 의무
> - NIST SP 800-53 AU-11 — 감사 기록 최소 7년 보존 (글로벌 기준)

#### Step 1. DB + 엔티티

- [x] `V9__add_audit_logs.sql` — `audit_logs` 테이블
  - 컬럼: `id`, `event_type`, `actor_email`, `signature_request_id (nullable)`,
    `bundle_id (nullable)`, `document_id (nullable)`, `ip_address (IPv6 대비 45자)`,
    `user_agent (500자)`, `created_at DATETIME(6)`
  - 인덱스: `(document_id)`, `(bundle_id)`, `(actor_email)`, `(created_at)`
  - **`UPDATE` · `DELETE` 문 없음** — append-only 테이블 (법적 불변성 요건)
  - `created_at`은 애플리케이션에서 `Instant.now(ZoneOffset.UTC)` 고정 (서버 시각 조작 방지)
- [x] `AuditLog` 엔티티 + `AuditEventType` enum
  ```
  SIGN_REQUEST_CREATED       — 단건 서명 요청 생성
  BUNDLE_REQUEST_CREATED     — 번들 서명 요청 생성
  SIGNED                     — 단건 서명 완료
  BUNDLE_SIGNED              — 번들 서명 완료
  SIGNER_CANCELLED           — 단건 서명자 취소
  BUNDLE_SIGNER_CANCELLED    — 번들 서명자 취소
  SIGNED_DOCUMENT_DOWNLOADED — 서명된 문서 다운로드 (증거 체인)
  ```
- [x] `AuditLogRepository` + `AuditLogService`
  - `save()` 만 노출 — **수정·삭제 메서드 미작성** (불변성 강제)
  - `findByDocumentId(documentId)` — 시간 오름차순
  - `findByBundleId(bundleId)` — 시간 오름차순

#### Step 2. 백엔드 — 감사 기록 포인트

- [x] `AuditContext(ipAddress, userAgent)` data class 추가
- [x] `SignatureController`에서 `HttpServletRequest`로 IP/UserAgent 추출 → `AuditContext`로 서비스에 전달
- [x] `SignatureFlowService` 메서드 시그니처에 `auditCtx` 파라미터 추가 (6곳)
  - `requestSignatureForUser()` → `SIGN_REQUEST_CREATED`
  - `requestBundleSignature()` → `BUNDLE_REQUEST_CREATED`
  - `sign()` 단건 경로 → `SIGNED`
  - `signBundle()` → `BUNDLE_SIGNED`
  - `cancelSigner()` → `SIGNER_CANCELLED`
  - `cancelBundleSigner()` → `BUNDLE_SIGNER_CANCELLED`
- [x] `SignatureController` 다운로드 4개 엔드포인트에 `SIGNED_DOCUMENT_DOWNLOADED` 기록
  - `getSignedDocument`, `getSignedBundleDocument`, `getSignedDocumentByRequester`, `getBundleSignedDocByRequester`

#### Step 3. PDF 메타데이터

- [x] `PdfSignatureService.embedSignature()` 인터페이스에 `ipAddress` 파라미터 추가
- [x] `PdfBoxSignatureService` 구현체에서 서명자 IP·서명 일시를 PDF 메타데이터에 삽입
  - 검증(`extractMetadata`, `verify`) 로직은 변경 없음

#### Step 4. API

- [x] `GET /api/documents/{documentId}/audit` — 단건 감사 로그 (요청자 본인만)
- [x] `GET /api/bundles/{bundleId}/audit` — 번들 감사 로그 (요청자 본인만)
- [x] `GET /api/documents/{documentId}/audit/export` — JSON 내보내기 (법적 분쟁 제출용, 요청자 본인만)
- [x] `GET /api/bundles/{bundleId}/audit/export` — 번들 감사 로그 내보내기
- [x] `AuditLogResponse` DTO — `id`, `eventType`, `actorEmail`, `ipAddress`, `createdAt`
  - `userAgent`는 내부 기록용으로만 사용, API 응답에서 제외

#### Step 5. 프론트엔드

- [x] 공용 컴포넌트 `AuditTimeline.vue` 제작 (props: `documentId?`, `bundleId?`)
- [x] `DocumentDetailView.vue` 하단에 `AuditTimeline` 삽입
- [x] `BundleDetailView.vue` 하단에 `AuditTimeline` 삽입

#### Step 6. 테스트

- [x] 단위 테스트: `AuditLogService` — 각 이벤트 타입 기록 검증, 삭제 메서드 미노출 확인
- [x] 통합 테스트: sign 엔드포인트 호출 → audit API 조회 → 로그 존재 확인
- [x] `./gradlew test` 통과

#### Step 7. 운영 정책 (법적 의무)

- [x] **보존 기간 10년** 명시 — 전자서명법 제31조 기준, 자동 삭제·TTL 설정 금지
- [x] **약관 수정** — 회원가입 약관에 감사 로그 수집 목적·보유 기간 명시
  - 문구 예시: *"서비스 이용 중 발생하는 서명 이벤트 및 접속 IP는 인증 보안 감시·서명 무결성 검증·분쟁 해결 목적으로 10년간 보관됩니다."*
  - 근거: 개인정보보호법 제6조 (수집 동의 근거 명시 의무)
- [ ] **KISA RFC 3161 타임스탬프 연동 검토** — 서버 시각 대신 공인 시각 사용 (eIDAS·법원 제출 시 증거력 강화, 3단계 이후 적용)

**완료 기준:** 서명 완료 PDF에 감사 정보(IP·일시) 포함 + 상세 페이지에서 이벤트 타임라인 조회 + 다운로드 이력 기록 + 감사 로그 JSON 내보내기 + 약관 IP 수집 동의 문구 반영

---

### 4-9. 관리자 페이지

**목표:** 감사 로그·사용자·서명 현황을 관리자가 시스템 전체 범위에서 조회·관리할 수 있는 어드민 패널을 구축한다

> 4-8 감사 로그 구현 완료 후 진행

#### Step 1. 권한 모델 확장

- [x] `V12__add_role_to_users.sql` — `users.role VARCHAR(20) DEFAULT 'USER'`
- [x] `User` 엔티티에 `role: String` 필드 추가
- [x] `SecurityConfig` — `/api/admin/**` 경로를 `ROLE_ADMIN`만 접근 허용
- [x] 초기 관리자 계정 등록 방법 명세 — `ADMIN_EMAIL` / `ADMIN_PASSWORD` ENV, 앱 시작 시 `AdminInitializer` 자동 생성·승격

#### Step 2. 백엔드 — 관리자 API

- [x] `GET /api/admin/stats` — 시스템 전체 통계 (총 사용자·서명 수, 상태별 카운트)
- [x] `GET /api/admin/users` — 전체 사용자 목록 (페이징, 이메일 검색)
- [x] `PUT /api/admin/users/{email}/disable` — 사용자 비활성화 (소프트, 로그인 차단)
- [x] `GET /api/admin/audit` — 시스템 전체 감사 로그 (페이징, `eventType`·날짜 필터)
  - 4-8 `AuditLogService`에 `findAll(pageable, filter)` 메서드 추가
- [x] `GET /api/admin/users/{email}/audit` — 특정 사용자의 감사 로그 전체 조회
- [x] `GET /api/admin/audit/export` — 전체 감사 로그 JSON 내보내기 (법적 분쟁·감사 대응)
- [x] 모든 관리자 API에 `@PreAuthorize("hasRole('ADMIN')")` 적용

#### Step 3. 프론트엔드

- [x] 라우터 가드: `/admin/**` → `ADMIN` 역할 없으면 `/home` 리다이렉트
- [x] `AdminLayout.vue` — 관리자 전용 레이아웃 (사이드바: 통계 / 사용자 / 감사 로그)
- [x] `AdminStatsView.vue` (`/admin`) — 통계 카드 + 최근 감사 이벤트 요약
- [x] `AdminUsersView.vue` (`/admin/users`) — 사용자 목록 테이블 (이메일 검색, 비활성화 버튼)
- [x] `AdminAuditView.vue` (`/admin/audit`) — 전체 감사 로그 테이블 (이벤트 타입·날짜 필터, 내보내기 버튼)

#### Step 4. 테스트

- [ ] `ROLE_USER` 계정이 `/api/admin/**` 접근 시 403 반환 확인
- [ ] 관리자 전체 감사 로그 조회 및 필터 동작 확인
- [ ] 사용자 비활성화 후 해당 계정 로그인 차단 확인
- [ ] `./gradlew test` 통과

**완료 기준:** 관리자 로그인 → 전체 사용자·감사 로그 조회 → JSON 내보내기 동작 + 비관리자의 어드민 접근 시 403

---

### 4-10. 루트 페이지 대시보드화

**목표:** 로그인 후 진입점(`/`)을 서비스 현황이 한눈에 보이는 대시보드로 만든다

#### Step 1. Claude Design → UI 설계

- [x] claude.ai/design에서 대시보드 목업 생성
  - 상단 요약 카드: 전체 문서 수 / 서명 완료 / 대기 중 / 만료
  - 최근 요청 목록 (5건)
  - 빠른 액션 버튼 (서명 요청 생성, 검증)
- [x] `home.html` + `home.css` + `home.jsx` 다운로드
- [x] `harness/DESIGN_PROMPTS.md`에 프롬프트 기록

#### Step 2. 백엔드

- [x] `GET /api/dashboard` — 요약 통계 반환 (문서 수, 상태별 카운트)

#### Step 3. 프론트엔드

- [x] `HomeView.vue` 구현 (목업 기반, 라우트 `/home`)
- [x] 루트 라우트(`/`) → 로그인 시 `/home`, 비로그인 시 `/login`으로 분기

**완료 기준:** 로그인 후 `/` 접속 시 통계 카드 + 최근 요청 목록 표시

---

### 4-11. 계정 설정

**목표:** 비밀번호 변경, 알림 수신 설정, 계정 탈퇴를 제공한다

#### Step 1. Claude Design → UI 설계

- [x] claude.ai/design에서 계정 설정 목업 생성
- [x] `settings.html` + `settings.css` 다운로드
- [x] `harness/DESIGN_PROMPTS.md`에 프롬프트 기록

#### Step 2. 백엔드

- [x] `PUT /api/users/password` — 현재 비밀번호 확인 후 변경 (개인키 재암호화 포함)
- [x] `PUT /api/users/notification-settings` — 알림 수신 여부 저장
- [x] `DELETE /api/users/me` — 계정 탈퇴 (소프트 딜리트, 서명 이력 보존)
- [x] `GET /api/users/me` — 프로필 + 알림 설정 조회
- [x] 탈퇴 계정 로그인 차단 (`AccountDeletedException`) + 전체 테스트 통과

#### Step 3. 프론트엔드

- [x] `AccountSettingsView.vue` 구현 (라우트 `/settings` 등록, 네비게이션 연결)
- [x] 비밀번호 변경 폼 (현재 비밀번호 확인 필수, 강도 표시 바 포함)
- [x] 계정 탈퇴 확인 모달 (이메일 일치 입력 확인)

**완료 기준:** 비밀번호 변경 동작 + 탈퇴 후 로그인 불가 확인

---

### 4-12. 알림 시스템 고도화

**목표:** 4-5에서 구축한 Redis 알림 기반 위에 UX 개선 및 설정 연동을 완성한다

> 4-5 완료 후 진행. Redis + SSE 기반은 4-5에서 구현됨.

#### Step 1. UX 개선

- [x] 알림 드롭다운 — 타입별 아이콘·색상 구분 (서명 완료 / 요청 / 취소 / 만료)
- [x] 알림 클릭 시 읽음 처리 + 상세 페이지 동시 이동
- [x] 알림 없을 때 빈 상태 메시지 ("새로운 알림이 없습니다")
- [x] 알림 드롭다운 외부 클릭 시 닫힘 처리

#### Step 2. 설정 연동

- [x] 계정 설정(4-11) `notifySignRequest` / `notifySignDone` 토글 — 알림 생성 여부 실제 반영
- [x] 알림 발생 전 사용자 설정 확인 (`NotificationService`에서 `User.notify*` 체크)

#### Step 3. 알림 목록 전체 페이지

- [x] `/notifications` 라우트 — 전체 알림 이력 (페이지네이션), `NotificationsView.vue` 구현
- [ ] 헤더 드롭다운 "전체 보기" 링크 복원 (현재 주석 처리)

> **⏸ 보류:** "전체 보기" 버튼 및 `NotificationsView.vue` UI는 Claude Design 목업 완성 후 복원.
> 백엔드 API(`GET /api/notifications?page=N`)와 라우트(`/notifications`)는 유지.
> 복원 시 `NotificationDropdown.vue`의 `<!-- TODO: Claude Design 목업 완성 후 복원 -->` 주석 해제.

**완료 기준:** 알림 설정 OFF 시 해당 타입 알림 미생성 + 타입별 아이콘 표시

---

## 5단계: 보안 취약점 개선
> 기간: 4단계 완료 후

**목표:** OWASP Top 10 기준 전 영역 점검·수정

---

### 실행 절차

#### Phase 1 — Scan (발견)

- [x] 피처 브랜치 생성 (`security/owasp-review`)
- [x] `/security-review` 스킬 실행 → findings 목록 확보
- [x] OWASP 점검 항목 수동 확인 (아래 체크리스트 기준)

#### Phase 2 — Triage (등급 분류)

발견 항목을 아래 기준으로 분류한다:

| 등급 | 기준 | 처리 |
|---|---|---|
| Critical | 인증 우회 / RCE / 데이터 직접 노출 가능 | 즉시 수정 (이번 사이클) |
| High | 권한 상승 / 암호화 오류 / 인젝션 가능 | 즉시 수정 (이번 사이클) |
| Medium | 특정 조건 필요, 영향 있음 | 다음 단계까지 허용 |
| Low | 심층 방어 항목 | backlog 이슈로만 기록 |

#### Phase 3 — Fix (수정)

- [x] Critical / High 항목 하나씩 커밋
- [x] 커밋 메시지에 OWASP 항목 태그 포함 (예: `fix(A01): JWT 권한 검사 누락`)
- [x] 수정 전후 테스트 케이스 작성 필수

#### Phase 4 — Verify (검증)

- [x] Critical / High 항목은 `/security-review` 재실행으로 닫힘 확인
- [x] `./gradlew test` 통과

---

### OWASP 점검 체크리스트

- [x] A01 — 접근 제어 (비활성/탈퇴 계정 JWT 즉시 차단, ADMIN 경로 `@PreAuthorize` 보호)
- [x] A02 — 암호화 실패 (PBKDF2+AES-256-GCM 개인키 암호화, BCrypt 비밀번호, 서명 후 키 메모리 zeroing)
- [x] A03 — 인젝션 (JPA 사용으로 SQL 인젝션 없음, 입력 검증 `@Valid` 적용)
- [x] A05 — 보안 설정 오류 (HTTP 보안 헤더 추가, CORS 환경변수 기반 설정, X-Forwarded-For 신뢰 설정)
- [x] A07 — 인증/세션 관리 (SSE 단기 토큰으로 JWT URL 노출 완화, 토큰 일회성 소비)
- [x] A09 — 보안 로깅 (서명·취소·다운로드 이벤트 AuditLog 기록 확인)
- [x] 프론트엔드 XSS / CSRF (Vue 3 기본 이스케이핑, Stateless JWT → CSRF 불필요)
- [x] API 응답 민감 필드 노출 여부 (userAgent API 미노출, privateKey 미노출 확인)

---

**5단계 완료 기준:** Critical / High 취약점 0건 (재스캔으로 확인)

---

## 6단계: AWS 배포 + SES + GitHub Actions
> 기간: 이후 / (구 5단계)

---

### 아키텍처 확정

```
인터넷
  │
  ▼
Route53 (qusign.link)
  │
  ▼
EC2 t3.small (퍼블릭 서브넷, ap-southeast-1a)  ← 싱가포르 리전
  ├── Nginx  → 80/443  → 리버스 프록시
  ├── Spring Boot Docker  (8080)
  ├── MariaDB Docker  (127.0.0.1:3306, 로컬 볼륨)  ← 루프백만 노출
  └── Vue 3 빌드 결과물 서빙 (/dist)
  │
  ├── S3 (문서 저장)  ← VPC Endpoint로 인터넷 미경유
  └── SES (이메일 발송)

ECR  ← GitHub Actions가 Docker 이미지 푸시
EventBridge Scheduler  ← KST 21:30 EC2 정지 / KST 09:00 재시작
Lambda (start/stop)  ← EventBridge에서 호출 (EC2만 제어)
SSM Parameter Store  ← DB 비밀번호, JWT 시크릿 등 민감값 관리
```

> **ALB 생략 결정**: ALB는 월 ~$18 고정 비용 발생. 포트폴리오 단계에서는 EC2 Nginx 직접 443으로 충분.  
> **CloudFront 생략**: Vue 빌드 결과를 EC2 Nginx에서 서빙. 사용자가 10명 이내면 EC2 부담 없음.

---

### 예상 월 비용 (비용 절감 적용 후)

> 기준 리전: **ap-southeast-1 (싱가포르)** / 단가: EC2 t3.small $0.0230/h / 스케줄: KST 09:00~21:30 (12.5h/day)

| 리소스 | 스펙 | 비고 | 월 예상 |
|---|---|---|---|
| EC2 | t3.small | 12.5h/day 가동 (375h × $0.0230) + MariaDB 포함 | ~$8.6 |
| Elastic IP | 고정 IP | 정지 중 과금 (11.5h × 30일 × $0.005) | ~$1.7 |
| S3 | 5GB 이하 | 문서 저장 + DB 백업 ($0.025/GB) | ~$0.15 |
| ECR | 500MB 이하 | Docker 이미지 | ~$0.05 |
| Route53 | 호스팅 영역 1개 | | ~$0.50 |
| SES | 이메일 수백 건 | | ~$0.02 |
| 도메인 | .com 구매 | 연 $12 = 월 | ~$1 |
| **합계** | | | **~$12/월** |

> 비교: 스케줄러 없이 24시간 가동 시 ~$18/월 → **약 33% 절감**  
> 비교: RDS 사용 대비 월 **~$9.5 절감** (구성 단순화)

---

### 6-0. 개인정보보호법 — 국외 이전 고지 (필수)

> **한국 개인정보보호법 제28조의8**: 싱가포르 리전 사용 시 이용자 개인정보가 국외로 이전됨.  
> 서비스 운영 전 아래 의무 고지 사항을 개인정보 처리방침 및 회원가입 동의서에 반드시 포함해야 함.

| 고지 항목 | 내용 |
|---|---|
| 이전받는 자 | Amazon Web Services, Inc. |
| 이전되는 국가 | 싱가포르 (ap-southeast-1) |
| 이전 일시 및 방법 | 서비스 이용 시 네트워크를 통해 전송 |
| 이전되는 개인정보 항목 | 이메일, 이름, 서명 데이터, 업로드 문서 |
| 이전받는 자의 이용 목적 | 서비스 인프라 운영 (저장, 처리) |
| 이전받는 자의 보유·이용 기간 | 회원 탈퇴 후 즉시 파기 또는 법령에 따른 보존 기간 |

- [x] 개인정보 처리방침에 국외 이전 항목 추가
- [x] 회원가입 화면에 국외 이전 동의 체크박스 추가 (또는 처리방침 링크 명시)
- [ ] 실서비스 전환 시 `ap-northeast-2` (서울) 복귀 검토 → 개인정보보호법 국외 이전 의무 소멸

> ℹ️ 포트폴리오 단계에서 실제 개인정보를 수집·처리하지 않는다면 법적 의무는 낮으나, 서비스 구조상 미리 적용해두는 것을 권장.

---

### 6-0. AWS 계정 사용 기준

| 작업 | 사용 계정 | 이유 |
|------|-----------|------|
| 도메인 구매 (Route53) | **루트 계정** | 결제 수반 작업 |
| 결제 정보 변경 / 예산 알림 설정 | **루트 계정** | 결제 콘솔은 루트만 접근 |
| IAM 사용자·그룹·정책 생성 | **루트 계정** | IAM 관리 권한 |
| EC2 / S3 / RDS / SES 실제 작업 | **IAM 계정** (`qusign_cwkwak`) | 최소 권한 원칙 |
| AWS CLI (`aws` 명령) | **IAM 계정** | Access Key는 IAM 계정 발급분 사용 |
| GitHub Actions 배포 | **IAM 계정** (`github-actions-deployer`) | 별도 배포 전용 계정 |

> 루트 계정은 도메인 구매·결제·IAM 초기 설정 외에는 **절대 사용 금지** (AWS 보안 모범 사례)

---

### 6-1. 사전 준비 (로컬)

- [x] AWS CLI v2 설치 (`winget install Amazon.AWSCLI`)
- [x] `aws configure` — Access Key, Secret Key, 리전 `ap-southeast-1` (싱가포르) 설정
  - [x] IAM 콘솔 → 사용자 → 보안 자격증명 → 액세스 키 발급
    - AWS 계정 ID: `285868221698`
    - IAM 사용자: `qusign_cwkwak` (그룹: `qusign_developers`)
    - 태그: `Project=qusign`, `Environment=prod`, `Owner=cwkwak`
    - 다음 단계: 보안 자격증명 탭 → 액세스 키 만들기 → CLI 선택
  - [x] `aws configure set region ap-southeast-1` (리전 사전 설정 완료)
  - [x] `aws configure` 실행 후 Access Key ID / Secret Access Key 입력
  - [x] `aws sts get-caller-identity` 로 인증 확인
- [x] Docker Desktop 로그인 확인
- [x] 도메인 구매 (Route53)
  > ⚠️ **루트 계정 사용** — 도메인 구매는 결제가 포함되므로 반드시 루트 계정(kwakchaewon)으로 콘솔 로그인 후 진행
  - [x] AWS 콘솔 → Route53 → 도메인 등록 → 도메인 이름 검색
  - [x] 도메인 선택 및 구매: **`qusign.link`** ($5/년) — 2026-06-17 등록 완료, 만료 2027-06-17
  - [x] 구매 완료 시 호스팅 영역(Hosted Zone) 자동 생성 확인
    - Route53 → 호스팅 영역 → `qusign.link` 존재 여부 확인
  - [x] ⚠️ **이메일 인증 필수** — ksh03003@naver.com 수신 메일 링크 클릭 (15일 내 미인증 시 도메인 일시 중지)
  - [x] 이후 EC2 연결 시 A 레코드 추가 예정 (§6-3에서 처리)
- [x] Spring Boot `application-prod.yml` 환경변수 기반 설정 확인
  - DB URL / 유저 / 패스워드 → `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}` 환경변수로 읽는지 확인
  - S3 버킷명, 리전 → 환경변수로 읽는지 확인
  - JWT 시크릿 → 환경변수로 읽는지 확인
  - [x] `application-prod.yml` 생성 완료

---

### 6-2. IAM 설정 (최소 권한 원칙)

> 콘솔: IAM → 역할/사용자 생성

#### EC2 인스턴스 역할 (EC2가 AWS 서비스에 접근하기 위한 역할)
- [x] IAM 역할 생성: `qusign_ec2_role`
  - 신뢰 정책: EC2 서비스
  - 권한 정책:
    - `AmazonS3FullAccess` (나중에 버킷 한정으로 축소)
    - `AmazonSESFullAccess`
    - `AmazonSSMReadOnlyAccess` (Parameter Store 읽기)
    - `AmazonEC2ContainerRegistryReadOnly` (ECR 이미지 풀)

#### GitHub Actions 배포용 IAM 사용자
- [x] IAM 사용자 생성: `qusign_github_actions_deployer`
  - 프로그래밍 방식 액세스 (Access Key 발급 완료)
  - 권한 정책:
    - `AmazonEC2ContainerRegistryFullAccess` (ECR 푸시)
    - `AmazonSSMFullAccess` (EC2 Run Command로 배포)
    - EC2 start/stop 권한 (인라인 정책 `qusign_github_actions_ec2_control`):
      ```json
      {
        "Version": "2012-10-17",
        "Statement": [
          {
            "Effect": "Allow",
            "Action": ["ec2:StartInstances", "ec2:StopInstances"],
            "Resource": "arn:aws:ec2:ap-northeast-2:285868221698:instance/*"
          }
        ]
      }
      ```
  - Access Key → GitHub Secrets 등록 완료 (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`)
  - ⚠️ 인라인 정책 Resource를 아래 ARN으로 교체 필요 (리전 ap-northeast-2 → ap-southeast-1 수정 포함):
    `arn:aws:ec2:ap-southeast-1:285868221698:instance/i-0447a621521e4fc2d`

#### EventBridge + Lambda용 역할
- [x] IAM 역할 생성: `qusign_lambda_eventbridge_role`
  - ARN: `arn:aws:iam::285868221698:role/qusign_lambda_eventbridge_role`
  - 신뢰 정책: Lambda 서비스 (`lambda.amazonaws.com`)
  - 권한 정책:
    - 고객 관리형 정책 `qusign_lambda_ec2_control` 연결 (EC2 start/stop)

---

### 6-3. 네트워크 (VPC)

> 콘솔: VPC → VPC 생성 (리전: ap-southeast-1 싱가포르)

- [x] VPC 생성
  - 이름: `qusign_vpc` (VPC ID: `vpc-0ad2925f2b2784bd1`)
  - IPv4 CIDR: `10.0.0.0/16`
- [x] 서브넷 생성
  - 퍼블릭: `qusign_vpc-subnet-public1-ap-southeast-1a` (ap-southeast-1a)
- [x] 인터넷 게이트웨이 생성 → VPC에 연결 (`qusign_vpc-igw`)
- [x] 라우팅 테이블: 퍼블릭 `0.0.0.0/0 → IGW` (`qusign_vpc-rtb-public`)
- [x] S3 VPC 엔드포인트 생성 (Gateway 타입, 무료) (`qusign_vpc-vpce-s3`)
  - EC2→S3 트래픽이 인터넷을 거치지 않아 데이터 전송 비용 0원

#### 보안 그룹

- [x] `qusign_ec2_sg` (SG ID: `sg-0d6159b3a82a45699`)
  | 방향 | 포트 | 소스 | 용도 |
  |---|---|---|---|
  | 인바운드 | 22 | 내 IP만 | SSH |
  | 인바운드 | 80 | 0.0.0.0/0 | HTTP (→ 443 리다이렉트) |
  | 인바운드 | 443 | 0.0.0.0/0 | HTTPS |
  | 아웃바운드 | 전체 | 0.0.0.0/0 | 나가는 트래픽 |

  > MariaDB(3306)는 `127.0.0.1`만 바인딩 — 보안 그룹 인바운드 규칙 불필요

---

### 6-4. ECR (Docker 이미지 레지스트리)

> **배포 아키텍처 결정**
>
> | 컴포넌트 | 배포 방식 | 이유 |
> |---|---|---|
> | Spring Boot (백엔드) | Docker → ECR → EC2 | 컨테이너 표준화, CI/CD 연동 |
> | Vue 3 (프론트엔드) | `npm run build` → EC2 Nginx 정적 서빙 | 단일 EC2 구조에서 컨테이너 불필요 |
>
> Nginx가 `/` → Vue 정적파일, `/api` → Spring Boot(8080) 프록시 처리.
> 프론트는 ECR 불필요 — `qusign_backend` 리포지토리는 백엔드 전용.

> 콘솔: ECR → 리포지토리 생성

- [x] ECR 리포지토리 생성: `qusign_backend`
  - 리전: `ap-southeast-1`
  - 이미지 스캔 활성화 (보안 취약점 자동 감지)
  - 수명 주기 정책 설정: 최신 3개 이미지만 유지 (스토리지 비용 절감)
    ```json
    {
      "rules": [{
        "rulePriority": 1,
        "selection": { "tagStatus": "any", "countType": "imageCountMoreThan", "countNumber": 3 },
        "action": { "type": "expire" }
      }]
    }
    ```
- [x] 로컬에서 ECR 로그인 테스트
  ```bash
  aws ecr get-login-password --region ap-southeast-1 | \
    docker login --username AWS --password-stdin \
    ACCOUNT_ID.dkr.ecr.ap-southeast-1.amazonaws.com
  ```

---

### 6-5. EC2 인스턴스 설정

> 콘솔: EC2 → 인스턴스 시작

- [x] 인스턴스 생성
  - **인스턴스 이름**: `qusign_app` → **인스턴스 ID: `i-0447a621521e4fc2d`**
  - AMI: Amazon Linux 2023
  - 인스턴스 유형: `t3.small` (vCPU 2, RAM 2GB — Spring Boot + Docker 최소 사양)
  - 키 페어: `qusign-keypair` 생성 완료
  - VPC: `qusign_vpc` / 서브넷: public1-ap-southeast-1a
  - 퍼블릭 IP 자동 할당: 활성화
  - IAM 인스턴스 프로필: `qusign_ec2_role` ✅
  - 보안 그룹: `qusign_ec2_sg`
  - 퍼블릭 IP: `3.0.102.188` (Elastic IP 연결 전 임시)

- [x] Elastic IP 할당 및 연결 (EC2 재시작 시 IP 변경 방지)
  - **고정 IP: `3.0.193.52`** (할당 ID: `eipalloc-0d6803d4ca4534aaa`)
  - 이름: `qusign_app_elasticip`, 연결 인스턴스: `i-0447a621521e4fc2d`
  > ⚠️ EC2가 **실행 중일 때만 Elastic IP 무료**. 정지 시에도 EC2에 연결되어 있으면 요금 발생.  
  > → EventBridge로 정지하는 동안 Elastic IP 요금 발생 (월 ~$0.005/hr × 11.5h × 30일 = ~$1.7)  
  > → 수용 가능한 비용이므로 연결 유지

- [x] SSH 접속 정보 ✅ 접속 확인 완료 (2026-06-18)
  | 항목 | 값 |
  |---|---|
  | **Host** | `3.0.193.52` (Elastic IP) |
  | **Port** | `22` |
  | **Login as** | `ec2-user` |
  | **키 페어** | `qusign-keypair.pem` / `qusign-keypair.ppk` |

  ```bash
  # OpenSSH (PowerShell / Terminal)
  ssh -i "C:\Users\eden240213\Desktop\qusign_secrets\qusign-keypair.pem" ec2-user@3.0.193.52
  ```
  > PuTTY: PuTTYgen으로 `.pem` → `.ppk` 변환 후 Connection → SSH → Auth → Credentials에서 `.ppk` 지정

- [x] EC2 접속 후 초기 설정 ✅ 완료 (2026-06-18)
  ```bash
  # Docker 설치
  sudo dnf install -y docker
  sudo systemctl enable --now docker
  sudo usermod -aG docker ec2-user

  # Docker Compose 설치
  sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64" \
    -o /usr/local/bin/docker-compose
  sudo chmod +x /usr/local/bin/docker-compose

  # AWS CLI 확인 (AL2023은 기본 설치됨)
  aws --version

  # Nginx 설치
  sudo dnf install -y nginx
  sudo systemctl enable nginx
  ```

- [x] Nginx 설정 (`/etc/nginx/conf.d/qusign.conf`) ✅ (2026-06-23)
  ```nginx
  server {
      listen 80;
      server_name qusign.link www.qusign.link;
      return 301 https://$host$request_uri;
  }

  server {
      listen 443 ssl;
      server_name qusign.link www.qusign.link;

      ssl_certificate /etc/letsencrypt/live/qusign.link/fullchain.pem;
      ssl_certificate_key /etc/letsencrypt/live/qusign.link/privkey.pem;

      # Vue 3 정적 파일
      root /var/www/qusign/dist;
      index index.html;

      # Swagger UI (springdoc) — Vue catch-all보다 먼저 선언
      location /v3/api-docs {
          proxy_pass         http://127.0.0.1:8080;
          proxy_set_header   Host              $host;
          proxy_set_header   X-Real-IP         $remote_addr;
          proxy_set_header   X-Forwarded-For   $proxy_add_x_forwarded_for;
          proxy_set_header   X-Forwarded-Proto $scheme;
      }
      location /swagger-ui/ {
          proxy_pass         http://127.0.0.1:8080;
          proxy_set_header   Host              $host;
          proxy_set_header   X-Real-IP         $remote_addr;
          proxy_set_header   X-Forwarded-For   $proxy_add_x_forwarded_for;
          proxy_set_header   X-Forwarded-Proto $scheme;
      }

      # Spring Boot API 프록시
      location /api/ {
          proxy_pass http://127.0.0.1:8080;
          proxy_set_header Host $host;
          proxy_set_header X-Real-IP $remote_addr;
          proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
          proxy_set_header X-Forwarded-Proto $scheme;
      }

      # SSE 엔드포인트 (긴 커넥션)
      location /api/notifications/stream {
          proxy_pass         http://127.0.0.1:8080;
          proxy_buffering    off;
          proxy_cache        off;
          proxy_read_timeout 3600s;
          proxy_set_header   Host              $host;
          proxy_set_header   X-Real-IP         $remote_addr;
          proxy_set_header   X-Forwarded-For   $proxy_add_x_forwarded_for;
          proxy_set_header   X-Forwarded-Proto $scheme;
      }

      # Vue Router history mode 지원
      location / {
          try_files $uri $uri/ /index.html;
      }
  }
  ```

- [x] Let's Encrypt SSL 인증서 발급 ✅ (2026-06-18)
  - 인증서 경로: `/etc/letsencrypt/live/qusign.link/fullchain.pem`
  - 만료일: 2026-09-17 (자동 갱신 타이머 설정됨)
  - Nginx 설정 자동 반영 완료
  ```bash
  sudo dnf install -y certbot python3-certbot-nginx
  sudo certbot --nginx -d qusign.link -d www.qusign.link
  # 자동 갱신 확인
  sudo systemctl status certbot-renew.timer
  ```

- [x] ~~배포 스크립트 생성 (`/home/ec2-user/deploy.sh`)~~ → **불필요로 판명, §6-12로 대체** ✅ (2026-06-19)
  > 초기 설계는 `docker run` 단일 컨테이너 + EC2에 별도 MariaDB 설치 구조였으나,
  > 실제로는 `docker-compose.prod.yml`(백엔드+MariaDB+Redis)로 전환됨.
  > 별도 스크립트 파일 없이 `.github/workflows/deploy.yml`의 SSH step이
  > SSM 조회 → ECR 로그인 → `docker-compose pull/up`을 인라인으로 직접 실행 (§6-12 참조).

---

### 6-6. SSM Parameter Store (민감값 관리)

> 콘솔: Systems Manager → Parameter Store  
> `.env` 파일을 서버에 올리지 않고 SSM에서 주입

- [x] 파라미터 생성 ✅ (2026-06-19, 타입: SecureString/String, 암호화: AWS managed key)
  | 파라미터 이름 | 유형 |
  |---|---|
  | `/qusign/prod/db-password` | SecureString |
  | `/qusign/prod/jwt-secret` | SecureString |
  | `/qusign/prod/s3-bucket` | String |
  | `/qusign/prod/cors-origins` | String |
  | `/qusign/prod/server-url` | String (`https://qusign.link`) — Swagger 서버 URL용 |
  > `db-url`/`db-username`은 SSM 파라미터로 분리하지 않음 — `docker-compose.prod.yml`에서
  > MariaDB가 컴포즈 내부 서비스(`mariadb:3306`, 계정 `qusign`)로 고정되어 있어 불필요.
- [x] GitHub Actions `deploy.yml`의 SSH step에서 SSM 값을 `.env`로 주입 후 `docker-compose`에 전달 ✅ (§6-12 참조)

---

### 6-7. S3 버킷 설정

> 콘솔: S3 → 버킷 만들기

> **사전 점검 결과 — 코드 버그 3건 발견 (Step 2에서 수정)**
>
> | # | 파일 | 문제 |
> |---|---|---|
> | 1 | `application-prod.yml` | `storage:` 섹션 없음 → prod에서도 MinIO 주소로 연결 시도 |
> | 2 | `docker-compose.prod.yml` | `S3_BUCKET` 전달하지만 `application.yml`은 `STORAGE_BUCKET` 읽음 → 버킷명 미전달 |
> | 3 | `StorageConfig.kt` | 항상 `StaticCredentialsProvider` 사용 → EC2 IAM 역할 인증 불가 |

#### Step 1. AWS 콘솔 (수동)

- [ ] 버킷 생성: **`qusign-documents-prod-285868221698`**
  > ⚠️ S3 버킷명은 하이픈만 허용 (언더스코어 불가)
  - 리전: `ap-southeast-1`
  - 퍼블릭 액세스 차단: **전체 차단** (EC2 IAM 역할로만 접근)
  - 버전 관리: 비활성화 (비용 절감)
  - 서버 측 암호화: SSE-S3 (AES-256) 활성화
- [ ] 버킷 정책: EC2 역할만 허용
  ```json
  {
    "Version": "2012-10-17",
    "Statement": [{
      "Effect": "Allow",
      "Principal": { "AWS": "arn:aws:iam::285868221698:role/qusign_ec2_role" },
      "Action": ["s3:GetObject", "s3:PutObject", "s3:DeleteObject"],
      "Resource": "arn:aws:s3:::qusign-documents-prod-285868221698/*"
    }]
  }
  ```
- [ ] 수명 주기 정책: 180일 이상 미접근 객체 Glacier로 이동 (장기 비용 절감)
- [ ] SSM 파라미터 값 업데이트
  - `/qusign/prod/s3-bucket` → `qusign-documents-prod-285868221698`

#### Step 2. 코드 수정 (버그 3건)

> **분리 원칙**: `StorageConfig.kt` 1개로 로컬(MinIO)·프로덕션(AWS S3) 모두 처리.
> `endpoint`가 비어있으면 AWS 네이티브 모드(IAM 역할 자동 인증), 값이 있으면 MinIO 모드(StaticCredentials).

- [ ] `StorageConfig.kt` — `endpoint` 비어있을 때 IAM 역할 자동 인증 분기 추가, `region` 파라미터 추가
- [ ] `application.yml` — `storage.region: ${STORAGE_REGION:us-east-1}` 항목 추가
- [ ] `application-prod.yml` — `storage:` 섹션 추가 (endpoint 없음, region `ap-southeast-1`, bucket `${S3_BUCKET}`)
- [ ] `docker-compose.prod.yml` — `STORAGE_BUCKET: ${S3_BUCKET}`, `STORAGE_REGION: ap-southeast-1` 추가
- [ ] `./gradlew test` 통과

#### Step 3. 배포 후 검증

- [ ] GitHub Actions 배포 후 EC2 컨테이너 로그에서 `버킷 생성 완료` or 버킷 정상 인식 확인
  ```bash
  docker logs qusign-backend 2>&1 | grep -i bucket
  ```
- [ ] PDF 업로드 → S3 콘솔에서 객체 생성 확인
- [ ] PDF 다운로드 정상 동작 확인

**완료 기준:** 프로덕션에서 PDF 업로드·다운로드 정상 동작 + S3 콘솔 객체 확인

---

### 6-8. EC2 MariaDB 설치 (Docker)

> RDS 대신 EC2 내 Docker 컨테이너로 MariaDB 운영.  
> EC2 정지 시 DB도 함께 정지 → EventBridge 스케줄러가 EC2 하나만 제어하면 됨.

- [x] ~~MariaDB 컨테이너 초기 실행 (EC2 접속 후 최초 1회)~~ → **§6-12 docker-compose로 대체** ✅ (2026-06-19)
  > 단독 `docker run` 대신 `docker-compose.prod.yml`의 `mariadb` 서비스로 운영 중.
  > 컨테이너명 `qusign-mariadb`, 계정 `qusign`(`qsadmin` 아님), 볼륨은 named volume `mariadb_data`.
  > `docker ps` 확인 결과 healthy 상태로 정상 기동 중 (2026-06-19 기준 2일째 가동).

- [x] 초기화 확인 ✅ (2026-06-19) — Flyway 마이그레이션으로 전체 테이블 정상 생성 확인
  (`audit_logs`, `documents`, `document_bundles`, `document_bundle_items`, `notifications`,
  `signature_requests`, `signatures`, `timestamp_tokens`, `users`, `flyway_schema_history`)
  ```bash
  docker exec -it qusign-mariadb mariadb -uqusign -p qusign -e "SHOW TABLES;"
  ```

- [ ] 일별 백업 스크립트 등록 (`/home/ec2-user/backup-db.sh`)
  ```bash
  #!/bin/bash
  DB_PASS=$(aws ssm get-parameter --name /qusign/prod/db-password \
    --with-decryption --query Parameter.Value --output text)
  BUCKET=$(aws ssm get-parameter --name /qusign/prod/s3-bucket \
    --query Parameter.Value --output text)
  DATE=$(date +%Y%m%d)

  docker exec qusign-mariadb \
    mariadb-dump -uqusign -p"$DB_PASS" qusign \
    | gzip > /tmp/qusign-db-$DATE.sql.gz

  aws s3 cp /tmp/qusign-db-$DATE.sql.gz \
    s3://$BUCKET/backups/db-$DATE.sql.gz
  rm -f /tmp/qusign-db-$DATE.sql.gz
  ```
  ```bash
  chmod +x /home/ec2-user/backup-db.sh
  # crontab -e 로 등록 (KST 21:00 = UTC 12:00, 정지 30분 전)
  (crontab -l 2>/dev/null; echo "0 12 * * * /home/ec2-user/backup-db.sh") | crontab -
  ```

---

### 6-9. EventBridge 스케줄러 (핵심 비용 절감)

> KST 21:30 = UTC 12:30 → 정지  
> KST 09:00 = UTC 00:00 → 시작  
> cron 표현식: `cron(분 시 * * ? *)`

#### Lambda 함수 생성 (EC2만 제어)

- [x] Lambda 함수 생성: `qusign_start_instances` ✅ (2026-06-22)
  - 런타임: Python 3.14 (계획 당시 3.12였으나 단순 boto3 호출이라 버전 무관)
  - 실행 역할: `qusign_lambda_eventbridge_role` (§6-2에서 기존 생성된 역할 재사용, `qusign_scheduler_role`이라는 새 역할은 만들지 않음)
  - 코드:
    ```python
    import boto3

    ec2 = boto3.client('ec2', region_name='ap-southeast-1')

    EC2_ID = 'i-0447a621521e4fc2d'

    def lambda_handler(event, context):
        action = event.get('action')  # 'start' or 'stop'

        if action == 'start':
            ec2.start_instances(InstanceIds=[EC2_ID])
            return {'status': 'started'}

        elif action == 'stop':
            ec2.stop_instances(InstanceIds=[EC2_ID])
            return {'status': 'stopped'}
    ```
  > MariaDB가 EC2 내부에 있으므로 EC2 정지 시 DB도 함께 정지됨 — RDS 별도 제어 불필요

- [x] EventBridge Scheduler 규칙 2개 생성 ✅ (2026-06-22, 시간대 `Asia/Seoul` 적용 — UTC 환산 대신 KST 값 직접 입력)
  | 이름 | Cron 표현식 (Asia/Seoul) | payload | 설명 |
  |---|---|---|---|
  | `qusign_nightly_stop` | `cron(30 21 * * ? *)` | `{"action": "stop"}` | KST 21:30 정지 |
  | `qusign_morning_start` | `cron(0 9 * * ? *)` | `{"action": "start"}` | KST 09:00 시작 |
  - 실행 역할: `qusign_eventbridge_scheduler_role` (두 규칙이 공유)
  > ⚠️ 시간대를 `Asia/Seoul`로 선택했기 때문에 Cron 값도 KST 기준으로 직접 입력해야 함.
  > UTC 기준 cron(`30 12`, `0 0`)을 그대로 쓰면 시간대 이중 적용으로 낮 12:30/밤 0:00에 실행되는 버그 발생 — 콘솔에서 발견하고 수정함.

- [x] Lambda 테스트 ✅ (2026-06-22) — 콘솔 Test 탭에서 `{"action": "stop"}` → `{"status": "stopped"}`, `{"action": "start"}` → `{"status": "started"}` 양방향 확인
- [x] EventBridge Scheduler에 Lambda 연결 확인 ✅ (2026-06-22) — 일정 목록에서 두 규칙 모두 대상 `qusign_start_instances`(LAMBDA_Invoke), 상태 "활성" 확인
- [ ] CloudWatch Logs에서 실제 스케줄 실행 확인 (다음 KST 21:30/09:00 정기 실행 후 확인 필요, §6-13과 동일 항목)

---

### 6-10. AWS SES 이메일 연동

> 콘솔: SES → 리전: ap-southeast-1 (싱가포르)

- [ ] 이메일 주소 자격 증명 (샌드박스 테스트용)
  - 발신자 이메일 인증: `noreply@qusign.link` (도메인 구매 후) 또는 개인 이메일로 먼저 테스트
- [ ] 도메인 자격 증명
  - SES → 자격 증명 → 도메인 추가 → Route53에 DKIM CNAME 레코드 자동 추가
- [ ] 샌드박스 제한 확인
  - 샌드박스 상태: 검증된 이메일로만 발송 가능
  - 베타 단계에서는 샌드박스로 충분 (실서비스 전에 프로덕션 접근 요청)
- [ ] `SesEmailService` 실제 구현
  ```kotlin
  // application-prod.yml 추가
  cloud:
    aws:
      ses:
        region: ap-southeast-1
  ```
  - `SesClient` 빈 등록 (EC2 IAM 역할로 자동 인증, 별도 AccessKey 불필요)
  - 서명 요청 HTML 템플릿 작성
  - 서명 완료 HTML 템플릿 작성
- [ ] 실제 이메일 수신 테스트

---

### 6-11. Route53 + 도메인 연결

- [x] 도메인 구매 ✅ (`qusign.link` — Route53 Registrar)
  - Route53에서 직접 구매 시 자동 연동 (추천)
  - 가비아/후이즈에서 구매 시 NS 레코드를 Route53 네임서버로 교체
- [x] 호스팅 영역 생성: `qusign.link` ✅ (ID: `Z05035142H5MR6F494TUU`)
- [x] A 레코드 등록 ✅ (2026-06-18)
  | 레코드 | 타입 | 값 |
  |---|---|---|
  | `qusign.link` | A | `3.0.193.52` |
  | `www.qusign.link` | CNAME | `qusign.link` |
- [ ] SES DKIM 레코드 추가 (SES 콘솔에서 자동 생성된 값 사용)
- [x] DNS 전파 확인 ✅ (2026-06-18) — `nslookup qusign.link 8.8.8.8` → `3.0.193.52` 정상 응답

---

### 6-12. GitHub Actions CI/CD

> `.github/workflows/deploy.yml`

- [x] Dockerfile 작성 (백엔드용) ✅ — `backend/Dockerfile` (multi-stage, liboqs 포함)
- [x] 프로덕션 Docker Compose 작성 ✅ (2026-06-19) — `docker-compose.prod.yml`
  - MariaDB 10.11, Redis 7, Spring Boot 백엔드 3개 컨테이너
  - healthcheck 기반 시작 순서 제어, `.env` 파일로 비밀값 주입
- [x] GitHub Secrets 등록 ✅ (2026-06-19)
  | Secret 이름 | 값 |
  |---|---|
  | `AWS_ACCESS_KEY_ID` | github-actions-deployer Access Key |
  | `AWS_SECRET_ACCESS_KEY` | github-actions-deployer Secret Key |
  | `EC2_HOST` | `3.0.193.52` |
  | `EC2_SSH_KEY` | EC2 .pem 파일 내용 전체 |
  | `ECR_REGISTRY` | `285868221698.dkr.ecr.ap-southeast-1.amazonaws.com` |
- [x] SSM 파라미터 등록 ✅ (2026-06-19)
  | 파라미터 | 유형 |
  |---|---|
  | `/qusign/prod/db-password` | SecureString |
  | `/qusign/prod/jwt-secret` | SecureString |
  | `/qusign/prod/s3-bucket` | String |
  | `/qusign/prod/cors-origins` | String |
  | `/qusign/prod/server-url` | String (`https://qusign.link`) — Swagger 서버 URL용 ✅ (2026-06-23) |
- [x] GitHub Actions workflow 파일 작성 ✅ (2026-06-19) — `.github/workflows/deploy.yml`
  - `deploy-backend` / `deploy-frontend` 병렬 잡
  - 백엔드: JAR 빌드 → ECR push → EC2 SSH → docker-compose up
  - 프론트엔드: Vue 빌드 → SCP → `/var/www/qusign/dist`
- [x] EC2 보안 그룹 SSH 포트 `0.0.0.0/0` 변경 ✅ (2026-06-19) — GitHub Actions 접근 허용
- [x] EC2 IAM 역할 권한 확인 ✅ — `AmazonEC2ContainerRegistryReadOnly` + `AmazonSSMReadOnlyAccess` 정상
- [x] `deploy-frontend` 첫 배포 성공 ✅ (2026-06-19) — Vue 빌드 → SCP → EC2 `/var/www/qusign/dist`
- [x] `docker compose` → `docker-compose` 명령어 교체 ✅ (2026-06-19) — EC2 v5.1.4 standalone 호환
- [x] CI/CD 경로 기반 분리 ✅ (2026-06-19) — `dorny/paths-filter@v3` 적용
  - `backend/**` 변경 시 `deploy-backend`만 실행
  - `frontend/**` 변경 시 `deploy-frontend`만 실행
  - `workflow_dispatch` 수동 실행 시 항상 둘 다 실행
  - `docker-compose.prod.yml` 변경 시 백엔드 파이프라인 트리거
- [x] 프론트엔드 API baseURL 빈 문자열로 변경 + Vite 개발 프록시 추가 ✅ (2026-06-19) — `frontend/src/lib/api.ts`, `frontend/vite.config.ts`
- [x] CORS 환경변수명 `CORS_ALLOWED_ORIGINS` → `CORS_ORIGINS` 통일 ✅ (2026-06-19) — `application.yml`과 SSM 파라미터명 불일치 수정
- [x] `deploy-backend` 파이프라인 성공 확인 ✅ (2026-06-19) — CORS 수정 커밋에서 7m22s 성공 (Run #27815548754)

---

### 6-13. 최종 검증

- [x] `https://qusign.link` HTTPS 접속 확인 ✅ (2026-06-18) — SSL 정상, 프론트엔드 미배포로 403 (다음 단계)
- [x] SSL 인증서 만료일 확인 ✅ — 2026-09-17, 자동 갱신 타이머 설정됨
- [x] GitHub Actions push → 자동 배포 확인 ✅ (2026-06-19) — `deploy-backend` 성공 (Run #27815548754, 7m22s)
- [x] Swagger UI 정상 렌더링 확인 ✅ (2026-06-23) — `https://qusign.link/swagger-ui/index.html` (springdoc 2.8.0, Spring Boot 3.5 호환)
- [ ] 회원가입 → 로그인 → PDF 업로드 → 서명 요청 → 이메일 수신 → 서명 → 검증 전체 플로우
- [ ] EventBridge 스케줄러 동작 확인 (KST 21:30에 정지, 09:00에 시작)
- [ ] CloudWatch Logs에서 Lambda 실행 로그 확인

---

### 비용 추가 절감 팁 (직장인 사이드 프로젝트)

| 전략 | 절감액 | 방법 |
|---|---|---|
| **EC2 내장 MariaDB** | ~$9.5/월 | RDS 제거 (확정 적용) |
| **EventBridge 야간 정지** | ~33% | KST 21:30-09:00 EC2 정지 (확정 적용) |
| **주말 전체 정지 추가** | 추가 ~28% | 금 21:30 ~ 월 09:00 정지 (베타 사용자 없을 때) |
| **t3.micro 강등** | ~50% | Spring Boot 메모리 최적화 후 검토 (`-Xmx512m`) |
| **1년 예약 인스턴스** | ~30% | 6개월 운영 확신 후 구매 (선불 없음 옵션) |
| **Free Tier 신규 계정** | 12개월 무료 | EC2 t2.micro 750h/월, S3 5GB 무료 (RDS Free Tier는 불필요) |

> **추천 순서**: EC2 내장 MariaDB(확정) → EventBridge(확정) → 주말 정지 추가 → Free Tier 계정 활용 → 안정화 후 예약 인스턴스  
> Free Tier 신규 계정: t2.micro 750h/월 무료 → 포트폴리오 기간 EC2 비용 거의 $0 가능

---

**6단계 완료 기준**
- [x] 실제 도메인으로 HTTPS 접속 가능 ✅ (2026-06-18)
- [x] GitHub Actions push 시 자동 배포 ✅ (2026-06-19)
- [ ] 이메일로 서명 링크 수신 후 서명까지 전체 플로우 동작 (SES 미연동)
- [ ] EventBridge로 KST 21:30-09:00 자동 정지/시작 동작 확인
- [ ] CloudWatch에서 Lambda 스케줄러 실행 로그 확인

---

## 7단계: Terraform + 수익화
> 기간: 7~10개월

### 7-1. Terraform 인프라 코드화

- [ ] IaC 개념 이해
- [ ] Terraform 기본 명령어 (`init`, `plan`, `apply`, `destroy`)
- [ ] HCL 문법 기초 / provider / state / 모듈 개념
- [ ] VPC / EC2 / RDS / S3 코드화
- [ ] Terraform state S3 백엔드 설정
- [ ] `terraform plan`으로 기존 인프라 diff 확인 → `apply` 검증

---

### 7-2. 수익화 구현

- [ ] 결제 모듈 연동 (토스페이먼츠 또는 아임포트)
- [ ] 무료 플랜 (월 5건) / 유료 플랜 (월 50건, 9,900원)
- [ ] 플랜별 사용량 제한 로직

---

### 7-3. 문서화 + 홍보

- [ ] RSA vs ML-DSA 성능 벤치마크 (키 생성 / 서명 / 검증 시간) → README 수치 정리
- [ ] 기술 블로그 연재 (liboqs-java 세팅기 / ML-DSA 실전 적용기 / Kotlin PQC 구현기)

**7단계 완료 기준**
- [ ] Terraform으로 인프라 재현 가능
- [ ] 첫 유료 결제 발생

---

## 8단계: Loki + Grafana + 이직 준비
> 기간: 10~12개월

### 8-1. 모니터링 스택 구축

- [ ] Loki + Promtail + Grafana Docker Compose 구성
- [ ] Spring Boot Logback → Promtail 연동
- [ ] Grafana 대시보드 구성
  - [ ] 일별 서명 요청 건수
  - [ ] 검증 성공/실패 비율
  - [ ] API 응답 시간 분포
  - [ ] 이상 접근 탐지 (짧은 시간 내 대량 요청)
- [ ] 이상 접근 알림 설정

---

### 8-2. 포트폴리오 완성

- [ ] GitHub README — 아키텍처 다이어그램, 벤치마크 결과, API 문서
- [ ] 이력서 업데이트
  ```
  - NIST 표준 ML-DSA 기반 전자서명 서비스 설계 및 개발 (Kotlin + Spring Boot)
  - liboqs-java 기반 PQC 암호화 실전 적용
  - Terraform으로 AWS 인프라 코드화 (VPC / EC2 / RDS / S3)
  - GitHub Actions CI/CD 파이프라인 구축 및 운영
  - Loki + Grafana 기반 운영 모니터링 구성
  - 실 서비스 운영 (유료 사용자 보유)
  ```

---

### 8-3. 이직 준비

- [ ] AWS SAA 시험 준비 (7~8개월부터 병행)
- [ ] 클라우드 보안 엔지니어 채용 공고 분석 (KT / SKT / 삼성SDS / LG CNS / 금융보안원)
- [ ] 이직 지원 시작

---

### 8-4. PQC 하이브리드 TLS 심화 (이직 포트폴리오 차별화)

> 목표: "전송 계층 ML-KEM + 응용 계층 ML-DSA를 한 사람이 다 만진" 풀스택 PQC 포트폴리오  
> 선행 조건: 6단계 AWS 배포 완료 (실서버에서 작업)

---

#### 8-4-0. 환경 준비

- [ ] EC2에서 OpenSSL 버전 확인
  ```bash
  openssl version          # 3.5+ 필요
  openssl list -kem-algorithms | grep ML-KEM
  ```
- [ ] OpenSSL 3.5 미만이면 소스 빌드 또는 패키지 설치
  - Amazon Linux 2023 기준 기본 버전이 3.5 미만일 경우 소스 컴파일 필요
- [ ] Nginx를 OpenSSL 3.5와 연동하여 재빌드 (패키지 매니저 버전은 번들 OpenSSL 사용)
- [ ] Nginx `ssl_conf_command Groups X25519MLKEM768:X25519` 설정 추가
  - X25519MLKEM768 우선 협상, PQC 미지원 클라이언트는 X25519로 자동 폴백

---

#### 8-4-1. 데모를 "증명"으로 끌어올리기 (1~2주)

> "연결된다"가 아니라 "PQC로 협상됐다는 증거가 있다" 수준

- [ ] Chrome에서 `https://qusign.link` 접속 → DevTools Security 패널에서 키 교환 알고리즘 확인
  - `X25519MLKEM768` 표시 스크린샷 저장
- [ ] tshark로 TLS 핸드셰이크 패킷 캡처
  ```bash
  tshark -i eth0 -f "port 443" -w handshake.pcap
  # ClientHello key_share extension에 MLKEM 바이트 확인
  tshark -r handshake.pcap -Y "tls.handshake.type == 1" -V | grep -A5 "Key Share"
  ```
- [ ] 고전 vs 하이브리드 오버헤드 직접 측정 및 비교표 작성

  | | X25519 (고전) | X25519MLKEM768 (하이브리드) |
  |---|---|---|
  | ClientHello 크기 | ~300B | ~1,600B (+약 1,200B) |
  | 핸드셰이크 지연 | 기준 | 거의 동일 (~1ms 미만) |
  | 보안 레벨 | 128-bit classical | 128-bit classical + PQC |

- [ ] 블로그/README 작성 (이 두 질문에 반드시 답할 것)
  - "왜 순수 MLKEM이 아니라 X25519와의 하이브리드인가"
  - "왜 oqs-provider가 아니라 OpenSSL 3.5 네이티브인가"

---

#### 8-4-2. 운영 현실 이해 (2~3주)

> 실무에서 PQC 전환 비용을 이해하는 단계 — 데모 단계에서 안 보이는 것들

- [ ] 폴백 직접 재현
  ```bash
  # PQC 미지원 클라이언트로 접속 → X25519로 폴백되는지 확인
  openssl s_client -connect qusign.link:443 -groups X25519
  ```
- [ ] Nginx 로그에 협상된 그룹 기록
  ```nginx
  log_format pqc '$remote_addr "$ssl_protocol" "$ssl_cipher" curve="$ssl_curve"';
  ```
- [ ] 큰 ClientHello 문제 정리
  - 1,600B+ ClientHello가 MTU(1,500B) 초과 → IP 단편화 발생
  - 일부 미들박스/방화벽이 분할된 패킷을 드롭하는 시나리오 이해
- [ ] Grafana 대시보드에 "PQC 협상 비율" 메트릭 추가 (8-1 Loki 연동)
- [ ] 블로그 작성: "왜 키 교환(KEM)은 실용 단계고 TLS 인증서는 아직인가"

---

#### 8-4-3. ML-DSA 사설 CA + 인증서 오버헤드 실측 (3~4주) ★희소성 최고

> 공개 PKI(Let's Encrypt, AWS ACM)는 ML-DSA 미지원 → 사설 CA로 직접 체험

- [ ] OpenSSL 3.5로 ML-DSA 사설 CA 구성
  ```bash
  # ML-DSA-65 루트 CA 생성
  openssl genpkey -algorithm ML-DSA-65 -out root-ca.key
  openssl req -new -x509 -key root-ca.key -out root-ca.crt -days 3650 \
    -subj "/CN=QuSign Test CA/O=QuSign"

  # 서버 인증서 생성 및 CA 서명
  openssl genpkey -algorithm ML-DSA-65 -out server.key
  openssl req -new -key server.key -out server.csr -subj "/CN=qusign.link"
  openssl x509 -req -in server.csr -CA root-ca.crt -CAkey root-ca.key -out server.crt -days 365
  ```
- [ ] 인증서 크기 비교표 작성 (직접 측정)

  | | RSA-2048 | ECDSA P-256 | ML-DSA-65 |
  |---|---|---|---|
  | 공개키 크기 | ~270B | 65B | 1,952B |
  | 서명 크기 | 256B | ~72B | 3,309B |
  | 핸드셰이크 추가 비용 | 기준 | 작음 | ~5KB 증가 |

- [ ] ML-DSA mTLS 핸드셰이크 동작 확인 (서버 + 클라이언트 모두 ML-DSA 인증서)
  ```bash
  # 서버
  openssl s_server -cert server.crt -key server.key -CAfile root-ca.crt -Verify 1
  # 클라이언트
  openssl s_client -connect localhost:4433 -cert client.crt -key client.key -CAfile root-ca.crt
  ```
- [ ] (선택) AWS Private CA에서 ML-DSA GA 지원 여부 확인 → 클라우드 버전 실험
- [ ] 미래 방향 정리 문서 작성
  - Merkle Tree Certificates — Google/Let's Encrypt가 논의 중인 대형 인증서 압축 방식
  - Composite 인증서 — RSA + ML-DSA 병렬 서명으로 점진적 전환
  - FN-DSA (FALCON) — ML-DSA보다 서명 크기가 작은 대안 (FIPS 206)
  - "지금은 안 되지만 이렇게 풀릴 것" — 현재 한계를 데이터로 설명하는 글

---

#### 8-4-4. QuSign에 crypto-agility 적용 (병행, 4주+)

> QuSign만이 만들 수 있는 차별점 — 전송·응용 두 계층 PQC를 한 설계로 묶기

- [ ] 서명 알고리즘 추상화 (`SignatureAlgorithm` 인터페이스 추출)
  ```kotlin
  interface SignatureAlgorithm {
      fun generateKeyPair(): KeyPair
      fun sign(privateKey: PrivateKey, data: ByteArray): ByteArray
      fun verify(publicKey: PublicKey, data: ByteArray, signature: ByteArray): Boolean
  }
  // 구현체: MlDsa44Algorithm, MlDsa65Algorithm, MlDsa87Algorithm
  // application.yml: pqc.signature.algorithm: ML-DSA-65
  ```
- [ ] ML-DSA 서명값이 QuSign 시스템에 주는 크기 영향 실측
  - DB 저장 크기 (ML-DSA-65 서명 ~3.3KB vs RSA 256B)
  - PDF 메타데이터 삽입 후 파일 크기 변화
  - 서명·검증 처리 시간 (liboqs-java vs BouncyCastle 1.84 비교)
- [ ] JDK 네이티브 PQC 전환 로드맵 정리
  - JEP 496 (ML-KEM) — JDK 24 Preview
  - JEP 527 (TLS 하이브리드) — 논의 중
  - "현재는 liboqs-java, 표준 라이브러리 출시 시 교체 가능한 구조" 문서화

---

**8-4 완료 기준**
- [ ] Chrome DevTools에서 X25519MLKEM768 협상 스크린샷 + tshark 패킷 캡처 확보
- [ ] 고전 vs 하이브리드 오버헤드 비교표 블로그 게시
- [ ] ML-DSA 사설 CA로 mTLS 핸드셰이크 동작 확인 및 인증서 크기 비교표 작성
- [ ] QuSign `SignatureAlgorithm` 인터페이스 추출 및 알고리즘 교체 가능 구조 구현

---

**8단계 완료 기준**
- [ ] Loki + Grafana 운영 중
- [ ] GitHub README 완성
- [ ] 이직 서류 제출

---

## 마일스톤 체크

| 시점 | 목표 | 완료 |
|------|------|------|
| 2주 | Kotlin + Spring Boot 실행 확인 | ✅ |
| 4주 | ML-DSA 서명 / 검증 단위 테스트 통과 | ✅ |
| 6주 | PDF ML-DSA 서명값 삽입 / 추출 성공 | ✅ |
| 3개월 | 백엔드 API 전체 완성 | ✅ |
| 5개월 | Vue 3 프론트 구현 완성 | ✅ |
| 6개월 | 기능 고도화 & 품질 강화 완료 | ⬜ |
| 6.5개월 | 보안 취약점 개선 완료 (Critical/High 0건) | ⬜ |
| 8개월 | AWS 배포 완료 + SES 이메일 + GitHub Actions | ⬜ |
| 10개월 | Terraform 코드화 완료 + 유료 플랜 출시 | ⬜ |
| 12개월 | Loki + Grafana 운영 + 이직 지원 시작 | ⬜ |

---

## 자격증 체크

| 순서 | 자격증 | 목표 시점 | 완료 |
|------|--------|-----------|------|
| 1 | AWS SAA | 7~8개월 | ⬜ |
| 2 | 정보보안기사 | 이직 후 | ⬜ |

---

## 기타 요구사항 (미분류 / 차후 개발 예정)

> 개발 중 발견된 누락 기능, 개선 아이디어, UX 요구사항을 기록한다.  
> 단계에 배정되지 않은 항목은 여기서 관리하다가 로드맵 정기 리뷰 시 해당 단계로 편입한다.

### 계획 외 구현 완료 항목

> 4단계 진행 중 계획에 없던 기능이 추가 구현됨. 4단계 완료 후 로드맵 정기 리뷰 시 해당 단계로 편입한다.

- [x] **보낸·받은 서명 요청 통합 대시보드** (`UnifiedDashboardView.vue`) — 요청자·서명자 시점을 하나의 화면에서 전환
- [x] **받은 문서 전용 화면** (`ReceivedDocumentsView.vue`, `ReceivedDetailView.vue`) + `GET /api/signature-requests/received` API
- [x] **자리비움 자동 잠금 기능** — 일정 시간 미조작 시 자동 로그아웃
- [x] **서명 요청 message 필드** — 요청자가 서명자에게 메모 전달, 서명자 화면에 표시
- [x] **이미 서명된 PDF 재업로드 차단** — SHA3-256 해시 중복 검사로 서명 완료 문서 재요청 방지
- [x] **검증 페이지 로그인 시 공통 헤더 표시** — 비로그인/로그인 상태 분기 처리
- [x] **대시보드 요약·액션 API 확장** (`GET /api/dashboard/summary`, `/api/dashboard/actions`) — 홈 화면 집계·할 일 섹션 연동

---

### 서명 거절 (Signer-initiated Rejection)

> 현재는 **요청자가 취소**하는 흐름(4-6)만 구현됨.  
> 서명자가 스스로 거절하는 흐름은 미구현 상태.

- [ ] **DB** — `REJECTED` 상태값 추가 (현재 `PENDING / SIGNED / CANCELLED / EXPIRED` 4종)
- [ ] **백엔드** — `POST /api/signature-requests/{token}/reject` 엔드포인트
  - 서명자 본인 + PENDING 상태만 허용
  - 선택적 사유(reason) 필드 수신
  - 요청자에게 `SIGN_REJECTED` 알림 발송
- [ ] **백엔드** — `SIGN_REJECTED` 알림 타입 추가 (`NotificationService`)
- [ ] **프론트엔드 (서명자)** — SignerView에 "서명 거절" 버튼 + 사유 입력 모달
- [ ] **프론트엔드 (요청자)** — DocumentDetailView에 `REJECTED` 상태 배지 및 사유 표시
- [ ] `./gradlew test` 통과

---

## 막혔을 때 체크리스트

- [ ] Docker 로그 확인 (`docker compose logs -f`)
- [ ] liboqs 빌드 에러 → OQS 공식 이슈 트래커 확인
- [ ] PDFBox 서명 에러 → PDFBox 공식 예제 코드와 비교
- [ ] AWS 배포 에러 → CloudWatch 로그 확인
- [ ] Terraform 에러 → `terraform plan`으로 변경사항 먼저 확인
