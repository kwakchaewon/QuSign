# OWASP Top 10 — 보안 취약점 개선 (5단계)

> PLAN.md §5 대응 — QuSign에 실제 적용된 항목 중심

---

## 개요

OWASP(Open Web Application Security Project)는 웹 앱 보안 위협 TOP 10을 발표합니다.
QuSign은 5단계에서 이 체크리스트를 기준으로 전 영역을 점검·수정했습니다.

---

## A01 — 접근 제어 실패 (Broken Access Control)

**위협**: 인증됐지만 권한 없는 리소스에 접근

**QuSign 적용**
```kotlin
// 비활성/탈퇴 계정 즉시 차단 (JwtAuthenticationFilter)
if (user.disabled || user.deletedAt != null) {
    response.sendError(401); return
}

// 타인 문서 접근 차단 (DocumentService)
val document = documentRepository.findById(id)
    ?: throw DocumentNotFoundException()
if (document.ownerEmail != currentUserEmail) {
    throw AccessDeniedException()  // 403
}

// 관리자 경로 보호
@PreAuthorize("hasRole('ADMIN')")
fun getAdminStats(): AdminStatsResponse { ... }
```

---

## A02 — 암호화 실패 (Cryptographic Failures)

**위협**: 민감 데이터 평문 저장·전송

**QuSign 적용**

| 항목 | 구현 |
|---|---|
| 비밀번호 | BCrypt (cost factor 10) |
| 개인키 | PBKDF2 → AES-256-GCM 암호화 후 DB 저장 |
| 전송 계층 | HTTPS (Let's Encrypt) |
| 서명 후 키 | 메모리 zeroing (`Arrays.fill(keyBytes, 0)`) |

```kotlin
// 개인키 메모리 zeroing (서명 완료 후)
val privateKeyBytes = decryptPrivateKey(encryptedKey, password)
try {
    val signature = pqcService.sign(privateKeyBytes, documentHash)
    // ... 서명 처리
} finally {
    Arrays.fill(privateKeyBytes, 0)  // 메모리에서 즉시 제거
}
```

---

## A03 — 인젝션 (Injection)

**위협**: SQL 인젝션, 명령 인젝션

**QuSign 적용**
```kotlin
// JPA + JPQL 사용 → SQL 인젝션 불가
fun findByEmail(email: String): User?  // PreparedStatement 자동 생성

// @Valid 입력 검증
data class RegisterRequest(
    @field:Email val email: String,
    @field:Size(min = 8) @field:Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$",
        message = "영문자와 숫자를 포함해야 합니다"
    )
    val password: String
)
```

PDF 매직 바이트 검증 — Content-Type 위조 방지:
```kotlin
fun validatePdf(bytes: ByteArray) {
    val magic = bytes.take(4).toByteArray()
    require(magic.contentEquals("%PDF".toByteArray())) {
        "유효한 PDF 파일이 아닙니다"
    }
}
```

---

## A05 — 보안 설정 오류 (Security Misconfiguration)

**위협**: 기본 자격증명, 불필요한 포트 노출, 보안 헤더 누락

**QuSign 적용**
```kotlin
// HTTP 보안 헤더
http.headers { headers ->
    headers
        .frameOptions { it.deny() }           // Clickjacking 방지
        .contentTypeOptions { }               // MIME 스니핑 방지
        .xssProtection { }
        .httpStrictTransportSecurity { it.includeSubDomains(true).maxAgeInSeconds(31536000) }
}

// CORS — 환경변수 기반 설정
@Value("\${CORS_ORIGINS}") private val allowedOrigins: String

// X-Forwarded-For 신뢰 — Nginx 프록시만 신뢰
http.remoteIpTrustedProxies("127.0.0.1")
```

---

## A07 — 인증/세션 관리 실패

**위협**: 세션 고정, 브루트포스, 토큰 노출

**QuSign 적용**
```kotlin
// SSE 단기 토큰 — JWT URL 파라미터 노출 방지
@PostMapping("/api/notifications/sse-token")
fun issueSseToken(authentication: Authentication): ApiResponse<SseTokenResponse> {
    val userId = resolveUserId(authentication)
    return ApiResponse.ok(SseTokenResponse(sseTokenService.issue(userId)))
}

// SseTokenService — Redis에 "sse-token:{uuid}" → userId, TTL 30초로 저장
fun issue(userId: Long): String {
    val token = UUID.randomUUID().toString()
    redisTemplate.opsForValue().set("sse-token:$token", userId.toString(), Duration.ofSeconds(30))
    return token
}

// SSE 엔드포인트는 단기 토큰으로 인증 (URL에 JWT 노출 없음)
@GetMapping("/api/notifications/stream")
fun stream(@RequestParam token: String?): SseEmitter {
    val userId = sseTokenService.consume(token)  // getAndDelete — 1회용 소비
        ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않거나 만료된 토큰입니다.")
    return registry.register(userId)
}
```
(토큰 발급은 `POST`, 토큰 조회+삭제는 `GET`으로 분리되어 있고, 실제 구현은 `SseTokenService`에 캡슐화되어 있습니다. — [[12-redis-sse]] 참고)

---

## A09 — 보안 로깅 실패

**위협**: 이상 접근 감지 불가, 법적 증거 부재

**QuSign 적용 — 감사 로그 설계**

```kotlin
// append-only 테이블 (UPDATE/DELETE 없음)
@Entity
@Table(name = "audit_logs")
class AuditLog(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = 0,
    @Enumerated(EnumType.STRING) val eventType: AuditEventType,
    val actorEmail: String,
    val signatureRequestId: Long? = null,
    val bundleId: Long? = null,
    val documentId: Long? = null,
    val ipAddress: String,           // IPv6 대비 45자
    val userAgent: String,           // DB에만 저장 (API 응답 미포함)
    val createdAt: LocalDateTime,    // 서버 시각 (UTC 아님 — LocalDateTime, 애플리케이션 타임존 기준)
    val retainedUntil: LocalDateTime,  // createdAt + 10년, 삽입 후 변경 금지
)
```

기록 항목: `SIGN_REQUEST_CREATED`, `SIGNED`, `SIGNER_CANCELLED`, `SIGNED_DOCUMENT_DOWNLOADED` 등
보존 기간: 전자서명법 제31조 기준 10년 (`TTL·자동 삭제 설정 금지`)

---

## 프론트엔드 — XSS / CSRF

```
XSS  → Vue 3의 템플릿 바인딩은 기본으로 HTML 이스케이핑
       v-html 사용 시에는 반드시 DOMPurify로 sanitize 필요

CSRF → Stateless JWT(세션 없음) → CSRF 토큰 불필요
       쿠키에 JWT 저장 시에는 SameSite=Strict 필수
       QuSign은 localStorage에 저장 → CSRF 위협 없음 (대신 XSS 취약)
```

---

## 확인 질문 & 답변

**Q1. BCrypt 대신 SHA-256으로 비밀번호를 해시하면 왜 안전하지 않은가?**

> SHA-256은 범용 해시 함수로 GPU를 사용한 레인보우 테이블 공격에 취약합니다. BCrypt는 의도적으로 느린(cost factor) 알고리즘으로 브루트포스 공격 비용을 높이고, 솔트(salt)를 내장하여 동일 비밀번호도 다른 해시를 생성합니다.

**Q2. 개인키를 `Arrays.fill(privateKeyBytes, 0)`으로 zeroing하는 이유는?**

> Java GC는 메모리 해제 시점을 보장하지 않습니다. `privateKeyBytes = null`만 하면 GC 전까지 힙 메모리에 키 값이 남습니다. 메모리 덤프나 스왑 파티션 분석으로 키가 노출될 수 있습니다. `Arrays.fill`로 즉시 덮어써서 노출 시간을 최소화합니다.

**Q3. CORS 설정을 하드코딩하지 않고 환경변수로 주입하는 이유는?**

> 로컬(`localhost:5173`), 스테이징, 프로덕션(`https://qusign.link`) 각각 허용 오리진이 다릅니다. 환경변수로 주입하면 코드 변경 없이 배포 환경마다 다른 값을 사용할 수 있습니다. 하드코딩하면 코드에 도메인이 박혀 이식성이 떨어집니다.

**Q4. 감사 로그 테이블에 `UPDATE`와 `DELETE` 메서드를 작성하지 않는 이유는?**

> 전자서명법은 서명 이벤트 기록을 10년간 보관하고 위변조를 금지합니다. Repository에 수정/삭제 메서드가 없으면 애플리케이션 코드가 실수로 감사 로그를 수정하는 것을 원천 차단합니다. DB 수준에서는 INSERT 권한만 있는 별도 계정 사용도 고려할 수 있습니다.
