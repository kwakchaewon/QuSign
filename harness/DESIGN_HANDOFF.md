# DESIGN_HANDOFF.md
# Claude 디자인 → Vue 화면 이식 가이드

> claude.ai/design에서 완성한 산출물을 Claude Code에 전달해 Vue SFC로 변환하는 표준 절차.
> 이 문서를 따르면 매 화면마다 동일한 품질과 일관성을 보장할 수 있다.

---

## 1. 전제: Claude 디자인 프로젝트 구조

| 프로젝트 | 역할 | 산출물 |
|---|---|---|
| **QuSign Design System** | 공통 기반 — 한 번만 설정 | `tokens.css`, `tier1-components.html`, `design-principles.md` |
| **QuSign UI (화면별)** | 각 화면의 목업 | `[화면명].html`, `[화면명].css`, `[화면명].jsx` |

Design System 프로젝트는 최초 1회 전달 후, **변경이 생길 때만 재전달**한다.

---

## 2. 화면별 작업 순서

```
① claude.ai/design 에서 화면 설계
        ↓
② 디자인 확정 (레이아웃·컬러·인터랙션 검토 완료)
        ↓
③ 파일 내보내기 (아래 §3 참고)
        ↓
④ Claude Code 에 파일 붙여넣기 + "이식해줘" 요청
        ↓
⑤ Vue SFC 변환 결과 브라우저에서 확인
        ↓
⑥ 피드백 → 재조정 (디자인 or 코드 레벨)
```

---

## 3. 전달 파일 목록

### 필수 (매 화면마다)

| 파일 | 내용 | 내보내기 방법 |
|---|---|---|
| `[화면명].html` | 화면 전체 구조 | 파일 클릭 → 코드 전체 복사 |
| `[화면명].css` | 화면 전용 스타일 | 동일 |

### 선택 (인터랙션이 있을 때)

| 파일 | 내용 |
|---|---|
| `[화면명].jsx` | 상태·이벤트 로직 (React) → Composition API 변환 참고용 |

### Design System 변경 시만

| 파일 | 내용 |
|---|---|
| `tokens.css` | CSS 변수 전체 (컬러·폰트·간격·반경·그림자) |
| `tier1-components.html` | 공통 컴포넌트 HTML 구조 |
| `design-principles.md` | 디자인 원칙 문서 |

---

## 4. 파일 명명 규칙

| claude.ai/design 파일명 | Vue 변환 후 위치 |
|---|---|
| `Login.html` / `login.css` | `frontend/src/views/LoginView.vue` |
| `DocumentList.html` | `frontend/src/views/DocumentListView.vue` |
| `Upload.html` | `frontend/src/views/UploadView.vue` |
| `Sign.html` | `frontend/src/views/SignView.vue` |
| `Verify.html` | `frontend/src/views/VerifyView.vue` |
| `tier1-components.html` | `frontend/src/components/ui/*.vue` (분해) |
| `tokens.css` | `frontend/src/assets/tokens.css` |
| `[화면명].css` | `frontend/src/assets/[화면명].css` (전역 import) |

---

## 5. 화면별 프롬프트

각 화면의 claude.ai/design 프롬프트는 `harness/DESIGN_PROMPTS.md` 참조.

---

## 6. 전달 방법

claude.ai/design에서 파일을 열어 **코드 전체 선택 → 복사** 후 대화창에 붙여넣는다.

```
[사용자 메시지 예시]

로그인 화면 이식해줘.

--- login.html ---
(붙여넣기)

--- login.css ---
(붙여넣기)

--- login.jsx ---  ← 있을 때만
(붙여넣기)
```

**tokens.css는 최초 1회만 전달**. 이후 Design System이 변경됐을 때만 재전달.

---

## 6. Claude Code가 수행하는 작업

1. `.html` → `<template>` 블록으로 변환
2. `.jsx` 로직 → `<script setup lang="ts">` (Composition API) 로 변환
   - `useState` → `ref` / `reactive`
   - `useEffect` → `onMounted` / `watch`
   - `useMemo` → `computed`
   - 콜백 prop → `emit`
3. `.css` → `frontend/src/assets/[화면명].css` 배치 후 `main.ts`에 전역 import
4. 재사용 가능한 요소 → `frontend/src/components/ui/`로 분리
5. `npm run build` 타입 체크 통과 확인

---

## 7. 디렉토리 구조

```
frontend/src/
├── assets/
│   ├── tokens.css          ← Design System CSS 변수 (전역)
│   ├── login.css           ← 로그인 스타일 (전역)
│   └── [화면명].css        ← 화면별 스타일 (전역)
│
├── components/
│   └── ui/                 ← tier1-components.html 분해 결과
│       ├── QuSignMark.vue
│       ├── PqcBadge.vue
│       ├── TrustStrip.vue
│       ├── ThemeToggle.vue
│       └── Toast.vue
│
└── views/                  ← 화면별 .html → .vue 변환 결과
    ├── LoginView.vue
    ├── DocumentListView.vue
    ├── UploadView.vue
    ├── SignView.vue
    └── VerifyView.vue
```

---

## 8. 검증 체크리스트

화면 하나가 이식될 때마다 아래를 확인한다.

- [ ] `npm run build` 타입 오류 0건
- [ ] `npm run dev` 후 브라우저에서 화면 렌더링 확인
- [ ] 라이트/다크 토글 정상 동작
- [ ] 모바일 뷰포트(375px) 레이아웃 깨짐 없음
- [ ] 폼 유효성 검사 동작 확인 (입력 화면일 경우)
- [ ] 버튼 로딩·성공·에러 상태 확인 (인터랙션 있을 경우)

---

## 9. 자주 발생하는 변환 이슈

| 현상 | 원인 | 해결 |
|---|---|---|
| TypeScript 오류: `style` prop | `style`이 HTML 예약 속성 | prop 이름을 `variant`로 변경 |
| 한글 깨짐 (ì´ë©ì¼) | claude.ai/design 내보내기 인코딩 | 대화창에 붙여넣을 때 UTF-8로 재저장하거나 Claude Code가 문맥 추론 |
| 아이콘 누락 | `.jsx`의 inline SVG가 분리 안 됨 | `<script setup>` 안에 정의 불가 → 템플릿 inline SVG로 이동 |
| 반응형 레이아웃 깨짐 | CSS max-width 단위 미스매치 | `tokens.css`의 `--container-*` 변수 사용 확인 |
