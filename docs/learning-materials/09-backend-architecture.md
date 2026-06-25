# 백엔드 아키텍처 — JWT · JPA · 레이어 분리 (2단계)

> PLAN.md §2 대응 — Spring Boot 백엔드 핵심 구현 개념 정리

---

## 이론

### 레이어 아키텍처와 의존성 방향

#### 레이어 구조

```
┌─────────────────────────────────────────────┐
│  Controller  — HTTP 요청/응답, @Valid 검증   │  ← 외부 경계
├─────────────────────────────────────────────┤
│  Service     — 비즈니스 로직, 트랜잭션 경계  │  ← 핵심 로직
├─────────────────────────────────────────────┤
│  Repository  — DB 쿼리 (Spring Data JPA)    │  ← 데이터 접근
├─────────────────────────────────────────────┤
│  Entity      — DB 테이블 객체 매핑           │  ← 영속 모델
└─────────────────────────────────────────────┘
```

의존 방향: 위 → 아래 **단방향**. Repository가 Service를 알면 안 됩니다.
DTO는 레이어 간 데이터 전달용으로 Entity와 분리합니다.

#### 왜 단방향 의존인가

```
❌ 잘못된 의존: Repository → Service 참조
  → Service가 바뀌면 Repository도 바뀜
  → 단위 테스트에서 Service 없이 Repository를 테스트할 수 없음

✅ 올바른 의존: Controller → Service → Repository
  → Repository는 Service를 모름 → DB 로직만 집중
  → Repository를 모킹하면 Service를 독립 테스트 가능
```

---

### JPA / Hibernate / Spring Data JPA 계층 이해

#### 세 기술의 관계

```
Spring Data JPA (자동 쿼리 생성, Repository 인터페이스)
      ↓ 사용
JPA 표준 API (EntityManager, JPQL)
      ↓ 구현체
Hibernate (실제 SQL 생성·실행, 캐시, 세션 관리)
      ↓ 실행
JDBC (DB 커넥션)
      ↓ 연결
MariaDB
```

#### Hibernate 1차 캐시 (영속성 컨텍스트)

```kotlin
@Transactional
fun updateUser(email: String, newName: String) {
    val user = userRepository.findByEmail(email)!!  // DB 조회 → 1차 캐시에 저장
    user.name = newName                              // 객체 속성 변경

    // save() 호출 없어도 트랜잭션 종료 시 자동 UPDATE (dirty checking)
}
```

Hibernate는 트랜잭션 내에서 조회한 Entity를 **영속성 컨텍스트**에 보관합니다.
트랜잭션 커밋 시점에 변경된 필드를 감지(dirty checking)하여 자동으로 UPDATE SQL을 실행합니다.

#### 동일 트랜잭션 내 동일 Entity 중복 조회

```kotlin
val user1 = userRepository.findByEmail("a@b.com")
val user2 = userRepository.findByEmail("a@b.com")
println(user1 === user2)  // true — 캐시에서 반환, DB 쿼리 1번만 실행
```

#### N+1 문제

```kotlin
// 위험: 문서 목록 조회 후 각 문서의 소유자를 별도 쿼리로 조회
val documents = documentRepository.findAll()       // 쿼리 1번
documents.forEach { doc -> println(doc.owner.email) }  // 문서 수만큼 쿼리

// 해결: JOIN FETCH로 한 번에 조회
@Query("SELECT d FROM Document d JOIN FETCH d.owner")
fun findAllWithOwner(): List<Document>
```

---

### JWT (JSON Web Token) 구조와 보안

#### JWT 구조 상세

```
header.payload.signature

header (Base64URL):
{
  "alg": "HS256",   // HMAC-SHA256
  "typ": "JWT"
}

payload (Base64URL):
{
  "sub": "user@example.com",   // subject — 사용자 식별자
  "iat": 1736914800,           // issued at — 발급 시각 (Unix timestamp)
  "exp": 1737001200,           // expiration — 만료 시각
  "role": "USER"               // 커스텀 클레임
}

signature:
HMAC-SHA256(Base64URL(header) + "." + Base64URL(payload), SECRET_KEY)
```

#### JWT 검증 원리

```
서버 수신 토큰: header.payload.signature

1. header + payload를 SECRET_KEY로 HMAC-SHA256 계산 → expected_sig
2. expected_sig == signature → 위변조 없음 확인
3. payload.exp > now() → 만료 여부 확인
4. payload.sub로 사용자 식별
```

SECRET_KEY를 모르면 payload를 변경해도 signature가 일치하지 않아 검증 실패합니다.

#### JWT vs 세션(Session) 비교

| | Session | JWT |
|---|---|---|
| 상태 저장 | 서버 (메모리/DB) | 클라이언트 (토큰 자체) |
| 확장성 | 세션 서버 필요 | Stateless — 어느 서버에서도 검증 |
| 즉시 무효화 | 가능 (세션 삭제) | 만료까지 기다려야 함 |
| CSRF 위험 | 쿠키 기반 → 있음 | 헤더 기반 → 없음 |
| 토큰 크기 | 세션 ID (작음) | Base64 payload (크지 않음) |

QuSign은 Stateless JWT를 선택하여 수평 확장이 용이합니다.
단, 비활성/탈퇴 계정 즉시 차단을 위해 JWT 필터에서 DB를 추가 조회합니다.

---

### Spring Security 필터 체인 상세

#### 필터 실행 순서

```
HTTP 요청
  │
  ▼
SecurityContextPersistenceFilter  ← SecurityContext 로드 (Stateless라 스킵)
  │
  ▼
JwtAuthenticationFilter (커스텀)  ← JWT 검증 → SecurityContext 설정
  │
  ▼
UsernamePasswordAuthenticationFilter  ← /api/auth/login 처리
  │
  ▼
ExceptionTranslationFilter  ← 인증/인가 예외 → 401/403
  │
  ▼
FilterSecurityInterceptor  ← 경로별 권한 체크
  │
  ▼
DispatcherServlet → Controller
```

#### SecurityContext와 스레드 로컬

```kotlin
// JwtAuthenticationFilter에서 설정
SecurityContextHolder.getContext()
    .authentication = UsernamePasswordAuthenticationToken(email, null, authorities)

// Controller에서 꺼냄
@GetMapping("/api/documents")
fun getDocuments(@AuthenticationPrincipal email: String): List<DocumentResponse> {
    // Spring이 SecurityContext에서 email을 꺼내 주입
}
```

`SecurityContextHolder`는 기본적으로 `ThreadLocal`을 사용합니다.
요청을 처리하는 스레드에 인증 정보를 저장하고, 요청 완료 시 자동 삭제합니다.

---

### @Transactional — 트랜잭션 전파와 격리 수준

#### 트랜잭션 전파(Propagation)

```kotlin
@Service
class SignatureFlowService(
    private val notificationService: NotificationService
) {

    @Transactional  // 기본: PROPAGATION_REQUIRED
    fun sign(...) {
        // 트랜잭션 A 시작
        signatureRepository.save(...)    // 트랜잭션 A에 참여
        notificationService.createAndPublish(...)  // 아래 설명
        // 트랜잭션 A 커밋
    }
}

@Service
class NotificationService {

    @Transactional  // PROPAGATION_REQUIRED — 기존 트랜잭션 A에 참여
    fun createAndPublish(...) {
        notificationRepository.save(...)  // 트랜잭션 A에 함께 커밋
        redisTemplate.convertAndSend(...) // Redis는 트랜잭션 밖
    }
}
```

**PROPAGATION_REQUIRED**: 트랜잭션이 있으면 참여, 없으면 새로 생성.
**PROPAGATION_REQUIRES_NEW**: 항상 새 트랜잭션 (감사 로그처럼 본 트랜잭션 롤백 시에도 기록 유지할 때).

#### 트랜잭션 격리 수준

| 수준 | Dirty Read | Non-Repeatable Read | Phantom Read |
|---|---|---|---|
| READ_UNCOMMITTED | 가능 | 가능 | 가능 |
| READ_COMMITTED | 불가 | 가능 | 가능 |
| **REPEATABLE_READ** | 불가 | 불가 | 가능 |
| SERIALIZABLE | 불가 | 불가 | 불가 |

MariaDB 기본값은 `REPEATABLE_READ`입니다.
같은 트랜잭션 내에서 같은 행을 다시 읽어도 동일한 값을 보장합니다.

---

### Spring Bean과 의존성 주입

#### Spring IoC Container

```kotlin
// @Service, @Repository, @Controller, @Component → 빈으로 등록

@Service
class AuthService(
    private val userRepository: UserRepository,        // 생성자 주입 (권장)
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {
    // Spring이 생성 시 의존 빈을 자동 주입
}
```

**생성자 주입이 필드 주입(@Autowired)보다 나은 이유**:
- `val`(불변)로 선언 가능 → 런타임 중 교체 불가
- 테스트에서 명시적으로 목(mock)을 전달 가능
- 순환 의존성을 컴파일 타임에 감지

#### @Bean vs @Component

```kotlin
// @Component: 클래스에 직접 붙임 (내가 만든 클래스)
@Service class AuthService(...)

// @Bean: 메서드에 붙임 (외부 라이브러리 클래스를 빈으로 등록)
@Configuration
class AppConfig {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun jwtParser(): JwtParser = Jwts.parserBuilder()
        .setSigningKey(secretKey)
        .build()
}
```

---

## QuSign 도메인 구조

```
auth/          ← 회원가입, 로그인, JWT
document/      ← PDF 업로드, 목록, 다운로드, 대시보드
signature/     ← 서명 요청, 서명 실행, 검증, 취소, 감사 로그
notification/  ← Redis Pub/Sub + SSE 알림
admin/         ← 관리자 API
```

### 인터페이스 분리 — 환경별 전략 패턴

```kotlin
// 추상화
interface StorageService {
    fun upload(key: String, bytes: ByteArray, contentType: String)
    fun download(key: String): ByteArray
    fun delete(key: String)
}

// 로컬 환경 구현체
@Service
@Profile("local")
class MinioStorageService(private val minioClient: MinioClient) : StorageService {
    override fun upload(key: String, bytes: ByteArray, contentType: String) {
        minioClient.putObject(PutObjectArgs.builder()
            .bucket("qusign-local").`object`(key)
            .stream(bytes.inputStream(), bytes.size.toLong(), -1)
            .contentType(contentType).build())
    }
}

// 프로덕션 환경 구현체
@Service
@Profile("prod")
class S3StorageService(private val s3Client: S3Client) : StorageService {
    override fun upload(key: String, bytes: ByteArray, contentType: String) {
        s3Client.putObject(PutObjectRequest.builder()
            .bucket(bucketName).key(key)
            .contentType(contentType).build(),
            RequestBody.fromBytes(bytes))
    }
}
```

서비스 코드는 `StorageService` 인터페이스만 의존 → 환경 전환 시 코드 변경 없음.

---

## 확인 질문 & 답변

**Q1. Entity를 Controller 응답으로 직접 반환하면 안 되는 이유는?**

> Entity에는 `passwordHash`, `privateKeyEncrypted`, `deletedAt` 같은 내부 필드가 있습니다. 직접 반환하면 민감 정보가 노출됩니다. 또한 JPA 지연 로딩(Lazy Loading)으로 인해 직렬화 시 예상치 못한 추가 쿼리나 `LazyInitializationException`이 발생할 수 있습니다. DTO는 응답에 필요한 필드만 포함하여 API 계약을 명확히 하고 Entity 변경으로부터 API를 보호합니다.

**Q2. `@Transactional` 없이 여러 Repository 저장을 호출하면?**

> 각 `save()` 호출이 별도 트랜잭션으로 커밋됩니다. 중간에 예외가 발생하면 이미 커밋된 데이터는 롤백되지 않아 불일치 상태가 됩니다. 예: `signatureRepository.save()` 성공 후 `auditLogRepository.save()` 실패 → 서명 기록은 있지만 감사 로그 없음. `@Transactional`을 붙이면 메서드 전체가 하나의 원자적 단위가 됩니다.

**Q3. JWT가 DB 조회 없이도 사용자를 인증할 수 있는 원리는?**

> JWT는 서버의 비밀 키(HMAC-SHA256)로 서명됩니다. 수신된 토큰의 header+payload를 같은 키로 다시 서명하면 동일한 signature가 나와야 합니다. 다르면 위변조입니다. 서명이 유효하면 payload의 이메일을 신뢰할 수 있습니다. DB 조회 없이도 인증이 가능하지만, 비활성/탈퇴 계정의 유효한 JWT를 즉시 차단하려면 DB 조회가 필요합니다.

**Q4. Hibernate dirty checking이 `save()` 없이 UPDATE를 실행하는 원리는?**

> Hibernate는 영속성 컨텍스트에 Entity를 로드할 때 원본 스냅샷을 보관합니다. 트랜잭션 커밋 시 현재 Entity 상태와 스냅샷을 비교하여 변경된 필드를 감지합니다. 변경이 있으면 해당 필드만 UPDATE SQL로 실행합니다. 이를 `save()` 없이도 변경이 반영되는 이유입니다. 단, `@Transactional` 범위 안에서만 동작합니다.

**Q5. Spring 생성자 주입에서 `@Autowired`를 생략할 수 있는 이유는?**

> Spring 4.3부터 생성자가 하나뿐인 경우 `@Autowired`를 생략해도 자동 주입됩니다. Kotlin에서 `class Service(private val repo: Repository)`처럼 선언하면 Spring이 repo에 해당하는 빈을 자동으로 주입합니다. `@Autowired` 생략은 관례적으로 권장됩니다.
