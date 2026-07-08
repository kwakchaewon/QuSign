# Kotlin Null Safety

## 사전 개념: val과 var

Kotlin에서 변수를 선언할 때 쓰는 두 가지 키워드입니다.

| 키워드 | 의미 | 재할당 |
|---|---|---|
| `val` | value — 불변 참조 | 불가 |
| `var` | variable — 가변 참조 | 가능 |

```kotlin
val name = "Alice"   // 재할당 불가
name = "Bob"         // 컴파일 에러

var count = 0        // 재할당 가능
count = 1            // OK
```

타입은 명시하거나 컴파일러가 추론합니다.

```kotlin
val name: String = "Alice"   // 타입 명시
val name = "Alice"           // String으로 자동 추론 (동일)
```

> **val은 참조가 고정되는 것**이지, 객체 내부까지 불변을 보장하지 않습니다.
> ```kotlin
> val list = mutableListOf("a", "b")
> list = mutableListOf("c")   // 에러 — 참조 재할당 불가
> list.add("c")               // OK — 내부 값 변경은 가능
> ```

Kotlin에서는 변경이 필요할 때만 `var`을 쓰고, 기본적으로 `val`을 권장합니다.

---

## 이론

Kotlin은 컴파일 타임에 NullPointerException을 방지하도록 설계됐습니다.
타입 시스템이 "null이 될 수 있는 값"과 "절대 null이 아닌 값"을 구분합니다.

### 핵심 연산자

| 연산자 | 이름 | 설명 |
|---|---|---|
| `String?` | Nullable 타입 | null을 허용하는 타입 |
| `?.` | Safe call | null이면 null 반환, null이 아니면 실행 |
| `?:` | Elvis | 좌변이 null이면 우변 반환 |
| `!!` | Not-null assertion | null이면 NPE 발생 (사용 자제) |

```kotlin
// 타입 선언
val name: String  = "Alice"   // null 불가
val name: String? = null      // null 가능

// safe call — user가 null이면 null 반환, 아니면 .email 접근
val email = user?.email

// elvis — null이면 기본값 사용
val email = user?.email ?: "unknown@example.com"

// 연쇄 safe call
val city = order?.address?.city   // 중간에 null이면 전체 null

// not-null assertion — null이면 NullPointerException 발생
val city = address!!.city         // address가 null이면 크래시
```

### `!!` 를 피해야 하는 이유

```kotlin
// 나쁜 예 — 런타임 크래시 위험
val length = str!!.length

// 좋은 예 — 안전하게 처리
val length = str?.length ?: 0
```

---

## 현재 코드에서의 사용 예시

### Elvis로 예외 던지기 — `AuthService.kt:46`
```kotlin
val user = userRepository.findByEmail(email)
    ?: throw InvalidCredentialsException()
```
`findByEmail`은 `User?`를 반환합니다. 결과가 null이면 즉시 예외를 던져 이후 코드가 null 역참조로 깨지는 것을 방지합니다.

### Elvis로 조기 반환 — `DocumentService.kt:72`
```kotlin
val user = userRepository.findByEmail(email)
    ?: return emptyList()
```
유저를 찾지 못했을 때 빈 리스트를 바로 반환합니다. if-null 분기 없이 한 줄로 처리합니다.

### 연쇄 safe call — `SignatureFlowService.kt:467`
```kotlin
signedAt = sig?.signedAt?.toString()
```
`sig`가 null이거나 `signedAt`이 null이면 전체 식이 null이 됩니다.
어느 단계에서도 NPE가 발생하지 않습니다.

### Nullable 필드 선언 — `User.kt:40`
```kotlin
var deletedAt: LocalDateTime? = null
```
소프트 삭제(soft delete)용 필드입니다. 삭제 전까지는 null이므로 nullable 타입이 맞습니다.

---

## 확인 질문 & 답변

**Q1. `user?.address?.city` 에서 `address`가 null이면 결과는?**

> `null`. safe call 체인은 중간에 null이 나오는 순간 나머지를 평가하지 않고 null을 반환합니다.

---

**Q2. 아래 두 코드의 차이는?**
```kotlin
// A
val len = str!!.length

// B
val len = str?.length ?: 0
```

> A는 `str`이 null이면 `NullPointerException`이 발생합니다. B는 null이면 0을 반환하므로 안전합니다. `!!`는 "이 값이 절대 null이 아님을 내가 보장한다"는 선언이므로 확신이 없으면 사용하면 안 됩니다.

---

**Q3. 다음 코드를 Elvis를 사용해 한 줄로 리팩토링하면?**
```kotlin
val result: String
if (input != null) {
    result = input
} else {
    result = "기본값"
}
```

> ```kotlin
> val result = input ?: "기본값"
> ```

---

**Q4. `AuthService.kt`에서 `?: throw` 패턴을 쓰는 이유는?**

> 인증 실패는 예외 상황이므로 null을 흘려보내지 않고 즉시 오류를 알려야 합니다. Elvis 우변에 `throw`를 쓰면 null 체크와 예외 발생을 한 줄로 표현할 수 있습니다.

---

**Q5. `String?`과 `String`의 차이를 타입 시스템 관점에서 설명하면?**

> Kotlin은 null 허용 여부를 타입 자체에 인코딩합니다. `String`은 절대 null이 아님이 보장된 타입이고, `String?`은 `String | null` 유니온에 해당합니다. 컴파일러가 null 가능성을 추적하므로 `String?` 변수를 null 체크 없이 `String` 위치에 사용하면 컴파일 에러가 납니다.
