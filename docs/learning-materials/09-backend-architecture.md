# 백엔드 아키텍처 — JWT · JPA · 레이어 분리 (2단계)

> PLAN.md §2 대응 — Spring Boot 백엔드 핵심 구현 개념 정리

---

## 이론

### 레이어 아키텍처

```
Controller  ─ HTTP 요청/응답 매핑, @Valid 검증, 인증 컨텍스트 추출
Service     ─ 비즈니스 로직, 트랜잭션 경계 (@Transactional)
Repository  ─ DB 쿼리 (Spring Data JPA)
Entity      ─ DB 테이블 매핑 (@Entity)
DTO         ─ 요청/응답 전용 데이터 클래스 (Entity 직접 노출 금지)
```

레이어 간 의존 방향은 위 → 아래 단방향입니다.
Service가 다른 Service를 호출할 수 있지만, Repository가 Service를 호출하면 안 됩니다.

### JPA / Spring Data JPA

JPA(Java Persistence API)는 객체-관계 매핑 표준입니다.
Hibernate가 JPA 구현체이고, Spring Data JPA는 Repository 보일러플레이트를 자동 생성합니다.

```kotlin
@Entity
@Table(name = "users")
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = false)
    var passwordHash: String,

    var deletedAt: LocalDateTime? = null   // soft delete
)
```

```kotlin
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
}
```

Spring Data JPA는 메서드 이름을 분석해 SQL을 자동 생성합니다.
복잡한 쿼리는 `@Query("SELECT u FROM User u WHERE ...")` JPQL로 작성합니다.

### JWT (JSON Web Token) 인증 흐름

```
1. POST /api/auth/login  →  이메일·비밀번호 검증
2. 성공 시 JWT 발급 (서명: HMAC-SHA256, 만료: 24h)
3. 이후 요청 시 Authorization: Bearer <token> 헤더 포함
4. JwtAuthenticationFilter가 토큰 검증 → SecurityContext에 사용자 설정
5. Controller에서 @AuthenticationPrincipal 또는 SecurityContextHolder로 사용자 조회
```

```kotlin
// JWT 구조: header.payload.signature
// payload 예시
{
  "sub": "user@example.com",   // subject (식별자)
  "iat": 1736914800,           // issued at
  "exp": 1737001200            // expiration
}
```

### Spring Security 필터 체인

```
요청 → JwtAuthenticationFilter → UsernamePasswordAuthenticationFilter → ...
         ↓ 토큰 검증
         SecurityContextHolder.setAuthentication(...)
                               ↓
                         Controller (@AuthenticationPrincipal)
```

`JwtAuthenticationFilter`는 `OncePerRequestFilter`를 상속합니다.
요청마다 1번만 실행되며 토큰 유효성 검사 후 인증 정보를 설정합니다.

### @Transactional

```kotlin
@Service
@Transactional
class SignatureFlowService(...) {

    fun requestSignature(dto: SignatureRequestDto, email: String) {
        val user = userRepository.findByEmail(email) ?: throw UserNotFoundException()
        val document = documentRepository.findById(dto.documentId) ?: throw DocumentNotFoundException()
        // 여러 DB 작업이 하나의 트랜잭션으로 묶임
        // 중간에 예외 발생 시 전체 롤백
        val signatureRequest = signatureRequestRepository.save(...)
        notificationService.createAndPublish(...)
    }
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

### 인터페이스 분리 예시

```kotlin
// 추상화 — 로컬/프로덕션 전환 가능
interface StorageService {
    fun upload(key: String, bytes: ByteArray, contentType: String)
    fun download(key: String): ByteArray
    fun delete(key: String)
}

@Service
@Profile("local")
class MinioStorageService(private val minioClient: MinioClient) : StorageService { ... }

@Service
@Profile("prod")
class S3StorageService(private val s3Client: S3Client) : StorageService { ... }
```

---

## 확인 질문 & 답변

**Q1. Entity를 Controller 응답으로 직접 반환하면 안 되는 이유는?**

> Entity에는 `passwordHash`, `privateKeyEncrypted` 같은 내부 필드가 있습니다. 직접 반환하면 민감 정보가 노출됩니다. DTO는 응답에 필요한 필드만 포함하여 API 계약을 명확히 합니다. Entity 구조가 바뀌어도 DTO를 유지하면 하위 호환을 지킬 수 있습니다.

**Q2. `@Transactional` 없이 여러 Repository 저장을 호출하면?**

> 각 `save()` 호출이 별도 트랜잭션으로 커밋됩니다. 중간에 예외가 발생하면 일부만 저장된 불일치 상태가 됩니다. `@Transactional`을 붙이면 메서드 전체가 하나의 트랜잭션으로 묶이고, `RuntimeException` 발생 시 전체 롤백됩니다.

**Q3. JWT가 DB 조회 없이도 사용자를 인증할 수 있는 원리는?**

> JWT는 서버의 비밀 키(HMAC-SHA256)로 서명됩니다. 서버는 토큰이 도착하면 서명을 검증해 위변조 여부를 확인합니다. 서명이 유효하면 payload 안의 이메일을 신뢰할 수 있습니다. DB 조회 없이도 인증이 가능하지만, 비활성/탈퇴 계정 차단을 위해 QuSign은 추가로 DB를 조회합니다.

**Q4. `@Profile("local")`과 `@Profile("prod")`를 쓰는 이유는?**

> 동일 인터페이스의 구현체를 프로파일로 조건부 등록합니다. `local` 프로파일에서는 MinIO 구현체가, `prod`에서는 S3 구현체가 빈으로 등록됩니다. 서비스 코드는 `StorageService` 인터페이스만 의존하므로 변경이 없습니다.
