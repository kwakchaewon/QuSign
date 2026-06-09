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
| 5단계 | 보안 취약점 개선 (OWASP Top 10) | ⬜ 진행 전 |
| 6단계 | AWS 배포 + SES + GitHub Actions | ⬜ 진행 전 |
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

- [ ] `V9__add_audit_logs.sql` — `audit_logs` 테이블
  - 컬럼: `id`, `event_type`, `actor_email`, `signature_request_id (nullable)`,
    `bundle_id (nullable)`, `document_id (nullable)`, `ip_address (IPv6 대비 45자)`,
    `user_agent (500자)`, `created_at DATETIME(6)`
  - 인덱스: `(document_id)`, `(bundle_id)`, `(actor_email)`, `(created_at)`
  - **`UPDATE` · `DELETE` 문 없음** — append-only 테이블 (법적 불변성 요건)
  - `created_at`은 애플리케이션에서 `Instant.now(ZoneOffset.UTC)` 고정 (서버 시각 조작 방지)
- [ ] `AuditLog` 엔티티 + `AuditEventType` enum
  ```
  SIGN_REQUEST_CREATED       — 단건 서명 요청 생성
  BUNDLE_REQUEST_CREATED     — 번들 서명 요청 생성
  SIGNED                     — 단건 서명 완료
  BUNDLE_SIGNED              — 번들 서명 완료
  SIGNER_CANCELLED           — 단건 서명자 취소
  BUNDLE_SIGNER_CANCELLED    — 번들 서명자 취소
  SIGNED_DOCUMENT_DOWNLOADED — 서명된 문서 다운로드 (증거 체인)
  ```
- [ ] `AuditLogRepository` + `AuditLogService`
  - `save()` 만 노출 — **수정·삭제 메서드 미작성** (불변성 강제)
  - `findByDocumentId(documentId)` — 시간 오름차순
  - `findByBundleId(bundleId)` — 시간 오름차순

#### Step 2. 백엔드 — 감사 기록 포인트

- [ ] `AuditContext(ipAddress, userAgent)` data class 추가
- [ ] `SignatureController`에서 `HttpServletRequest`로 IP/UserAgent 추출 → `AuditContext`로 서비스에 전달
- [ ] `SignatureFlowService` 메서드 시그니처에 `auditCtx` 파라미터 추가 (6곳)
  - `requestSignatureForUser()` → `SIGN_REQUEST_CREATED`
  - `requestBundleSignature()` → `BUNDLE_REQUEST_CREATED`
  - `sign()` 단건 경로 → `SIGNED`
  - `signBundle()` → `BUNDLE_SIGNED`
  - `cancelSigner()` → `SIGNER_CANCELLED`
  - `cancelBundleSigner()` → `BUNDLE_SIGNER_CANCELLED`
- [ ] `SignatureController` 다운로드 4개 엔드포인트에 `SIGNED_DOCUMENT_DOWNLOADED` 기록
  - `getSignedDocument`, `getSignedBundleDocument`, `getSignedDocumentByRequester`, `getBundleSignedDocByRequester`

#### Step 3. PDF 메타데이터

- [ ] `PdfSignatureService.embedSignature()` 인터페이스에 `ipAddress` 파라미터 추가
- [ ] `PdfBoxSignatureService` 구현체에서 서명자 IP·서명 일시를 PDF 메타데이터에 삽입
  - 검증(`extractMetadata`, `verify`) 로직은 변경 없음

#### Step 4. API

- [ ] `GET /api/documents/{documentId}/audit` — 단건 감사 로그 (요청자 본인만)
- [ ] `GET /api/bundles/{bundleId}/audit` — 번들 감사 로그 (요청자 본인만)
- [ ] `GET /api/documents/{documentId}/audit/export` — JSON 내보내기 (법적 분쟁 제출용, 요청자 본인만)
- [ ] `GET /api/bundles/{bundleId}/audit/export` — 번들 감사 로그 내보내기
- [ ] `AuditLogResponse` DTO — `id`, `eventType`, `actorEmail`, `ipAddress`, `createdAt`
  - `userAgent`는 내부 기록용으로만 사용, API 응답에서 제외

#### Step 5. 프론트엔드

- [ ] 공용 컴포넌트 `AuditTimeline.vue` 제작 (props: `auditLogs`)
- [ ] `DocumentDetailView.vue` 하단에 `AuditTimeline` 삽입
- [ ] `BundleDetailView.vue` 하단에 `AuditTimeline` 삽입

#### Step 6. 테스트

- [ ] 단위 테스트: `AuditLogService` — 각 이벤트 타입 기록 검증, 삭제 메서드 미노출 확인
- [ ] 통합 테스트: sign 엔드포인트 호출 → audit API 조회 → 로그 존재 확인
- [ ] `./gradlew test` 통과

#### Step 7. 운영 정책 (법적 의무)

- [ ] **보존 기간 10년** 명시 — 전자서명법 제31조 기준, 자동 삭제·TTL 설정 금지
- [ ] **약관 수정** — 회원가입 약관에 감사 로그 수집 목적·보유 기간 명시
  - 문구 예시: *"서비스 이용 중 발생하는 서명 이벤트 및 접속 IP는 인증 보안 감시·서명 무결성 검증·분쟁 해결 목적으로 10년간 보관됩니다."*
  - 근거: 개인정보보호법 제6조 (수집 동의 근거 명시 의무)
- [ ] **KISA RFC 3161 타임스탬프 연동 검토** — 서버 시각 대신 공인 시각 사용 (eIDAS·법원 제출 시 증거력 강화, 3단계 이후 적용)

**완료 기준:** 서명 완료 PDF에 감사 정보(IP·일시) 포함 + 상세 페이지에서 이벤트 타임라인 조회 + 다운로드 이력 기록 + 감사 로그 JSON 내보내기 + 약관 IP 수집 동의 문구 반영

---

### 4-9. 관리자 페이지

**목표:** 감사 로그·사용자·서명 현황을 관리자가 시스템 전체 범위에서 조회·관리할 수 있는 어드민 패널을 구축한다

> 4-8 감사 로그 구현 완료 후 진행

#### Step 1. 권한 모델 확장

- [ ] `V10__add_role_to_users.sql` — `users.role VARCHAR(20) DEFAULT 'USER'`
- [ ] `User` 엔티티에 `role: String` 필드 추가
- [ ] `SecurityConfig` — `/api/admin/**` 경로를 `ROLE_ADMIN`만 접근 허용
- [ ] 초기 관리자 계정 등록 방법 명세 (환경변수 또는 초기 데이터 SQL)

#### Step 2. 백엔드 — 관리자 API

- [ ] `GET /api/admin/stats` — 시스템 전체 통계 (총 사용자·서명 수, 상태별 카운트)
- [ ] `GET /api/admin/users` — 전체 사용자 목록 (페이징, 이메일 검색)
- [ ] `PUT /api/admin/users/{email}/disable` — 사용자 비활성화 (소프트, 로그인 차단)
- [ ] `GET /api/admin/audit` — 시스템 전체 감사 로그 (페이징, `eventType`·날짜 필터)
  - 4-8 `AuditLogService`에 `findAll(pageable, filter)` 메서드 추가
- [ ] `GET /api/admin/users/{email}/audit` — 특정 사용자의 감사 로그 전체 조회
- [ ] `GET /api/admin/audit/export` — 전체 감사 로그 JSON 내보내기 (법적 분쟁·감사 대응)
- [ ] 모든 관리자 API에 `@PreAuthorize("hasRole('ADMIN')")` 적용

#### Step 3. 프론트엔드

- [ ] 라우터 가드: `/admin/**` → `ADMIN` 역할 없으면 `/home` 리다이렉트
- [ ] `AdminLayout.vue` — 관리자 전용 레이아웃 (사이드바: 통계 / 사용자 / 감사 로그)
- [ ] `AdminStatsView.vue` (`/admin`) — 통계 카드 + 최근 감사 이벤트 요약
- [ ] `AdminUsersView.vue` (`/admin/users`) — 사용자 목록 테이블 (이메일 검색, 비활성화 버튼)
- [ ] `AdminAuditView.vue` (`/admin/audit`) — 전체 감사 로그 테이블 (이벤트 타입·날짜 필터, 내보내기 버튼)
  - 4-8의 `AuditTimeline.vue` 컴포넌트 재사용

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

- [ ] `/security-review` 스킬로 현재 브랜치 전체 보안 리뷰
- [ ] A01 — 접근 제어 (JWT 권한 검사, 토큰 범위 제한)
- [ ] A02 — 암호화 실패 (개인키 메모리 잔류, 전송 중 평문 노출)
- [ ] A03 — 인젝션 (SQL/NoSQL, API 파라미터 검증)
- [ ] A05 — 보안 설정 오류 (CORS, HTTPS only, 쿠키 플래그)
- [ ] A07 — 인증/세션 관리 (토큰 만료, 재사용 방지)
- [ ] A09 — 보안 로깅 부족 (서명 이벤트 감사 로그 확인)
- [ ] 프론트엔드 XSS / CSRF 점검
- [ ] API 응답 민감 필드 노출 여부 점검
- [ ] Critical / High 항목 전부 수정

**5단계 완료 기준:** Critical / High 취약점 0건

---

## 6단계: AWS 배포 + SES + GitHub Actions
> 기간: 이후 / (구 5단계)

### 6-1. AWS 인프라 콘솔 구성

- [ ] VPC 생성 (퍼블릭 / 프라이빗 서브넷 분리)
- [ ] 보안 그룹 생성 (EC2, RDS 각각)
- [ ] EC2 생성 + Docker 설치
- [ ] RDS MariaDB 생성 (프라이빗 서브넷)
- [ ] S3 버킷 생성 + IAM 정책 설정
- [ ] IAM 역할 생성 (EC2 → S3, SES 접근)

---

### 6-2. AWS SES 이메일 연동

> 로컬은 콘솔 로그 출력으로 완료. 이 단계에서 실제 발송으로 전환한다.

- [ ] SES 샌드박스 설정 및 이메일 도메인 인증
- [ ] `SesEmailService` 실제 구현 (SesClient + HTML 템플릿)
- [ ] 서명 요청 이메일 + 서명 완료 이메일 템플릿 작성
- [ ] 실제 이메일 수신 테스트

---

### 6-3. GitHub Actions CI/CD

- [ ] workflow 파일 작성 (`push → main → 빌드 → Docker → ECR → EC2 배포`)
- [ ] GitHub Secrets 등록 (AWS 키, EC2 SSH 키 등)
- [ ] 파이프라인 동작 확인

---

### 6-4. 배포 마무리

- [ ] MinIO → AWS S3 전환
- [ ] 로컬 DB → AWS RDS 전환
- [ ] 도메인 구매 + Route53 연결
- [ ] HTTPS 설정 (Let's Encrypt)
- [ ] 베타 사용자 10명 모집

**6단계 완료 기준**
- [ ] 실제 도메인으로 HTTPS 접속 가능
- [ ] 이메일로 서명 링크 수신 후 서명까지 전체 플로우 동작
- [ ] GitHub Actions 푸시 시 자동 배포

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
