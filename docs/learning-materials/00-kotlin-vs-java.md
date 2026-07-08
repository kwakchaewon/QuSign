# Kotlin vs Java — 왜 Kotlin을 쓰는가

QuSign 백엔드는 Kotlin + Spring Boot로 작성되어 있습니다. 01~05번 자료(Null Safety, Data Class,
Extension Function, when, Coroutines)에서 다루는 문법은 전부 "Java였다면 이 문제를 어떻게 풀었을까"라는
질문에 대한 Kotlin의 답입니다. 이 문서는 그 답들을 한데 모아 Kotlin이 Java 대비 갖는 강점을 정리합니다.

---

## 1. 전제: Kotlin은 Java를 대체하는 게 아니라 JVM 위에서 상호운용됩니다

Kotlin은 Java와 같은 JVM 바이트코드로 컴파일되고, Java 클래스를 그대로 호출할 수 있습니다
(반대도 가능). 따라서 "Kotlin의 강점"은 새로운 런타임이 주는 이점이 아니라, **같은 플랫폼 위에서
더 적은 코드로 더 많은 실수를 컴파일 타임에 잡아내는 언어 설계**에서 나옵니다.

---

## 2. 핵심 차이 요약

| 항목 | Java | Kotlin | 효과 |
|---|---|---|---|
| null 처리 | 모든 참조 타입이 기본적으로 null 가능 → NPE는 런타임에만 발견 | 타입에 nullable 여부(`String` vs `String?`)가 인코딩됨 → 컴파일 타임에 검출 | [[01-null-safety]] |
| 불변/가변 구분 | `final` 키워드를 명시해야 불변 | `val`/`var`로 기본값이 불변(`val`) | 실수로 재할당하는 버그 원천 차단 |
| 데이터 보관 클래스 | 생성자·getter·`equals`·`hashCode`·`toString`을 직접 작성 (Java 16+ `record`로 일부 해소) | `data class` 한 줄로 전부 자동 생성 + `copy()`, 구조 분해까지 지원 | [[02-data-class]] |
| 기존 클래스 확장 | 상속하거나 `XxxUtils.method(obj)` 형태의 정적 유틸 클래스 필요 | Extension function으로 기존 타입에 메서드처럼 추가 (소스 수정/상속 불필요) | [[03-extension-function]] |
| 분기 처리 | `switch`는 문(statement), 값 반환 불가(구식), fall-through 버그 위험 | `when`은 표현식(expression), 값 반환 가능, 타입/범위/다중값 매칭 지원 | [[04-when-expression]] |
| 비동기 처리 | 스레드 직접 관리 또는 `CompletableFuture` 콜백 체인 (콜백 지옥) | `suspend` 함수 + 코루틴으로 동기 코드처럼 작성, 경량 스레드로 수십만 개 동시 실행 | [[05-coroutines-basics]] |
| 타입 추론 | 로컬 변수는 Java 10+ `var`로 일부 지원, 필드/반환 타입은 항상 명시 | 대부분의 위치에서 타입 추론, 필요할 때만 명시 | 코드량 감소 |
| 스마트 캐스트 | `instanceof` 체크 후에도 명시적 캐스팅 필요 | 타입 체크(`is`) 후 자동으로 해당 타입처럼 사용 가능 | 보일러플레이트 감소 |
| 체크 예외 | `throws` 선언 강제, 호출부에서 반드시 처리 | 체크 예외 없음 — 예외 처리 여부를 컴파일러가 강제하지 않음 | 유연성 ↑ (대신 문서화 책임은 개발자에게) |

---

## 3. 대표적인 차이 — 코드로 비교

### 3-1. Null 안전성

```java
// Java — NPE는 런타임에만 드러남
User user = userRepository.findByEmail(email);
String city = user.getAddress().getCity();  // user나 address가 null이면 크래시
```

```kotlin
// Kotlin — 컴파일러가 null 가능성을 추적
val user = userRepository.findByEmail(email) ?: throw InvalidCredentialsException()
val city = user.address?.city  // address가 null이면 city는 자동으로 null
```

QuSign의 `AuthService.kt:46`, `SignatureFlowService.kt:467` 등이 이 패턴입니다. Java에서는
"이 메서드가 null을 반환할 수 있는지"를 문서나 `@Nullable` 어노테이션(강제력 없음)에 의존해야 하지만,
Kotlin은 타입 시스템이 강제합니다.

### 3-2. 데이터 클래스

```java
// Java 16+ record — 그나마 나아졌지만 여전히 val만 가능, copy() 없음
public record SignatureRequestResponse(Long id, String documentId, String signerEmail) {}
```

```kotlin
data class SignatureRequestResponse(
    val id: Long,
    val documentId: String,
    val signerEmail: String,
)
// equals/hashCode/toString/copy()/구조분해 전부 자동 생성
```

Java의 `record`는 Kotlin `data class`의 부분집합에 가깝습니다. `copy()`로 일부 필드만 바꾼
불변 객체를 만드는 기능이 없어서, QuSign처럼 상태 전이(PENDING → SIGNED)를 불변 객체로
표현하려면 Kotlin 쪽이 더 간결합니다.

### 3-3. 기존 타입 확장

```java
// Java — String을 직접 못 고치니 정적 유틸 클래스로 우회
public class FilenameUtils {
    public static String addQusignedSuffix(String filename) { ... }
}
FilenameUtils.addQusignedSuffix(originalFilename);
```

```kotlin
private fun String.addQusignedSuffix(): String { ... }
originalFilename.addQusignedSuffix()
```

`SignatureFlowService.kt:876`처럼, Kotlin은 메서드 체이닝 형태를 유지하면서도 스코프(`private`)를
좁게 제한할 수 있습니다. Java는 정적 유틸을 쓰거나 상속으로 우회해야 합니다.

### 3-4. 분기 표현식

```java
// Java switch — 문(statement), 각 case에서 값을 대입해야 함
String status;
switch (code) {
    case "SIGNED": status = "서명 완료"; break;
    case "PENDING": status = "대기 중"; break;
    default: status = "알 수 없음";
}
```

```kotlin
val status = when (code) {
    "SIGNED"  -> "서명 완료"
    "PENDING" -> "대기 중"
    else      -> "알 수 없음"
}
```

Java의 `switch`는 `break`를 빼먹으면 다음 case로 흘러내리는(fall-through) 고전적 버그 원인입니다.
Kotlin의 `when`은 표현식이라 `val`에 바로 대입되고, fall-through가 없습니다. (Java 14+의
`switch` 표현식이 이 간극을 상당 부분 좁혔지만, QuSign은 Java 14 이전 스타일 문서 대비 기준입니다.)

### 3-5. 비동기 처리

```java
// Java — CompletableFuture 콜백 체인
CompletableFuture.supplyAsync(() -> fetchUser(id))
    .thenCompose(user -> fetchDocumentsAsync(user))
    .thenAccept(docs -> render(docs))
    .exceptionally(ex -> { log.error(...); return null; });
```

```kotlin
// Kotlin — 동기 코드처럼 읽히는 비동기 코드
suspend fun loadUserDocuments(id: Long): List<Document> {
    val user = fetchUser(id)
    return fetchDocuments(user)
}
```

코루틴은 콜백 체인 없이 순차적으로 읽히면서도 non-blocking입니다. (QuSign 백엔드는 현재
동기식 Spring MVC를 쓰므로 이 부분은 실제 적용 사례가 아니라 [[05-coroutines-basics]]에서
다루는 "필요해지면 쓸 수 있는 도구"입니다.)

---

## 4. 그럼에도 Kotlin이 아니라 Java를 고려할 상황

균형 잡힌 이해를 위해 Kotlin의 트레이드오프도 짚습니다.

- **컴파일 속도**: 대규모 프로젝트에서 Kotlin 컴파일러가 Java `javac`보다 느린 경우가 있습니다.
- **팀 러닝 커브**: Java 팀에 새 문법(coroutine, 확장 함수, 스마트 캐스트)을 온보딩하는 비용.
- **체크 예외 부재**: Java의 `throws` 강제가 없어, 예외 처리 누락을 컴파일러가 잡아주지 않습니다.
  (QuSign은 커스텀 예외 + 전역 `@ExceptionHandler`로 이를 보완합니다.)
- **생태계 문서/예제**: 특정 라이브러리는 Java 예제만 존재하는 경우가 여전히 많습니다 (Kotlin에서도
  거의 그대로 호출 가능하지만 번역이 필요).

---

## 확인 질문 & 답변

**Q1. Java의 `record`와 Kotlin의 `data class`의 가장 실질적인 차이는?**

> `record`는 모든 필드가 `final`(불변)이고 `copy()`가 없습니다. 필드 일부만 바꾼 새 객체를 만들려면
> 생성자를 직접 다시 호출해야 합니다. `data class`는 `copy(field = newValue)`로 나머지 필드를
> 그대로 유지한 채 일부만 바꾼 새 인스턴스를 만들 수 있어, 불변 상태 전이 패턴에 더 적합합니다.

**Q2. Kotlin의 null safety가 "런타임 검증"이 아니라 "컴파일 타임 검증"이라는 말의 의미는?**

> Java에서 `@Nullable`/`@NonNull` 어노테이션은 IDE 경고 수준의 힌트일 뿐 컴파일러가 강제하지
> 않습니다. Kotlin은 `String`과 `String?`을 서로 다른 타입으로 취급하므로, null 체크 없이
> `String?`을 `String`이 필요한 위치에 넘기면 **컴파일 자체가 안 됩니다**. 버그가 배포 전에 걸러집니다.

**Q3. Extension function이 상속보다 나은 경우는 언제인가?**

> 상속은 새 서브클래스를 만들고 기존 코드가 그 서브클래스를 쓰도록 바꿔야 하지만, extension
> function은 기존 타입(`String` 등)을 그대로 쓰면서 특정 파일/모듈 스코프에서만 메서드를
> 추가할 수 있습니다. `String`처럼 `final`이라 상속 자체가 불가능한 클래스에는 extension이
> 유일한 방법입니다.

**Q4. `when`이 `switch`보다 안전하다고 말할 수 있는 근거는?**

> `when`은 fall-through가 없고(각 브랜치가 독립적으로 평가), 표현식이므로 값을 반드시 반환해야
> 하는 위치(예: `val`에 대입)에서는 컴파일러가 모든 경우가 커버됐는지 강제합니다. `switch`
> 문(statement)은 `break` 누락이나 값 대입 누락이 컴파일 에러 없이 통과될 수 있습니다.
