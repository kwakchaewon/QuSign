# Kotlin when Expression

## 이론

`when`은 Java의 `switch`를 대체하는 표현식입니다. 다양한 패턴 매칭을 지원하며, **표현식(expression)** 이기 때문에 값을 반환합니다.

### 기본 형태 — 인자 있음

```kotlin
val grade = when (score) {
    in 90..100 -> "A"
    in 80..89  -> "B"
    in 70..79  -> "C"
    else       -> "F"
}
```

### 조건 형태 — 인자 없음

```kotlin
val label = when {
    score >= 90 -> "우수"
    score >= 70 -> "보통"
    else        -> "미달"
}
```

인자 없는 `when`은 각 브랜치가 Boolean 조건이 됩니다. 복잡한 조건 분기에 유용합니다.

### 다양한 매칭 패턴

```kotlin
when (x) {
    1, 2       -> "one or two"     // 여러 값
    in 3..10   -> "three to ten"   // 범위
    is String  -> "it's a string"  // 타입 체크
    else       -> "other"
}
```

### 문(statement) vs 표현식(expression)

```kotlin
// 표현식 — 반환값을 변수에 저장
val result = when (status) {
    "SIGNED"  -> "서명 완료"
    "PENDING" -> "대기 중"
    else      -> "알 수 없음"
}

// 문 — 반환값 사용 안 함
when (status) {
    "SIGNED"  -> println("완료")
    "PENDING" -> println("대기")
}
```

표현식으로 사용할 때는 `else`가 **필수**입니다(sealed class처럼 컴파일러가 모든 케이스를 알면 생략 가능).

### if-else와의 비교

```kotlin
// if-else
val label = if (status == "SIGNED") "완료"
            else if (status == "PENDING") "대기"
            else "만료"

// when — 더 명확한 의도 표현
val label = when (status) {
    "SIGNED"  -> "완료"
    "PENDING" -> "대기"
    else      -> "만료"
}
```

---

## 현재 코드에서의 사용 예시

### 조건 형태로 상태 계산 — `DocumentService.kt:69`
```kotlin
val status = when {
    reqs.isEmpty()                        -> "NONE"
    reqs.all { it.status == "SIGNED" }    -> "SIGNED"
    else                                  -> "PENDING"
}
```
서명 요청 목록을 보고 문서의 전체 상태를 결정합니다. 서로 다른 조건(isEmpty, all)을 다뤄야 해서 인자 없는 형태를 사용합니다.

### 만료 여부 포함 상태 결정 — `SignatureFlowService.kt:218`
```kotlin
val effectiveStatus = when {
    req.status == "SIGNED"       -> "SIGNED"
    req.expiresAt.isBefore(now)  -> "EXPIRED"
    else                         -> "PENDING"
}
```
순서가 중요합니다. SIGNED 체크를 먼저 해야 이미 서명된 요청이 만료 기간이 지났더라도 "EXPIRED"로 잘못 분류되지 않습니다.

### 카운터 증감 — `DashboardService.kt:34`
```kotlin
when (aggregateStatus(requestsByDocId[doc.id].orEmpty(), now)) {
    "SIGNED"  -> signedCount++
    "PENDING" -> pendingCount++
    "EXPIRED" -> expiredCount++
}
```
반환값을 사용하지 않는 문(statement) 형태입니다. 각 상태별 카운터를 증가시킵니다.

---

## 확인 질문 & 답변

**Q1. `when`을 표현식으로 쓸 때 `else`가 필수인 이유는?**

> 표현식은 항상 어떤 값을 반환해야 합니다. 컴파일러가 모든 경우를 커버한다고 증명하지 못하면 `else`가 없을 때 일부 입력에서 반환값이 없어지므로 컴파일 에러가 납니다. sealed class나 enum처럼 가능한 값이 한정된 경우에만 `else` 없이 사용할 수 있습니다.

---

**Q2. `DocumentService.kt`에서 `when`의 조건 순서를 바꾸면 어떤 문제가 생기는가?**
```kotlin
// 원래
when {
    reqs.isEmpty()                     -> "NONE"
    reqs.all { it.status == "SIGNED" } -> "SIGNED"
    else                               -> "PENDING"
}

// 순서를 바꾸면?
when {
    reqs.all { it.status == "SIGNED" } -> "SIGNED"
    reqs.isEmpty()                     -> "NONE"
    else                               -> "PENDING"
}
```

> `reqs.isEmpty()`일 때 `reqs.all { ... }`은 **vacuously true**(공허 참)를 반환합니다. 빈 리스트에 대해 `all` 조건은 항상 true입니다. 순서를 바꾸면 요청이 없는 문서가 `"NONE"` 대신 `"SIGNED"`로 잘못 분류됩니다.

---

**Q3. 아래 코드에서 `when`이 반환하는 타입은?**
```kotlin
val message = when (code) {
    200 -> "OK"
    404 -> "Not Found"
    else -> null
}
```

> `String?`. `"OK"`, `"Not Found"`는 `String`이고 `null`은 null이므로 공통 상위 타입인 `String?`로 추론됩니다.

---

**Q4. `when`으로 타입 체크를 수행하는 코드를 작성해보면?**

> ```kotlin
> fun describe(obj: Any): String = when (obj) {
>     is Int    -> "정수: $obj"
>     is String -> "문자열 (길이 ${obj.length})"
>     is List<*> -> "리스트 (크기 ${obj.size})"
>     else      -> "알 수 없는 타입"
> }
> ```
> `is` 체크 후 브랜치 안에서 `obj`는 해당 타입으로 자동 스마트 캐스트됩니다 (`obj.length` 등 접근 가능).

---

**Q5. `SignatureFlowService.kt:218`에서 `"SIGNED"` 체크를 먼저 하는 이유는?**

> 이미 서명이 완료된 요청은 만료 시간이 지났더라도 `"EXPIRED"`로 분류해서는 안 됩니다. `when`은 위에서 아래 순서로 평가하고 첫 번째 매칭에서 멈춥니다. `"SIGNED"` 브랜치가 먼저 있어야 서명 완료된 요청이 올바르게 처리됩니다.
