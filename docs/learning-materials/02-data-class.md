# Kotlin Data Class

## 사전 개념: 보일러플레이트(Boilerplate)

반복적으로 써야 하지만 실제 로직과는 무관한 **판에 박힌 코드**를 말합니다.

Java에서 단순히 데이터를 담는 클래스를 만들려면:

```java
// Java — 보일러플레이트 덩어리
public class User {
    private String name;
    private int age;

    public User(String name, int age) { this.name = name; this.age = age; }
    public String getName() { return name; }
    public int getAge() { return age; }

    @Override public boolean equals(Object o) { ... }
    @Override public int hashCode() { ... }
    @Override public String toString() { ... }
}
```

이 코드의 대부분은 "User는 name과 age를 가진다"는 사실과 직접 관련이 없습니다.
매번 똑같은 패턴을 반복해서 작성해야 하는 부분이 보일러플레이트입니다.

> 이름의 유래: 과거 신문사가 변경 없이 그대로 찍어내던 철제 인쇄판(boilerplate)에서 왔습니다.

---

## 이론

`data class`는 데이터를 담는 목적의 클래스에 보일러플레이트를 자동 생성해줍니다.
선언만 하면 컴파일러가 다음을 자동으로 만들어줍니다.

| 자동 생성 항목 | 설명 |
|---|---|
| `equals()` | 프로퍼티 값 기준 동등 비교 |
| `hashCode()` | equals와 일관된 해시 |
| `toString()` | `ClassName(prop=val, ...)` 형식 출력 |
| `copy()` | 일부 프로퍼티만 바꿔 새 객체 생성 |
| `componentN()` | 구조 분해 선언 지원 |

```kotlin
data class User(val id: Long, val email: String, val name: String)

val alice = User(1, "alice@example.com", "Alice")

// toString — 자동 생성
println(alice)  // User(id=1, email=alice@example.com, name=Alice)

// equals — 값 비교
val alice2 = User(1, "alice@example.com", "Alice")
println(alice == alice2)  // true (일반 class면 false)

// copy — 일부만 변경
val renamed = alice.copy(name = "Alicia")

// 구조 분해
val (id, email) = alice
```

### 일반 class와의 차이

```kotlin
// 일반 class — 참조 비교 (같은 객체만 equal)
class Point(val x: Int, val y: Int)
Point(1, 2) == Point(1, 2)  // false

// data class — 값 비교
data class Point(val x: Int, val y: Int)
Point(1, 2) == Point(1, 2)  // true
```

### 제약사항

- 기본 생성자에 파라미터가 최소 하나 있어야 합니다.
- 기본 생성자 파라미터는 `val` 또는 `var`이어야 합니다.
- `abstract`, `open`, `sealed`, `inner`와 함께 쓸 수 없습니다.

---

## 현재 코드에서의 사용 예시

### 요청 DTO — `AuthDtos.kt:10`
```kotlin
data class RegisterRequest(
    @field:Email(message = "유효한 이메일 형식이어야 합니다")
    val email: String,
    val password: String,
)
```
HTTP 요청 바디를 역직렬화할 때 사용합니다. `equals`/`hashCode`가 자동 생성되므로 테스트에서 값 비교가 쉬워집니다.

### 응답 DTO (보조 생성자 포함) — `SignatureDtos.kt:73`
```kotlin
data class SignatureRequestResponse(
    val id: Long,
    val documentId: Long,
    val documentFilename: String,
    val signerEmail: String,
    val token: String,
    val status: String,
    val expiresAt: String,
    val createdAt: String?,
) {
    constructor(req: SignatureRequest) : this(
        id = req.id,
        documentId = req.document.id,
        documentFilename = req.document.originalFilename,
        signerEmail = req.signerEmail,
        token = req.token,
        status = req.status,
        expiresAt = req.expiresAt.toString(),
        createdAt = req.createdAt?.toString(),
    )
}
```
(실제 파일에는 Swagger `@Schema` 어노테이션이 각 필드에 붙어 있으나, 여기서는 데이터 클래스 구조에 집중하기 위해 생략했습니다.)

엔티티(`SignatureRequest`)를 받아 DTO로 변환하는 보조 생성자 패턴입니다. 서비스 레이어에서 `SignatureRequestResponse(entity)` 한 줄로 변환합니다.

### 중첩 DTO — `DashboardDtos.kt:20`
```kotlin
data class RecentRequestItem(
    val id: Long,
    val documentName: String,
    val signers: List<String>,
)
```
대시보드 응답 안에 포함되는 내부 데이터 구조입니다. 여러 depth로 data class를 중첩해 복잡한 JSON 응답을 표현합니다.

---

## 확인 질문 & 답변

**Q1. `data class`에서 `copy()`의 용도는?**

> 기존 객체의 값을 대부분 유지하면서 일부 프로퍼티만 바꾼 새 객체를 만듭니다. 예를 들어 `user.copy(name = "새이름")` 은 나머지 필드는 그대로 두고 name만 바꾼 새 User를 반환합니다. 불변 객체를 수정할 때 유용합니다.

---

**Q2. 아래 코드의 출력값은?**
```kotlin
data class Point(val x: Int, val y: Int)

val a = Point(3, 4)
val b = a.copy(y = 10)
println(a)
println(b)
println(a == b)
```

> ```
> Point(x=3, y=4)
> Point(x=3, y=10)
> false
> ```

---

**Q3. `SignatureRequestResponse`에서 보조 생성자를 쓰는 이유는?**

> 엔티티를 DTO로 변환하는 로직을 DTO 클래스 안에 캡슐화하기 위해서입니다. 서비스나 컨트롤러에서 변환 코드가 흩어지지 않고, `SignatureRequestResponse(entity)` 한 줄로 사용할 수 있습니다.

---

**Q4. `data class`에서 자동 생성되지 않는 것은?**

> `body`에 직접 선언한 프로퍼티(기본 생성자 바깥)는 `equals`/`hashCode`/`toString`/`copy` 계산에서 제외됩니다. 또한 `clone()`은 자동 생성되지 않습니다.

---

**Q5. 구조 분해 선언(destructuring)을 사용한 코드를 작성해보면?**

> ```kotlin
> data class RecentRequestItem(
>     val id: Long,
>     val documentName: String,
>     val signers: List<String>,
> )
>
> val item = RecentRequestItem(1L, "계약서.pdf", listOf("a@b.com"))
> val (id, name, signers) = item
> println("$id: $name (${signers.size}명)")
> // 출력: 1: 계약서.pdf (1명)
> ```
