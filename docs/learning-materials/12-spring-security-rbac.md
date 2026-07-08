# Spring Security RBAC — 역할 기반 접근 제어 (4-9단계)

> PLAN.md §4-9 대응 — ROLE_USER / ROLE_ADMIN 권한 분리

---

## 이론

### RBAC (Role-Based Access Control)

사용자에게 역할(Role)을 부여하고, 역할에 권한(Permission)을 매핑하는 접근 제어 모델입니다.

```
사용자 → 역할(Role) → 권한(Permission)
kwak@   → ADMIN     → /api/admin/** 접근 가능
user@   → USER      → /api/admin/** 접근 불가
```

### Spring Security 권한 구조

```kotlin
// UserDetails 구현 — Spring Security가 인증에 사용
class CustomUserDetails(
    private val email: String,
    private val password: String,
    private val role: String  // "USER" or "ADMIN"
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_$role"))
        // Spring Security는 "ROLE_" 접두사를 role로 인식

    override fun getUsername() = email
    override fun getPassword() = password
    override fun isEnabled() = true
    // ...
}
```

### SecurityConfig — 경로별 권한 설정

```kotlin
@Configuration
@EnableMethodSecurity   // @PreAuthorize 활성화
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/**").permitAll()      // 인증 불필요
                    .requestMatchers("/api/verify/**").permitAll()    // 공개 검증 API
                    .requestMatchers("/api/admin/**").hasRole("ADMIN") // ADMIN만
                    .anyRequest().authenticated()                      // 나머지는 로그인 필요
            }
            .sessionManagement { it.sessionCreationPolicy(STATELESS) }
        return http.build()
    }
}
```

### @PreAuthorize — 메서드 수준 권한

```kotlin
@RestController
@RequestMapping("/api/admin")
class AdminController(private val adminService: AdminService) {

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")    // 메서드 진입 전 권한 검사
    fun getUsers(): List<UserResponse> = adminService.getAllUsers()

    @PutMapping("/users/{email}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    fun disableUser(@PathVariable email: String) = adminService.disableUser(email)
}
```

`@PreAuthorize`는 `@EnableMethodSecurity`가 활성화되어야 동작합니다.
경로 수준(`SecurityConfig`)과 메서드 수준(`@PreAuthorize`)을 함께 쓰면 이중 방어가 됩니다.

### AdminInitializer — 관리자 계정 자동 생성

```kotlin
@Component
class AdminInitializer(
    private val authService: AuthService,
    @Value("\${admin.email:}") private val adminEmail: String,
    @Value("\${admin.password:}") private val adminPassword: String,
) {
    @EventListener(ApplicationReadyEvent::class)
    fun init() {
        if (adminEmail.isBlank() || adminPassword.isBlank()) return
        authService.ensureAdmin(adminEmail, adminPassword)
    }
}

// AuthService.kt
fun ensureAdmin(email: String, password: String) {
    val user = userRepository.findByEmail(email) ?: run {
        register(email, password)
        userRepository.findByEmail(email)!!
    }
    if (user.role == "ADMIN") return
    user.role = "ADMIN"
    userRepository.save(user)
}
```

`@EventListener(ApplicationReadyEvent::class)`는 Spring Boot 시작이 완전히 끝난 직후 1회 실행됩니다.
`admin.email` / `admin.password` 프로퍼티(환경변수로는 `ADMIN_EMAIL`/`ADMIN_PASSWORD`가 매핑됨)가 없으면 관리자를 생성하지 않습니다.
계정 존재 여부 확인과 저장 로직은 `AuthService.ensureAdmin()`에 캡슐화되어 있습니다 — 계정이 없으면 새로 등록하고, 있는데 `ADMIN`이 아니면 역할만 승격합니다(이미 `ADMIN`이면 아무 것도 하지 않아 재시작마다 중복 실행되어도 안전합니다).

---

## 비활성/탈퇴 계정 즉시 차단

JWT는 만료 전까지 유효합니다. 비활성화된 계정의 JWT도 만료 전이면 통과됩니다.
이를 막으려면 **JWT 필터에서 DB를 조회**해야 합니다.

```kotlin
@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, ...) {
        val token = extractToken(request) ?: run { chain.doFilter(request, response); return }
        val email = jwtTokenProvider.getEmail(token)

        val user = userRepository.findByEmail(email)

        // 비활성화 또는 탈퇴 계정 즉시 차단
        if (user == null || user.disabled || user.deletedAt != null) {
            response.sendError(401); return
        }

        // SecurityContext에 인증 정보 설정
        val auth = UsernamePasswordAuthenticationToken(email, null, listOf(SimpleGrantedAuthority("ROLE_${user.role}")))
        SecurityContextHolder.getContext().authentication = auth
        chain.doFilter(request, response)
    }
}
```

---

## Vue Router 관리자 가드

```typescript
router.beforeEach((to) => {
  const authStore = useAuthStore()

  if (to.path.startsWith('/admin')) {
    if (!authStore.isLoggedIn) return '/login'
    if (authStore.user?.role !== 'ADMIN') return '/home'  // 일반 유저 차단
  }

  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return '/login'
  }
})
```

---

## 확인 질문 & 답변

**Q1. Spring Security에서 "ROLE_" 접두사가 자동으로 붙는 이유는?**

> `hasRole('ADMIN')`은 내부적으로 `ROLE_ADMIN` GrantedAuthority를 찾습니다. 반면 `hasAuthority('ROLE_ADMIN')`은 접두사 없이 그대로 찾습니다. 관례적으로 역할은 `ROLE_` 접두사를 사용하고, `hasRole()`이 이를 처리해줍니다. 혼동을 피하려면 항상 `hasRole()`을 사용하거나 항상 `hasAuthority('ROLE_ADMIN')`을 사용하는 방식으로 통일하면 됩니다.

**Q2. 경로 수준(`SecurityConfig`)과 메서드 수준(`@PreAuthorize`)을 둘 다 쓰는 이유는?**

> 이중 방어입니다. `SecurityConfig`는 경로 패턴으로 빠르게 차단하고, `@PreAuthorize`는 비즈니스 로직 가까이에서 명시적으로 권한을 선언합니다. 라우팅 변경으로 경로 보안이 우회되어도 메서드 수준 보안이 남아 있습니다.

**Q3. `AdminInitializer`가 `ensureAdmin()` 안에서 계정 존재 여부를 확인하는 이유는?**

> 서버를 재시작할 때마다 `@EventListener(ApplicationReadyEvent::class)`가 실행됩니다. 체크 없이 매번 `register()`를 호출하면 중복 이메일 제약 위반으로 시작 시 예외가 발생합니다. `ensureAdmin()`은 계정이 없으면 새로 만들고, 있는데 `role`이 `ADMIN`이 아니면 승격만 하며, 이미 `ADMIN`이면 아무 것도 하지 않아 재시작마다 반복 실행되어도 안전합니다(멱등성).

**Q4. 관리자 권한 검사를 프론트엔드(Router 가드)에서만 하면 안 되는 이유는?**

> 프론트엔드 가드는 UI 탐색을 막을 뿐입니다. 공격자가 프론트엔드를 우회하고 직접 `GET /api/admin/users`를 호출하면 서버 측 검증이 없으면 데이터가 노출됩니다. 실제 보안은 항상 서버에서 처리해야 합니다.
