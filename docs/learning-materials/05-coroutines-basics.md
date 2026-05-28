# Kotlin Coroutines 기초

## 이론

### 왜 코루틴인가

네트워크 요청, DB 쿼리, 파일 I/O는 결과가 올 때까지 기다려야 합니다.
이 대기 시간 동안 스레드를 점유하면 낭비입니다.

```
스레드 기반 (전통적)
Thread-1: [요청 전송] --------대기-------- [응답 처리]
                       ↑ 스레드가 블로킹됨

코루틴 기반
Thread-1: [요청 전송] [다른 작업] [다른 작업] [응답 처리]
                      ↑ 대기 중 스레드를 다른 작업에 양보
```

코루틴은 **경량 스레드**입니다. 수십만 개를 동시에 실행해도 메모리 부담이 작습니다.

---

### `suspend` 함수

`suspend` 키워드가 붙은 함수는 **일시 중단(suspend)** 될 수 있습니다.
중단되면 현재 스레드를 블로킹하지 않고 다른 코루틴에 양보합니다.

```kotlin
suspend fun fetchUser(id: Long): User {
    return httpClient.get("https://api.example.com/users/$id")
    // 응답 올 때까지 이 코루틴은 중단. 스레드는 해방.
}
```

**규칙**: `suspend` 함수는 코루틴 또는 다른 `suspend` 함수 안에서만 호출할 수 있습니다.

```kotlin
// 일반 함수에서 호출 — 컴파일 에러
fun main() {
    fetchUser(1)  // error: Suspend function should be called only from a coroutine
}

// 코루틴 안에서 호출 — OK
fun main() = runBlocking {
    val user = fetchUser(1)  // OK
    println(user)
}
```

---

### 코루틴 빌더

코루틴을 시작하는 함수들입니다.

| 빌더 | 반환 | 특징 |
|---|---|---|
| `launch { }` | `Job` | 결과 필요 없는 "fire and forget" |
| `async { }` | `Deferred<T>` | 결과가 필요한 비동기 작업, `.await()`로 결과 획득 |
| `runBlocking { }` | 직접 결과 | 현재 스레드를 블로킹 (주로 테스트/main에서) |

```kotlin
// launch — 결과가 필요 없는 작업
val job = launch {
    sendEmail("user@example.com", "서명 요청이 완료되었습니다")
}

// async + await — 결과가 필요한 작업
val deferred = async { fetchUser(userId) }
val user = deferred.await()  // 완료될 때까지 대기

// 두 작업 병렬 실행
val userDeferred = async { fetchUser(userId) }
val docsDeferred = async { fetchDocuments(userId) }
val user = userDeferred.await()
val docs = docsDeferred.await()  // 두 요청이 동시에 실행됨
```

---

### CoroutineScope

코루틴은 **Scope** 안에서 실행됩니다. Scope는 코루틴의 생명주기를 관리합니다.

```kotlin
class MyViewModel : ViewModel() {
    // viewModelScope: ViewModel이 destroy될 때 자동으로 코루틴 취소
    fun loadData() {
        viewModelScope.launch {
            val data = fetchData()
            _uiState.value = data
        }
    }
}
```

Spring WebFlux와 함께 쓸 때는 `CoroutineScope`를 직접 다루거나 suspend controller를 정의합니다.

---

### 구조적 동시성 (Structured Concurrency)

코루틴은 부모-자식 관계를 가집니다.
부모가 취소되면 자식도 모두 취소됩니다.

```kotlin
coroutineScope {
    val a = async { slowOperation1() }
    val b = async { slowOperation2() }
    // a 또는 b 중 하나가 예외를 던지면 나머지도 취소됨
    a.await() + b.await()
}
```

---

## QuSign에서의 현황과 적용 가능성

현재 QuSign 백엔드는 **동기식 Spring Boot (MVC)** 아키텍처로 코루틴을 사용하지 않습니다.
대신 Spring의 `@Transactional`, `@Service` 등을 사용합니다.

코루틴이 필요한 시나리오가 생긴다면:

```kotlin
// 현재 방식 (동기)
fun sendSignatureRequest(request: CreateSignatureRequest): SignatureRequestResponse {
    val document = documentRepository.findById(request.documentId)
        ?: throw DocumentNotFoundException()
    val signatureRequest = signatureRequestRepository.save(...)
    emailService.sendEmail(...)  // 이메일 전송이 완료될 때까지 블로킹
    return SignatureRequestResponse(signatureRequest)
}

// 코루틴 방식 (이메일을 병렬로 처리)
suspend fun sendSignatureRequest(request: CreateSignatureRequest): SignatureRequestResponse {
    val document = documentRepository.findById(request.documentId)
        ?: throw DocumentNotFoundException()
    val signatureRequest = signatureRequestRepository.save(...)
    launch { emailService.sendEmail(...) }  // 이메일을 비동기로 처리
    return SignatureRequestResponse(signatureRequest)  // 이메일 완료 기다리지 않음
}
```

---

## 확인 질문 & 답변

**Q1. `suspend` 함수는 어디서 호출할 수 있는가?**

> 다른 `suspend` 함수 안, 또는 코루틴 빌더(`launch`, `async`, `runBlocking`) 안에서만 호출할 수 있습니다. 일반 함수에서 직접 호출하면 컴파일 에러가 납니다.

---

**Q2. `launch`와 `async`의 차이는?**

> `launch`는 `Job`을 반환하고 결과값이 없습니다. "실행하고 잊기" 용도입니다.
> `async`는 `Deferred<T>`를 반환하고, `.await()`를 호출하면 결과를 얻을 수 있습니다.
> 결과가 필요하면 `async`, 필요 없으면 `launch`를 씁니다.

---

**Q3. 아래 코드에서 두 요청은 순차적인가, 병렬인가?**
```kotlin
val users = async { fetchUsers() }
val docs  = async { fetchDocuments() }
val result = users.await() + docs.await()
```

> **병렬**입니다. 두 `async`가 모두 시작된 후 `await`으로 결과를 기다립니다. `fetchUsers()`가 완료될 때까지 `fetchDocuments()`가 시작을 기다리지 않습니다.
> 만약 `val users = fetchUsers()` + `val docs = fetchDocuments()`를 순서대로 썼다면 순차 실행입니다.

---

**Q4. 코루틴이 스레드보다 "가볍다"는 말의 의미는?**

> 스레드는 OS 수준의 리소스로 생성·전환 비용이 크고 메모리를 많이 씁니다(JVM 기본 ~1MB/스레드).
> 코루틴은 JVM 힙에 저장되는 객체이므로 생성 비용이 훨씬 적습니다. 수십만 개의 코루틴을 동시에 유지해도 스레드 수는 CPU 코어 수 정도만 있으면 됩니다.

---

**Q5. `runBlocking`을 실제 서비스 코드에서 사용하면 안 되는 이유는?**

> `runBlocking`은 현재 스레드를 블로킹합니다. 서버에서 사용하면 스레드가 코루틴 완료를 기다리는 동안 다른 요청을 처리하지 못해 처리량이 떨어집니다. `runBlocking`은 테스트 코드나 `main()` 함수처럼 최상위 진입점에서만 사용합니다.
