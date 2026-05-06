# CLAUDE.md

## 0. 작업 시작 전 체크리스트

1. 이 파일을 읽는다
2. `ARCHITECTURE.md`로 시스템 지도 파악
3. §1 절대 금지 확인
4. §4 라우팅에서 작업 유형별 세부 문서 읽기
5. `.claude/skills/`에서 적용 가능한 스킬 확인
6. 플랜 먼저 작성 (`harness/WORKFLOW.md` 양식 참고)
7. 사용자 승인 후 구현

---

## 1. 절대 금지 사항 (NEVER)

- ❌ 개인키 원문 로그·응답·외부 전송
- ❌ `.env`, API 키, DB 패스워드 출력·커밋
- ❌ 영구 삭제 (`rm -rf`, `DROP TABLE`, force push to main)
- ❌ ML-DSA 검증 로직 스킵·하드코딩
- ❌ 결제·금융 거래 (5단계 전)
- ❌ 사용자 명시 승인 없는 배포

→ 상세: `harness/SECURITY.md`

---

## 2. 자유와 제약의 분리

| 영역 | 정책 |
|---|---|
| 구현 방식 | 자유 |
| §1 금지 사항 | 물리적 잠금 — 절대 위반 불가 |
| 암호화 레이어 변경 | 플랜 → 승인 → 테스트 통과 필수 |
| S/A 등급 영역 | 자율 변경 금지, 항상 승인 후 구현 |

→ 등급 정의: `harness/QUALITY_SCORE.md`

---

## 3. 검증

- **즉시**: `./gradlew test` + 타입 체크 + `ktlintCheck`
- **Docker**: `docker compose up -d && docker compose ps`
- **주기**: 코드-문서 동기화 / 아키텍처 위반 검사
- **회고**: 매주 금요일 20분 → `harness/CHANGELOG.md` 갱신

→ 단계별 게이트: `harness/WORKFLOW.md §5`

---

## 4. 규칙 라우팅

| 작업 유형 | 참조 문서 |
|---|---|
| 시스템 지도 | `ARCHITECTURE.md` |
| 보안 / 금지 | `harness/SECURITY.md` |
| 작업 흐름 / 플랜 양식 | `harness/WORKFLOW.md` |
| 제품 비전 / 우선순위 | `harness/PRODUCT_SENSE.md` |
| 영역별 품질 등급 | `harness/QUALITY_SCORE.md` |
| **Claude 디자인 → Vue 이식** | `harness/DESIGN_HANDOFF.md` |
| **화면별 디자인 프롬프트** | `harness/DESIGN_PROMPTS.md` |
| 전체 개발 체크리스트 | `docs/exec-plans/PLAN.md` |
| 사용 가능한 스킬 | `.claude/skills/` |
| 하네스 변경 이력 | `harness/CHANGELOG.md` |

---

## 5. 기술 스택 빠른 참조

| 레이어 | 기술 |
|---|---|
| 백엔드 | Kotlin + Spring Boot 3.2, Gradle Kotlin DSL |
| 암호화 | liboqs-java (ML-DSA), PDFBox |
| DB | MariaDB 10.11 |
| 스토리지 | MinIO (로컬) → AWS S3 (4단계) |
| 이메일 | AWS SES (3단계) |
| 프론트엔드 | Vue 3 + Vite + Pinia + Vue Router (3단계) |
| 인프라 | Docker Compose → AWS EC2/RDS (4단계) → Terraform (5단계) |
| 모니터링 | Loki + Grafana (6단계) |

---

## 6. 메타 원칙

> 최소한의 규칙으로 최대 효율. 규칙 추가 전 묻는다: "이게 정말 사고를 막는가?"
> 스킬 추가 전 묻는다: "이 작업을 3번 이상 반복했는가?"

---

## 7. 변경 이력

→ `harness/CHANGELOG.md`
