# Kotlin 학습 자료

QuSign 백엔드(Kotlin + Spring Boot)를 위한 개념별 학습 자료입니다.
각 문서는 **이론 → 현재 코드 적용 예시 → 확인 질문 & 답변** 구성으로 작성됐습니다.

## 목차

| # | 주제 | 파일 | PLAN.md 항목 |
|---|---|---|---|
| 1 | Null Safety (`?.` `!!` `?:`) | [01-null-safety.md](01-null-safety.md) | ✅ |
| 2 | Data Class | [02-data-class.md](02-data-class.md) | ✅ |
| 3 | Extension Function | [03-extension-function.md](03-extension-function.md) | ✅ |
| 4 | when Expression | [04-when-expression.md](04-when-expression.md) | ✅ |
| 5 | Coroutines 기초 (`suspend`, `launch`) | [05-coroutines-basics.md](05-coroutines-basics.md) | ✅ |

## 학습 방법

1. 각 파일의 **이론** 섹션을 읽습니다.
2. **현재 코드 예시** 섹션을 VSCode에서 실제 파일과 함께 확인합니다.
3. **확인 질문**을 먼저 스스로 답해본 뒤 답변과 비교합니다.
4. PLAN.md에서 해당 항목을 체크 표시합니다.

## 코드 참조 경로

| 파일 | 주요 패턴 |
|---|---|
| `backend/src/main/kotlin/com/qusign/auth/service/AuthService.kt` | null safety (elvis + throw) |
| `backend/src/main/kotlin/com/qusign/auth/entity/User.kt` | nullable 필드 |
| `backend/src/main/kotlin/com/qusign/auth/dto/AuthDtos.kt` | data class |
| `backend/src/main/kotlin/com/qusign/signature/dto/SignatureDtos.kt` | data class + 보조 생성자 |
| `backend/src/main/kotlin/com/qusign/document/dto/DashboardDtos.kt` | 중첩 data class |
| `backend/src/main/kotlin/com/qusign/signature/service/SignatureFlowService.kt` | extension function, when, null safety |
| `backend/src/main/kotlin/com/qusign/document/service/DocumentService.kt` | when (인자 없음), elvis |
| `backend/src/main/kotlin/com/qusign/document/service/DashboardService.kt` | when (문 형태) |
