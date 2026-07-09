# Redis Pub/Sub + SSE — 실시간 알림 (4-5단계)

> PLAN.md §4-5 대응 — Redis Pub/Sub과 Server-Sent Events 개념 정리

---

## 이론

### Redis 내부 동작 원리

#### 단일 스레드 이벤트 루프

Redis는 **싱글 스레드** 이벤트 루프로 동작합니다.

```
클라이언트 요청 → 이벤트 큐 → 이벤트 루프(단일 스레드) → 응답
```

이것이 역설적으로 빠른 이유:
- 락(lock)·뮤텍스가 없어 컨텍스트 스위칭 오버헤드 없음
- 모든 데이터 구조가 인메모리 → 디스크 I/O 없음
- 명령 처리 시간이 µs(마이크로초) 단위

단, I/O 집약적이라 CPU 코어 1개를 최대한 활용합니다.
Redis 6.0부터는 네트워크 I/O에 한해 멀티스레드를 도입했지만 명령 처리는 여전히 단일 스레드입니다.

#### Redis 데이터 구조 개요

| 자료구조 | 명령 | QuSign 활용 |
|---|---|---|
| String | GET/SET | SSE 단기 토큰 (`sse-token:{uuid}` → userId, TTL 30초) |
| List | LPUSH/RPOP | — |
| Hash | HGET/HSET | — |
| Set | SADD/SMEMBERS | — |
| Sorted Set | ZADD/ZRANGEBYSCORE | — |
| **Pub/Sub** | **PUBLISH/SUBSCRIBE** | **실시간 알림 전달** |
| Stream | XADD/XREAD | Pub/Sub 대안 (영속) |

---

### Redis Pub/Sub 동작 원리

#### 채널 기반 브로드캐스트

```
발행자(Publisher)
  PUBLISH notification:42 '{"type":"SIGN_DONE"}'
         │
         ▼
Redis 서버 (채널 테이블 조회)
  notification:42 → [구독자 A, 구독자 B]
         │
         ├──▶ 구독자 A (Spring SseEmitter 핸들러)
         └──▶ 구독자 B (다른 인스턴스의 SSE 핸들러)
```

#### Pub/Sub의 전송 보장 특성

| 특성 | 설명 |
|---|---|
| **At-most-once** | 구독자에게 최대 1번 전달 (0번 또는 1번) |
| **Fire-and-forget** | 발행자는 누가 수신했는지 모름 |
| **비영속** | 구독자 없을 때 발행된 메시지는 즉시 소실 |
| **채널 패턴** | `PSUBSCRIBE notification:*`로 와일드카드 구독 가능 |

#### Pub/Sub vs Redis Streams vs Redis Lists 비교

| | Pub/Sub | Streams | Lists |
|---|---|---|---|
| 영속성 | ❌ | ✅ | ✅ |
| 소비 보장 | At-most-once | At-least-once | At-least-once |
| 컨슈머 그룹 | ❌ | ✅ | ❌ |
| 메시지 재생 | ❌ | ✅ | ❌ |
| 구현 복잡도 | 낮음 | 높음 | 중간 |
| **QuSign 선택** | **✅ 실시간 push** | 누락 허용 안 될 때 | — |

**QuSign이 Pub/Sub을 선택한 이유**: 알림 누락 시 DB에서 복구 가능하므로 At-most-once로 충분. Streams는 컨슈머 그룹·오프셋 관리 복잡도가 증가함.

---

### SSE (Server-Sent Events) 프로토콜 상세

#### HTTP 스트리밍 원리

일반 HTTP 응답은 본문을 모두 보낸 후 연결을 닫습니다.
SSE는 **연결을 유지한 채 서버가 계속 데이터를 push**합니다.

```
HTTP/1.1 200 OK
Content-Type: text/event-stream   ← 브라우저가 SSE로 인식
Cache-Control: no-cache
Connection: keep-alive

data: {"type":"SIGN_DONE"}\n\n    ← 이벤트 1 (빈 줄 2개로 종료)

id: 42\n                          ← 이벤트 ID (재연결 시 Last-Event-ID로 전송)
event: notification\n             ← 커스텀 이벤트 타입
data: {"type":"SIGN_REQUEST"}\n\n ← 이벤트 2
```

#### SSE 이벤트 필드

| 필드 | 의미 | 예시 |
|---|---|---|
| `data:` | 페이로드 (필수) | `data: {"msg":"ok"}` |
| `id:` | 이벤트 ID | `id: 42` |
| `event:` | 커스텀 이벤트 타입 | `event: notification` |
| `retry:` | 재연결 대기 시간(ms) | `retry: 3000` |

#### 자동 재연결 메커니즘

브라우저는 SSE 연결이 끊기면 **자동으로 재연결**합니다.

```
브라우저 → GET /api/notifications/stream
서버     ← 연결 유지 (스트리밍)
[네트워크 끊김]
브라우저 → 3초 후 자동 재연결
         → GET /api/notifications/stream
            Last-Event-ID: 42   ← 마지막으로 받은 이벤트 ID 포함
서버     ← ID 42 이후 누락 이벤트 재전송 (구현 시)
```

QuSign은 DB에서 안 읽은 알림을 별도 API로 제공하므로 `Last-Event-ID` 재전송은 미구현.

#### HTTP/2에서의 SSE

HTTP/1.1에서는 도메인당 동시 SSE 연결이 브라우저별로 6개로 제한됩니다.
HTTP/2는 멀티플렉싱으로 이 제한이 없습니다.
QuSign은 Nginx + HTTP/1.1 기준으로 운영하며 탭당 1개 연결이 표준입니다.

---

### 실시간 통신 방식 비교

| 방식 | 동작 | 연결 | 장점 | 단점 |
|---|---|---|---|---|
| **Polling** | 클라이언트가 주기적으로 요청 | HTTP, 단기 | 구현 단순 | 지연, 서버 부하 |
| **Long Polling** | 응답 없으면 연결 유지 후 새 이벤트 시 응답 | HTTP, 단기 반복 | 실시간에 가까움 | 헤더 오버헤드 반복 |
| **SSE** | 서버가 단방향 스트림 push | HTTP, 장기 | 단순, 자동 재연결 | 단방향만 가능 |
| **WebSocket** | 양방향 풀 듀플렉스 | ws://, 장기 | 양방향, 저지연 | 구현 복잡, 프록시 설정 필요 |

알림(Notification)은 **서버 → 클라이언트 단방향**이므로 SSE가 최적입니다.

---

### SseEmitter (Spring) 생명주기

```
연결 요청
   │
   ▼
SseEmitter 생성 (timeout 설정)
   │
   ├── send() → 이벤트 전송
   ├── onCompletion() → 클라이언트 정상 종료 시 콜백
   ├── onTimeout() → 타임아웃 시 콜백
   └── onError() → 오류 발생 시 콜백
   │
   ▼
SseEmitterRegistry에서 emitter 제거
```

```kotlin
// NotificationController.kt — EventSource는 Authorization 헤더를 못 보내므로
// 30초짜리 단기 토큰을 먼저 발급받고, 그 토큰을 쿼리 파라미터로 스트림에 연결한다
@PostMapping("/sse-token")
fun issueSseToken(authentication: Authentication): ApiResponse<SseTokenResponse> {
    val userId = resolveUserId(authentication)
    return ApiResponse.ok(SseTokenResponse(sseTokenService.issue(userId)))
}

@GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
fun stream(@RequestParam token: String?): SseEmitter {
    if (token.isNullOrBlank()) throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "토큰이 필요합니다.")
    val userId = sseTokenService.consume(token)
        ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않거나 만료된 토큰입니다.")
    return registry.register(userId)
}
```

```kotlin
// SseEmitterRegistry.kt — emitter 생성·초기 이벤트 전송·정리 콜백을 registry가 전담
fun register(userId: Long): SseEmitter {
    val emitter = SseEmitter(0L)  // 0 = 타임아웃 없음(무제한 유지, Nginx가 실제 연결 수명을 관리)
    emitters.getOrPut(userId) { CopyOnWriteArrayList() }.add(emitter)

    val cleanup = Runnable { remove(userId, emitter) }
    emitter.onCompletion(cleanup)
    emitter.onTimeout(cleanup)
    emitter.onError { cleanup.run() }

    emitter.send(SseEmitter.event().name("connected").data("ok"))
    return emitter
}
```

**`produces = [MediaType.TEXT_EVENT_STREAM_VALUE]`**:
Spring이 응답 헤더를 `Content-Type: text/event-stream`으로 설정합니다.
이 헤더가 없으면 브라우저가 SSE로 인식하지 못하고 일반 HTTP 응답으로 처리합니다.

**단기 토큰을 쓰는 이유**: 브라우저의 `EventSource` API는 커스텀 헤더(`Authorization: Bearer ...`)를 지원하지 않습니다. JWT를 쿼리 파라미터로 그대로 노출하면 로그에 남는 등 위험하므로, `POST /sse-token`으로 30초짜리 일회성 토큰(Redis `sse-token:{uuid}` → userId)을 먼저 발급받고 `GET /stream?token=...`에서 소비합니다.

---

### Spring Data Redis — Pub/Sub 설정

```kotlin
@Configuration
class RedisConfig {

    // Redis 명령 실행 클라이언트
    // (연결 팩토리 빈은 직접 정의하지 않음 — Spring Boot autoconfigure가
    //  application.yml의 spring.data.redis.* 설정을 읽어 Lettuce 기반으로 자동 생성)
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, String> =
        RedisTemplate<String, String>().apply {
            setConnectionFactory(connectionFactory)
            keySerializer = StringRedisSerializer()
            valueSerializer = StringRedisSerializer()
        }

    // 구독 컨테이너 — 별도 스레드에서 채널 감청
    @Bean
    fun redisMessageListenerContainer(
        connectionFactory: RedisConnectionFactory,
        subscriber: NotificationEventSubscriber,
    ): RedisMessageListenerContainer =
        RedisMessageListenerContainer().apply {
            setConnectionFactory(connectionFactory)
            // "notification:*" 패턴 구독 — 채널명은 "notification:{userId}"
            addMessageListener(subscriber, PatternTopic("notification:*"))
        }
}
```

**Lettuce vs Jedis**:

| | Lettuce | Jedis |
|---|---|---|
| I/O 모델 | 비동기 (Netty) | 동기 (블로킹) |
| 스레드 안전 | 연결 공유 가능 | 연결 풀 필요 |
| Spring Boot 기본 | ✅ | ❌ |
| Reactive 지원 | ✅ | ❌ |

---

## QuSign 알림 전체 흐름

```
[서명자가 서명 완료]
      │
      ▼
SignatureFlowService.sign()
  → AuditLogService.save(SIGNED)        ← 감사 로그
  → createAndPublish(userId, SIGN_DONE) ← 알림 발행
      │
      ▼
NotificationService.createAndPublish()
  1. User.notifySignDone / notifySignRequest 확인 → false면 종료 (알림 타입별로 다른 플래그 참조)
  2. Notification 엔티티 DB 저장 (영속)
  3. redisTemplate.convertAndSend("notification:{userId}", json)
      │
      ▼
RedisMessageListenerContainer (별도 스레드)
  → NotificationEventSubscriber.onMessage(message)
  → SseEmitterRegistry.send(userId, payload)
      │
      ▼
활성 SseEmitter들에게 event push
  → 브라우저 EventSource 수신
  → notificationStore.addNotification(event)
  → AppTopbar 벨 아이콘 배지 +1 업데이트
```

### 다중 서버 인스턴스(Scale-out)에서의 동작

```
[로드 밸런서]
    │                  │
    ▼                  ▼
인스턴스 A          인스턴스 B
(서명 처리)         (SSE 연결 보유)
    │                  ▲
    │   Redis PUBLISH   │
    └──────────────────┘
         Redis가 브로커 역할
```

단일 EC2에서는 이 구조가 불필요하지만, 수평 확장 시 Redis 없이 하면
인스턴스 A에서 발행한 알림이 인스턴스 B의 SSE 연결에 전달되지 않습니다.

### 동시성 안전한 Emitter 관리

```kotlin
@Component
class SseEmitterRegistry {
    // userId → 해당 유저의 활성 SSE 연결 목록 (멀티탭 지원)
    private val emitters = ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>>()

    fun register(userId: Long): SseEmitter {
        val emitter = SseEmitter(0L)
        emitters.getOrPut(userId) { CopyOnWriteArrayList() }.add(emitter)
        // ... onCompletion/onTimeout/onError로 remove(userId, emitter) 연결 (앞 섹션 참고)
        return emitter
    }

    fun send(userId: Long, data: String) {
        emitters[userId]?.removeIf { emitter ->
            try {
                emitter.send(SseEmitter.event().name("notification").data(data))
                false
            } catch (_: Exception) {
                true  // 전송 실패한 emitter는 순회하며 즉시 목록에서 제거
            }
        }
    }

    private fun remove(userId: Long, emitter: SseEmitter) {
        emitters[userId]?.remove(emitter)
    }
}
```
`send()`가 실패한 emitter를 별도의 `remove()` 호출 없이 `removeIf` 안에서 바로 걸러내는 점이 특징입니다 — 순회와 제거를 한 번에 처리합니다.

#### 왜 ConcurrentHashMap + CopyOnWriteArrayList인가

```
시나리오: 스레드 1이 send()로 emitters를 순회하는 동시에
         스레드 2가 onCompletion()으로 emitter를 remove()

일반 HashMap + ArrayList:
  → ConcurrentModificationException 발생

ConcurrentHashMap: putIfAbsent 등 원자적 연산 지원
CopyOnWriteArrayList: 순회 시 스냅샷 복사본 사용 → remove와 동시 순회 안전
```

**주의**: `CopyOnWriteArrayList`는 쓰기(add/remove)마다 전체 배열을 복사합니다.
연결당 1개의 emitter만 있으므로 성능 영향 없습니다. 수백 개 이상이면 `ConcurrentLinkedQueue`를 검토해야 합니다.

---

## 확인 질문 & 답변

**Q1. Redis가 싱글 스레드인데 왜 빠른가?**

> 모든 데이터가 메모리에 있어 디스크 I/O가 없습니다. 싱글 스레드라 락·컨텍스트 스위칭 오버헤드가 없습니다. I/O 멀티플렉싱(epoll/kqueue)으로 수만 개의 동시 연결을 단일 스레드에서 처리합니다. 대부분의 Redis 명령은 O(1) 또는 O(log N)이라 처리 시간이 µs 단위입니다.

**Q2. Redis Pub/Sub이 At-most-once인데, SSE 연결이 끊겼다 재연결되면 그 동안의 알림은 어떻게 되는가?**

> Redis 메시지는 소실됩니다. QuSign은 알림을 DB에도 저장하므로, 재연결 시 `GET /api/notifications`를 호출해 DB에서 읽지 않은 알림을 가져옵니다. Redis는 실시간 push에, DB는 영속 저장소로 역할을 분리합니다. SSE `Last-Event-ID` 헤더를 활용한 서버 측 재전송을 구현하면 더 완벽하지만, 현재 DB 복구 방식으로 충분합니다.

**Q3. `SseEmitter(0L)`처럼 타임아웃을 0으로 설정한 이유는?**

> Spring의 `SseEmitter`에서 `0`은 "타임아웃 없음"을 의미합니다. QuSign은 Spring 쪽 타임아웃을 두지 않고, 연결의 실질적인 수명 관리를 Nginx의 `proxy_read_timeout`, 클라이언트의 네트워크 단절, `onError`/`onCompletion` 콜백에 맡깁니다. 브라우저 `EventSource`는 연결이 끊기면 자동 재연결하므로, 서버 쪽에서 임의로 짧은 타임아웃을 두면 오히려 불필요한 재연결(및 재차 SSE 토큰 발급)이 잦아집니다. SSE endpoint에는 `proxy_buffering off`가 필수입니다 — 버퍼링이 켜져 있으면 Nginx가 이벤트를 즉시 클라이언트로 흘려보내지 않습니다.

**Q4. `ConcurrentHashMap` 대신 일반 `HashMap`을 쓰면 어떤 문제가 생기나?**

> 여러 HTTP 요청이 동시에 들어오면 여러 스레드가 `HashMap.put()`을 동시에 호출합니다. Java 8 이하에서는 해시 체인이 순환 링크드리스트가 되어 `get()` 호출이 무한 루프에 빠집니다. Java 8 이상에서는 트리로 전환되어 무한 루프는 없지만 데이터 손실이 발생할 수 있습니다. `ConcurrentHashMap`은 버킷 단위 세그먼트 락으로 이를 방지합니다.

**Q5. Lettuce가 Jedis보다 Spring Boot에서 더 나은 이유는?**

> Lettuce는 Netty 기반 비동기 I/O를 사용합니다. 하나의 연결을 여러 스레드가 공유할 수 있어 연결 풀이 필요 없습니다. Jedis는 동기 블로킹 방식으로 스레드마다 별도 연결이 필요합니다. Spring Boot WebFlux(Reactor) 기반 애플리케이션에서는 Lettuce가 리액티브 연산을 지원하지만 Jedis는 불가능합니다.

**Q6. 사용자가 알림 설정(notifySignDone)을 OFF하면 무엇이 바뀌나?**

> `NotificationService.isEnabled()`가 알림 타입별로 서로 다른 사용자 플래그를 확인합니다: `SIGN_DONE`/`SIGN_EXPIRED`는 `User.notifySignDone`, `SIGN_REQUEST`/`SIGN_CANCELLED`/`SIGN_EXPIRING_SOON`은 `User.notifySignRequest`를 봅니다. 해당 플래그가 `false`이면 `createAndPublish()`가 DB 저장과 Redis 발행을 모두 건너뜁니다. Redis 구독 설정과 SSE 연결 자체는 변하지 않습니다. 알림이 생성되지 않으므로 배지 숫자도 증가하지 않습니다.
