# Redis Pub/Sub + SSE — 실시간 알림 (4-5단계)

> PLAN.md §4-5 대응 — Redis Pub/Sub과 Server-Sent Events 개념 정리

---

## 이론

### Redis Pub/Sub

Redis Pub/Sub은 **채널 기반 메시지 브로드캐스트** 패턴입니다.

```
발행자(Publisher)       채널                 구독자(Subscriber)
서명 완료 이벤트  →  user:123:notifications  →  SSE 핸들러
```

핵심 특성:
- **Fire-and-forget**: 메시지는 영속되지 않습니다. 구독자가 없거나 연결이 끊기면 메시지가 소실됩니다.
- **At-most-once**: 발행 시점에 연결된 구독자에게만 1회 전달됩니다.
- 영속성이 필요하면 Redis Streams(`XADD`/`XREAD`)를 사용합니다.

```bash
# Redis CLI로 직접 테스트
SUBSCRIBE user:123:notifications    # 구독 (터미널 1)
PUBLISH user:123:notifications '{"type":"SIGN_DONE"}'  # 발행 (터미널 2)
```

### SSE (Server-Sent Events)

SSE는 **서버 → 클라이언트 단방향 스트리밍** 프로토콜입니다.

```
HTTP/1.1 200 OK
Content-Type: text/event-stream
Cache-Control: no-cache

data: {"type":"SIGN_DONE","message":"서명이 완료됐습니다"}

data: {"type":"SIGN_REQUEST","message":"서명 요청을 받았습니다"}
```

- **자동 재연결**: 브라우저가 연결이 끊기면 자동으로 재연결합니다.
- **HTTP 기반**: 별도 프로토콜 업그레이드 없이 표준 HTTP를 사용합니다.
- **단방향**: 클라이언트 → 서버 메시지가 필요하면 WebSocket을 쓰지만, 알림은 단방향이면 충분합니다.

### WebSocket vs SSE

| | SSE | WebSocket |
|---|---|---|
| 방향 | 서버 → 클라이언트 | 양방향 |
| 프로토콜 | HTTP/1.1 | ws:// |
| 자동 재연결 | 브라우저 기본 지원 | 직접 구현 필요 |
| 프록시 친화성 | 높음 (일반 HTTP) | 낮음 (업그레이드 헤더 필요) |
| 알림 용도 | 충분 | 과도 |

### SseEmitter (Spring)

```kotlin
@RestController
class NotificationController(
    private val sseEmitterRegistry: SseEmitterRegistry
) {

    @GetMapping("/api/notifications/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(@AuthenticationPrincipal email: String): SseEmitter {
        val emitter = SseEmitter(3600_000L)  // 1시간 타임아웃
        sseEmitterRegistry.register(email, emitter)

        emitter.onCompletion { sseEmitterRegistry.remove(email, emitter) }
        emitter.onTimeout { sseEmitterRegistry.remove(email, emitter) }

        // 연결 확인용 초기 이벤트
        emitter.send(SseEmitter.event().name("connect").data("connected"))
        return emitter
    }
}
```

---

## QuSign 알림 전체 흐름

```
[서명자가 서명 완료]
      │
      ▼
SignatureFlowService.sign()
      │  createAndPublish(userId, SIGN_DONE, ...)
      ▼
NotificationService
  1. DB 저장 (notifications 테이블)
  2. Redis PUBLISH user:{userId}:notifications
      │
      ▼
RedisMessageListenerContainer (Redis 구독자)
      │  메시지 수신
      ▼
SseEmitterRegistry.send(userId, event)
      │  현재 연결된 emitter에게 push
      ▼
브라우저 EventSource (자동 수신)
      │
      ▼
notificationStore (Pinia) → 벨 아이콘 배지 업데이트
```

### 다중 서버 인스턴스에서의 동작

```
인스턴스 A (서명 완료 처리)   →  Redis PUBLISH  →  인스턴스 B (SSE 구독자 보유)
                                                      └── EventSource로 push
```

Redis가 브로커 역할을 해서 서명을 처리한 인스턴스와 SSE 연결을 가진 인스턴스가 달라도 알림이 전달됩니다.

### 동시성 안전한 Emitter 관리

```kotlin
@Component
class SseEmitterRegistry {
    // 사용자 1명이 여러 탭에서 접속 가능 → 리스트로 관리
    private val emitters = ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>>()

    fun register(userId: String, emitter: SseEmitter) {
        emitters.getOrPut(userId) { CopyOnWriteArrayList() }.add(emitter)
    }

    fun send(userId: String, event: Any) {
        emitters[userId]?.forEach { emitter ->
            runCatching { emitter.send(event) }
                .onFailure { emitters[userId]?.remove(emitter) }
        }
    }
}
```

`ConcurrentHashMap` — 여러 스레드가 동시에 읽고 쓸 때 안전합니다.
`CopyOnWriteArrayList` — 순회 중에 삭제가 발생해도 `ConcurrentModificationException`이 발생하지 않습니다.

---

## 확인 질문 & 답변

**Q1. Redis Pub/Sub이 At-most-once인데, SSE 연결이 끊겼다 재연결되면 그 동안의 알림은 어떻게 되는가?**

> Redis 메시지는 소실됩니다. QuSign은 알림을 DB에도 저장하므로, 재연결 시 `GET /api/notifications`를 호출해 DB에서 읽지 않은 알림을 가져옵니다. Redis는 실시간 push에 사용하고, DB는 영속 저장소로 역할을 분리합니다.

**Q2. `SseEmitter` 타임아웃을 1시간으로 설정한 이유는?**

> Nginx의 `proxy_read_timeout`을 3600s로 설정했기 때문입니다. 브라우저는 연결 끊김 시 자동 재연결하므로, 실제로는 타임아웃 전에 Nginx가 먼저 끊을 가능성이 높습니다. 너무 짧으면 재연결 트래픽이 많아지고, 너무 길면 서버 스레드/메모리가 낭비됩니다.

**Q3. `ConcurrentHashMap` 대신 일반 `HashMap`을 쓰면 어떤 문제가 생기나?**

> 여러 HTTP 요청이 동시에 들어오면 여러 스레드가 동시에 `HashMap`을 수정합니다. 이 경우 데이터 손상이나 무한 루프가 발생할 수 있습니다 (HashMap의 버킷 체인이 순환될 수 있음). `ConcurrentHashMap`은 세그먼트 단위 락으로 스레드 안전을 보장합니다.

**Q4. 사용자가 알림 설정(notifySignDone)을 OFF하면 무엇이 바뀌나?**

> `NotificationService.createAndPublish()`에서 해당 사용자의 `User.notifySignDone` 필드를 확인하여 `false`이면 DB 저장과 Redis 발행 모두 건너뜁니다. Redis 구독 설정 자체는 변하지 않습니다.
