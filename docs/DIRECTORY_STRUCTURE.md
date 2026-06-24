# 프로젝트 디렉토리 구조

> QuSign 프로젝트 루트 기준 1~2차 폴더 구조 및 역할 정리

---

## 루트 구조

```
QuSign/
├── backend/                  # Kotlin + Spring Boot 3 백엔드
├── frontend/                 # Vue 3 + Vite 프론트엔드
├── docs/                     # 프로젝트 문서
├── harness/                  # Claude AI 협업 가이드 (규칙·워크플로우)
├── .claude/                  # Claude Code 설정 및 스킬
├── .github/                  # GitHub Actions CI/CD 워크플로우
├── .vscode/                  # VS Code 공유 편집기 설정
├── ARCHITECTURE.md           # 시스템 전체 아키텍처 지도
├── CLAUDE.md                 # Claude 작업 지침 (체크리스트·금지사항)
├── README.md                 # 프로젝트 소개
├── docker-compose.yml        # 로컬 개발 환경 컨테이너 구성
└── docker-compose.prod.yml   # 프로덕션 컨테이너 구성
```

---

## 1차 폴더 상세

### `backend/`
Kotlin + Spring Boot 3.2 기반 REST API 서버. Gradle Kotlin DSL 빌드.

```
backend/
├── src/
│   ├── main/       # 프로덕션 소스 (kotlin/, resources/)
│   └── test/       # 테스트 소스 (kotlin/, resources/)
├── gradle/         # Gradle Wrapper 설정
├── build/          # 빌드 산출물 (git 제외)
└── .idea/          # IntelliJ 프로젝트 설정 (git 제외)
```

### `frontend/`
Vue 3 + Vite + Pinia + Vue Router 기반 SPA 프론트엔드.

```
frontend/
├── src/
│   ├── assets/       # 정적 자원 (이미지, 폰트 등)
│   ├── components/   # 재사용 가능한 Vue 컴포넌트
│   ├── composables/  # Vue Composition API 훅
│   ├── lib/          # 외부 라이브러리 래퍼·유틸리티
│   ├── router/       # Vue Router 라우트 정의
│   ├── stores/       # Pinia 상태 스토어
│   ├── views/        # 페이지 단위 뷰 컴포넌트
│   ├── App.vue       # 루트 컴포넌트
│   └── main.ts       # 앱 진입점
├── public/           # Vite 정적 서빙 파일
├── dist/             # 빌드 산출물 (git 제외)
└── .vscode/          # 프론트엔드 전용 VS Code 설정
```

### `docs/`
설계 다이어그램, 실행 계획, 학습 자료, 테스트 시나리오 등 프로젝트 문서 모음.

```
docs/
├── diagrams/           # 아키텍처 다이어그램 (PDF + PNG)
│   ├── qusign_application_layer.{pdf,png}
│   ├── qusign_aws_infrastructure.{pdf,png}
│   └── qusign_cicd_pipeline.{pdf,png}
├── exec-plans/         # 단계별 실행 계획 (PLAN.md 포함)
├── learning-materials/ # 팀 학습 자료 (Kotlin 주제별 문서)
├── references/         # 외부 참고 문서·링크 모음
├── test-scenarios/     # QA 테스트 시나리오 정의
└── PORTFOLIO.md        # 포트폴리오 소개 문서
```

### `harness/`
Claude AI와의 협업 규칙, 워크플로우, 보안 정책, 디자인 핸드오프 지침 등을 정의하는 메타 문서 폴더.

```
harness/
├── ARCHITECTURE.md           # (→ 루트 ARCHITECTURE.md 참조)
├── CHANGELOG.md              # 하네스 변경 이력
├── DESIGN_HANDOFF.md         # Claude 디자인 → Vue 이식 가이드
├── DESIGN_PROMPTS.md         # 화면별 디자인 프롬프트
├── PRODUCT_SENSE.md          # 제품 비전·우선순위
├── QUALITY_SCORE.md          # 영역별 품질 등급 기준
├── SECURITY.md               # 보안 금지 사항 상세
├── WORKFLOW.md               # 작업 흐름·플랜 양식
├── architecture-diagram-prompt.md  # 다이어그램 생성 프롬프트
├── diagram-01-application.md
├── diagram-02-aws-infra.md
└── diagram-03-cicd.md
```

### `.claude/`
Claude Code CLI 설정 및 프로젝트 전용 스킬 정의.

```
.claude/
└── skills/
    ├── auto-commit/          # Conventional Commits 자동 커밋 스킬
    └── create-pull-request/  # PR 자동 생성 스킬
```

### `.github/`
GitHub Actions 기반 CI/CD 파이프라인 워크플로우 정의.

```
.github/
└── workflows/
    └── deploy.yml            # 배포 자동화 워크플로우
```

---

## 제외 폴더 (버전 관리 미포함)

| 폴더 | 위치 | 사유 |
|---|---|---|
| `backend/build/` | 백엔드 빌드 산출물 | `.gitignore` |
| `backend/.gradle/` | Gradle 캐시 | `.gitignore` |
| `backend/.idea/` | IntelliJ 설정 | `.gitignore` |
| `frontend/dist/` | Vite 빌드 산출물 | `.gitignore` |
| `frontend/node_modules/` | npm 패키지 | `.gitignore` |
