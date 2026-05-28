# Kotlin Extension Function

## 이론

Extension function(확장 함수)은 기존 클래스의 소스코드를 수정하거나 상속하지 않고 새 함수를 추가하는 기능입니다.

```kotlin
fun 수신객체타입.함수이름(파라미터): 반환타입 {
    // this = 수신 객체
}
```

```kotlin
// String에 함수 추가
fun String.isPalindrome(): Boolean {
    return this == this.reversed()
}

println("racecar".isPalindrome())  // true
println("hello".isPalindrome())    // false
```

### 어떻게 동작하는가

확장 함수는 문법적 설탕(syntactic sugar)입니다. 컴파일 후에는 실제로 수신 객체를 첫 번째 인자로 받는 정적 함수가 됩니다.

```kotlin
// 작성 코드
fun String.shout() = this.uppercase() + "!!!"

// 컴파일 결과 (Java 관점)
static String shout(String receiver) {
    return receiver.toUpperCase() + "!!!";
}
```

따라서 클래스의 private 멤버에는 접근할 수 없습니다.

### 언제 사용하는가

| 상황 | 이유 |
|---|---|
| 라이브러리 클래스에 유틸 함수 추가 | 소스 수정 불가 |
| 특정 컨텍스트에서만 필요한 기능 | 클래스를 오염시키지 않음 |
| 도메인 표현 명확화 | `"file.pdf".addQusignedSuffix()` vs `StringUtils.addSuffix("file.pdf")` |

### 확장 프로퍼티

함수뿐 아니라 프로퍼티도 확장 가능합니다.

```kotlin
val String.wordCount: Int
    get() = this.split(" ").size

println("hello world".wordCount)  // 2
```

---

## 현재 코드에서의 사용 예시

### `String.addQusignedSuffix()` — `SignatureFlowService.kt:272`
```kotlin
private fun String.addQusignedSuffix(): String {
    val dot = lastIndexOf('.')
    return if (dot != -1) substring(0, dot) + "_qusigned" + substring(dot)
    else this + "_qusigned"
}
```

**용도**: 서명 완료된 파일 이름 변환에 사용합니다.
- `"계약서.pdf"` → `"계약서_qusigned.pdf"`
- `"문서"` (확장자 없음) → `"문서_qusigned"`

`String` 클래스에 파일명 관련 메서드를 추가하는 것보다 이 방식이 낫습니다. 이 로직이 필요한 곳(서비스 내부)에만 `private`으로 scoping할 수 있기 때문입니다.

**호출 예시** (서비스 내부):
```kotlin
val signedFilename = originalFilename.addQusignedSuffix()
```

---

## 확인 질문 & 답변

**Q1. 확장 함수 안에서 `this`는 무엇을 의미하는가?**

> 수신 객체(receiver object), 즉 점(`.`) 앞에 오는 값입니다. `fun String.shout()`에서 `this`는 함수가 호출된 String 인스턴스입니다. `"hello".shout()` 호출 시 `this == "hello"`가 됩니다.

---

**Q2. 확장 함수와 멤버 함수가 동일한 시그니처면 어떻게 되는가?**

> 멤버 함수가 항상 이깁니다. 컴파일러는 멤버 함수를 우선 선택합니다.
> ```kotlin
> class Foo {
>     fun bar() = "member"
> }
> fun Foo.bar() = "extension"
>
> Foo().bar()  // "member"
> ```

---

**Q3. `addQusignedSuffix()`를 확장 함수가 아닌 일반 함수로 구현하면?**

> ```kotlin
> private fun addQusignedSuffix(filename: String): String {
>     val dot = filename.lastIndexOf('.')
>     return if (dot != -1) filename.substring(0, dot) + "_qusigned" + filename.substring(dot)
>     else filename + "_qusigned"
> }
> // 호출
> val signedFilename = addQusignedSuffix(originalFilename)
> ```
> 동작은 같지만 `originalFilename.addQusignedSuffix()` 처럼 메서드 체이닝이 안 되어 가독성이 떨어집니다.

---

**Q4. 확장 함수가 수신 객체의 private 멤버에 접근할 수 있는가?**

> 없습니다. 확장 함수는 컴파일 시 정적 함수로 변환되므로 클래스 외부에서 선언한 것과 동일하게 취급됩니다. `private` 멤버에 접근하려면 멤버 함수로 정의해야 합니다.

---

**Q5. 다음 중 유효한 확장 함수를 모두 고르면?**
```kotlin
// A
fun Int.isEven() = this % 2 == 0

// B
fun List<String>.joinWithComma() = this.joinToString(", ")

// C
fun String?.orDefault(default: String) = this ?: default

// D — 이미 String에 있는 함수
fun String.length() = this.count()
```

> A, B, C 모두 유효합니다.
> - A: `Int`에 짝수 판별 추가
> - B: `List<String>`에 편의 함수 추가 (`42.isEven()`, `listOf("a","b").joinWithComma()`)
> - C: nullable 타입(`String?`)에도 확장 함수를 정의할 수 있습니다
> - D: `length`는 이미 `String`의 프로퍼티이므로 함수 `length()`와 충돌 없이 정의되긴 하지만, 멤버가 우선하므로 D의 함수는 호출되지 않습니다.
