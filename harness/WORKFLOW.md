# WORKFLOW.md

## 1. 기본 사이클

```
플랜 작성 → 사용자 승인 → 작은 변경 (파일 1~3개) → 검증 → 반복 → 종료 → 회고
```

## 2. 플랜 양식

```
[목표]       무엇을 만들거나 고치는가
[변경 대상]  파일 1~3개 명시
[단계]       번호 매긴 순서
[검증 방법]  ./gradlew test 또는 Postman 시나리오
[리스크]     암호화 관련이면 반드시 명시
[중단 기준]  이 조건이면 멈추고 사용자에게 알린다
[적용 스킬]  .claude/skills/ 중 사용할 스킬 명시 (없으면 "없음")
```

## 3. 변경 단위 가이드

- 파일 수: 1~3개
- 코드 라인: 100줄 이내
- **암호화 레이어** (`PqcSignatureService`, `PdfSignatureService`)는 단독 변경 — 다른 레이어와 동시 수정 금지
- 도메인 경계(auth / document / signature)는 절대 동시에 넘지 않음

## 4. 검증 게이트

매 변경 후 통과 필수:

- [ ] `./gradlew test` — 전체 테스트 (ML-DSA 포함)
- [ ] 타입 체크 (Kotlin 컴파일 성공)
- [ ] 린터 (`./gradlew ktlintCheck`)
- [ ] Docker Compose 기동 확인 (`docker compose up -d && docker compose ps`)

## 5. 단계별 추가 검증 게이트

| 단계 | 추가 게이트 |
|---|---|
| 1단계 | ML-DSA 단위 테스트 통과 |
| 2단계 | Postman 전체 플로우 (회원가입→서명→검증) |
| 3단계 | 실제 이메일 수신 확인 |
| 4단계 | GitHub Actions 파이프라인 Green |
| 5단계 | `terraform plan` diff 0 확인 |

## 6. 병렬 작업 정책

- 독립 도메인(예: auth ↔ storage)일 때만 허용
- 암호화 레이어 작업 중에는 병렬 금지
- 검증 큐가 밀리면 즉시 단일 흐름으로 복귀

## 7. 작업 종료 시

- `docs/exec-plans/PLAN.md` 체크리스트 갱신
- 코드-문서 동기화 확인
- 반복된 패턴 발견 시 → `.claude/skills/`로 추출 검토

## 8. 회고 (매주 금요일 20분)

- 새로 만들면 좋을 스킬은?
- 기존 스킬 중 사용 안 된 것은?
- 어떤 규칙이 무시·우회됐나?
- `harness/CHANGELOG.md` 갱신
