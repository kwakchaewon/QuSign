# DESIGN_PROMPTS.md
# claude.ai/design 화면별 프롬프트 모음

> 각 프롬프트는 **QuSign Design System 프로젝트를 컨텍스트로 첨부**한 상태에서 사용한다.
> 로그인 화면 HTML도 함께 첨부하면 스타일 일관성이 높아진다.
> 완성 후 `harness/DESIGN_HANDOFF.md` §5 포맷으로 Claude Code에 전달한다.

---

## 진행 현황

| 화면 | 프롬프트 | 디자인 | 이식 |
|---|---|---|---|
| 로그인 | ✅ | ✅ | ✅ |
| 회원가입 | ✅ | ⬜ | ⬜ |
| 문서 목록 | ✅ | ⬜ | ⬜ |
| PDF 업로드 / 서명 요청 | ✅ | ⬜ | ⬜ |
| 서명자 (`/sign/:token`) | ✅ | ✅ | ✅ |
| 무결성 검증 | ✅ | ⬜ | ⬜ |
| 서명 요청 상세 조회 | ✅ | ✅ | ✅ |
| PDF 멀티 업로드 | ✅ | ⬜ | ⬜ |
| 서명 요청 취소 (4-6) | ✅ | ⬜ | ⬜ |
| 루트 대시보드 (4-9) | ✅ | ⬜ | ⬜ |
| 계정 설정 (4-10) | ✅ | ⬜ | ⬜ |
| 서비스 아키텍처 구성도 | ✅ | ⬜ | — |

---

## 1. 회원가입 화면

**Vue 경로:** `frontend/src/views/RegisterView.vue`  
**라우트:** `/register`

```
QuSign 회원가입 화면을 만들어줘.

서비스: QuSign — NIST 표준 ML-DSA 기반 PQC 전자서명 SaaS

레이아웃: 로그인 화면과 동일한 centered 카드 레이아웃 유지
(로그인 화면과 한 쌍으로 자연스럽게 전환되어야 함)

입력 필드:
- 이메일 (이메일 형식 유효성 검사)
- 비밀번호 (8자 이상, 강도 표시 바 — weak/fair/strong)
- 비밀번호 확인 (불일치 시 에러)

카드 상단:
- QuSign 로고 + 브랜드명
- PQC 뱃지 (로그인과 동일)
- 제목: "회원가입"
- 설명: "가입 즉시 ML-DSA 키쌍이 생성됩니다"

버튼:
- "계정 만들기" (primary, full-width)
- 하단 "이미 계정이 있으신가요? → 로그인" 링크

카드 하단 Trust strip:
- 키생성 | ML-DSA-65
- 저장방식 | AES-256 암호화
- 전송 | TLS 1.3

상태:
- 기본 / 입력 포커스 / 유효성 오류 / 로딩 (키쌍 생성 중...) / 성공
- 비밀번호 강도 바는 입력하는 동안 실시간 업데이트
- 성공 시 "키쌍 생성 완료" 애니메이션 후 로그인 화면으로 전환 암시

라이트/다크 모드 지원, 모바일 375px 대응
```

---

## 2. 문서 목록 화면

**Vue 경로:** `frontend/src/views/DocumentListView.vue`  
**라우트:** `/documents`  
**인증:** 필요 (JWT)

```
QuSign 문서 목록(대시보드) 화면을 만들어줘.

서비스: QuSign — PQC 전자서명 SaaS
사용자: 로그인한 기업 담당자

레이아웃: 상단 네비게이션 바 + 메인 컨텐츠 (사이드바 없음, 심플하게)

네비게이션 바:
- 좌측: QuSign 로고 + 브랜드명
- 우측: 사용자 이메일 + 로그아웃 버튼 + 라이트/다크 토글

메인 컨텐츠:
- 페이지 제목: "내 문서"
- 우측 상단: "PDF 업로드" 버튼 (primary)

문서 목록 카드 (각 행):
- 파일명 (PDF 아이콘 포함)
- 업로드 날짜
- 상태 뱃지:
  - "서명 완료" (success 초록)
  - "서명 대기" (warning 주황)
  - "검토 중" (neutral 회색)
- 액션 버튼: "서명 요청" / "다운로드" / "검증"

빈 상태 (문서 없을 때):
- 중앙 일러스트 or 아이콘
- "아직 문서가 없어요"
- "PDF 업로드하기" CTA 버튼

목록 상단 필터:
- 전체 / 서명 완료 / 서명 대기 탭

로딩 스켈레톤 상태도 포함

라이트/다크 모드 지원, 모바일 대응
```

---

## 3. PDF 업로드 / 서명 요청 화면

**Vue 경로:** `frontend/src/views/UploadView.vue`  
**라우트:** `/upload`  
**인증:** 필요 (JWT)

```
QuSign PDF 업로드 + 서명 요청 화면을 만들어줘.

서비스: QuSign — PQC 전자서명 SaaS
흐름: 2단계 스텝 (업로드 → 서명 요청)

레이아웃: 상단 네비게이션 바 + 중앙 스텝 카드

스텝 인디케이터 (상단):
① PDF 업로드 → ② 서명 요청 설정 → ③ 완료

--- 스텝 1: PDF 업로드 ---
드래그앤드롭 업로드 존:
- 점선 테두리 박스 (hover 시 primary 색상으로 강조)
- 중앙: PDF 아이콘 + "PDF를 여기에 드래그하거나 클릭해서 선택"
- 파일 크기 제한 안내: "최대 50MB"
- 업로드 후: 파일명 + 크기 + 체크 아이콘 표시
- SHA3-256 해시 계산 중 → 계산 완료 표시

"다음 단계" 버튼 (파일 선택 전 비활성)

--- 스텝 2: 서명 요청 설정 ---
업로드된 파일 요약 (작은 카드로 상단에 표시)

서명자 정보:
- 서명자 이메일 입력 (이메일 유효성 검사)
- "+ 서명자 추가" (최대 5명, 현재 UI는 1명 기준)

메시지 (선택):
- 텍스트에어리어 "서명자에게 전달할 메시지"

"서명 요청 보내기" 버튼 (primary)

--- 스텝 3: 완료 ---
성공 화면:
- 체크 애니메이션
- "서명 요청이 전송되었습니다"
- 서명 링크 복사 버튼 (토큰 URL)
- "문서 목록으로" 버튼

라이트/다크 모드 지원, 모바일 대응
```

---

## 4. 서명자 화면

**Vue 경로:** `frontend/src/views/SignView.vue`  
**라우트:** `/sign/:token`  
**인증:** 불필요 (이메일 링크 직접 접근)

```
QuSign 서명자 화면을 만들어줘.

서비스: QuSign — PQC 전자서명 SaaS
특수 조건: 비회원 접근 가능 (이메일 링크로 진입), 로그인 불필요

레이아웃: 상단 미니 헤더 + 중앙 컨텐츠 (심플, 서명에 집중)

미니 헤더:
- QuSign 로고 + "전자서명 요청" 텍스트
- PQC 보안 뱃지 (pill 스타일, 라이브 dot 포함)

--- 단계 1: 이메일 인증 ---
카드:
- 제목: "본인 확인"
- 설명: "서명 요청을 받은 이메일 주소를 입력해 주세요"
- 이메일 입력 필드
- "인증 코드 받기" 버튼
- 인증 코드 입력 (6자리 숫자, 전송 후 표시)
- 타이머 표시 (3:00 카운트다운)
- "재전송" 링크

--- 단계 2: 문서 검토 + 서명 ---
문서 정보 카드:
- 파일명
- 요청자: [이메일]
- 요청 일시
- 만료 일시 (토큰 만료)
- SHA3-256 해시값 (monospace, 접을 수 있게)

PDF 미리보기 영역:
- 회색 박스 (실제 PDF 렌더링 placeholder)
- "전체 화면으로 보기" 버튼

서명 동의:
- 체크박스: "문서 내용을 확인했으며 서명에 동의합니다"
- 체크박스: "ML-DSA 전자서명의 법적 효력에 동의합니다"

"서명하기" 버튼 (primary, 동의 전 비활성)
서명 중 상태: "ML-DSA-65로 서명 생성 중..." (스피너)

--- 단계 3: 서명 완료 ---
성공 화면:
- 체크 애니메이션
- "서명이 완료되었습니다"
- 서명 일시 + 서명자 이메일
- "서명된 PDF 다운로드" 버튼

라이트/다크 모드 지원, 모바일 375px 최우선 (링크로 접근하므로)
```

---

## 5. 무결성 검증 화면

**Vue 경로:** `frontend/src/views/VerifyView.vue`  
**라우트:** `/verify`  
**인증:** 불필요 (공개 페이지)

```
QuSign 무결성 검증 화면을 만들어줘.

서비스: QuSign — PQC 전자서명 SaaS
특수 조건: 공개 페이지 (로그인 불필요), 누구나 접근 가능

레이아웃: 상단 미니 헤더 + 중앙 카드 (심플, 신뢰감 강조)

미니 헤더:
- QuSign 로고
- "무결성 검증" 타이틀
- PQC 뱃지

--- 상태 1: 파일 업로드 대기 ---
카드 제목: "서명된 PDF를 검증해 보세요"
설명: "QuSign으로 서명된 문서의 진위 여부와 변조 여부를 확인합니다"

드래그앤드롭 업로드 존:
- PDF 아이콘 + "PDF를 여기에 드래그하거나 클릭"
- 지원 형식: PDF only

또는: 서명 링크/토큰 직접 입력 탭
- "서명 토큰으로 검증" 탭 전환
- 토큰 입력 필드 + "검증하기" 버튼

--- 상태 2: 검증 중 ---
로딩 카드:
- 스피너
- "ML-DSA-65 서명값 검증 중..."
- 단계 표시: "해시 추출 → 서명값 확인 → 공개키 검증"

--- 상태 3: 검증 성공 ---
결과 카드 (success 테두리):
- 대형 초록 체크 아이콘
- "검증 완료 — 이 문서는 변조되지 않았습니다"

세부 정보 (접을 수 있는 섹션):
- 서명자: [이메일]
- 서명 일시: 2026-05-06 14:32:11
- 알고리즘: ML-DSA-65
- 문서 해시 (SHA3-256): [monospace 전체값]
- 서명값 앞 16자: [monospace]

"다시 검증하기" 버튼 (secondary)

--- 상태 4: 검증 실패 ---
결과 카드 (error 테두리):
- 대형 빨간 X 아이콘
- "검증 실패 — 이 문서가 변조되었거나 QuSign 서명이 아닙니다"
- 실패 사유 (서명 없음 / 해시 불일치 / 만료된 서명)

라이트/다크 모드 지원, 모바일 대응
```

---

## 6. 서명 요청 상세 조회 화면

**Vue 경로:** `frontend/src/views/DocumentDetailView.vue`  
**라우트:** `/documents/:id`  
**인증:** 필요 (JWT, 요청자 본인만 조회 가능)

```
QuSign 서명 요청 상세 조회 화면을 만들어줘.

서비스: QuSign — NIST 표준 ML-DSA 기반 PQC 전자서명 SaaS
사용자: 서명 요청을 생성한 로그인 사용자 (요청자)
목적: 단일 서명 요청의 진행 현황과 서명자별 상태를 한눈에 파악

레이아웃: 상단 네비게이션 바 + 메인 컨텐츠 (단일 컬럼, 최대 768px 중앙 정렬)

네비게이션 바:
- 좌측: QuSign 로고 + "← 내 문서" 뒤로가기 링크
- 우측: 사용자 이메일 + 로그아웃 버튼 + 라이트/다크 토글

--- 상단: 문서 정보 카드 ---
페이지 제목: "서명 요청 상세"

문서 정보 카드:
- 좌측: PDF 아이콘 (large)
- 파일명 (h2 크기, 긴 이름은 말줄임)
- 업로드 일시
- SHA3-256 해시값: 앞 16자 표시 + "전체 보기" 토글 (monospace, JetBrains Mono)

우측 상단 전체 진행 상태 배지:
- "서명 완료" (success 초록) — 모든 서명자 서명 완료
- "진행 중" (warning 주황) — 일부 서명 대기 중
- "만료됨" (neutral 회색) — 만료 일시 경과

--- 중단: 서명자 목록 ---
섹션 제목: "서명자 현황" + 완료 카운트 (예: "1 / 3명 서명 완료")

각 서명자 행 (카드 리스트):
- 좌측: 아바타 (이메일 이니셜, 상태에 따라 색상 변경)
- 이메일 주소
- 상태 배지:
  - SIGNED: "서명 완료" (success, 초록)
  - PENDING: "서명 대기" (warning, 주황)
  - EXPIRED: "만료" (neutral, 회색)
- 서명 일시: SIGNED일 때만 표시, PENDING/EXPIRED는 "—"
- 우측 액션:
  - SIGNED → "서명된 PDF 다운로드" 버튼 (secondary, 다운로드 아이콘)
  - PENDING → "서명 링크 복사" 버튼 (ghost, 링크 아이콘) + 복사 완료 시 체크 아이콘 피드백
  - EXPIRED → 비활성 표시

--- 하단: 요청 메타데이터 카드 ---
섹션 제목: "요청 정보"

2열 그리드:
- 요청자: [이메일]
- 알고리즘: ML-DSA-65
- 요청 일시: [datetime]
- 만료 일시: [datetime]
  - 만료까지 24시간 이내: 주황 강조 + "곧 만료"
  - 이미 만료: 회색 strikethrough + "만료됨"

로딩 상태:
- 문서 정보 카드 스켈레톤
- 서명자 행 스켈레톤 3개
- 요청 정보 카드 스켈레톤

에러 상태:
- 404: "요청을 찾을 수 없습니다" + "내 문서 목록으로" 버튼
- 403: "조회 권한이 없습니다" + "내 문서 목록으로" 버튼

라이트/다크 모드 지원, 모바일 375px 대응
```

---

## 7. PDF 멀티 업로드 / 서명 요청 화면

**Vue 경로:** `frontend/src/views/RequestView.vue`  
**라우트:** `/request`  
**인증:** 필요 (JWT)  
**기존 화면과의 관계:** 기존 단일 업로드 Step 1을 멀티 업로드로 교체. 3단계 스텝 구조는 유지.

```
QuSign PDF 멀티 업로드 + 서명 요청 화면을 만들어줘.

서비스: QuSign — NIST 표준 ML-DSA 기반 PQC 전자서명 SaaS
사용자: 로그인한 기업 담당자 (요청자)
목적: PDF 최대 5개를 한 번에 업로드하고 서명 요청 생성

레이아웃: 상단 네비게이션 바 + 중앙 스텝 카드 (최대 720px)

네비게이션 바:
- 좌측: QuSign 로고 + "← 내 문서" 뒤로가기 링크
- 우측: 사용자 이메일 + 로그아웃 버튼 + 라이트/다크 토글

스텝 인디케이터 (카드 상단):
① PDF 업로드 → ② 서명자 설정 → ③ 완료
(각 스텝 원형 번호 + 라벨, 현재 스텝 primary 강조)

--- 스텝 1: PDF 멀티 업로드 ---

드래그앤드롭 업로드 존:
- 점선 테두리 박스 (hover 시 primary 색상으로 강조)
- 중앙: PDF 스택 아이콘 + "PDF를 여기에 드래그하거나 클릭해서 선택"
- 안내 문구: "최대 5개 · 파일당 최대 50MB · PDF만 지원"
- 파일이 1개 이상 있을 때: 드롭존 높이 축소 + "파일 추가" 링크 텍스트로 대체

파일 목록 (업로드된 파일이 있을 때 드롭존 아래 표시):
각 파일 행:
  - 좌측: PDF 아이콘 (작은 크기, 빨간 계열)
  - 파일명 (최대 너비 넘으면 말줄임, hover 시 전체 표시 tooltip)
  - 파일 크기 (회색, 예: "2.4 MB")
  - SHA3-256 해시: "계산 중..." 스피너 → 완료 시 앞 12자 + "…" (monospace, JetBrains Mono)
  - 우측: 삭제 버튼 (X 아이콘, hover 시 빨간 강조)
  - 하단: 업로드 진행 바 (0% → 100%, 완료 시 초록 체크로 전환)
    - 상태: 대기 중(회색) / 업로드 중(primary 진행 바) / 완료(초록) / 오류(빨간)

파일 추가 버튼:
- 파일 목록 하단, 파일이 1~4개일 때만 표시
- "+ PDF 추가" (ghost 스타일, 점선 테두리)
- 5개 도달 시 숨김 (버튼 없애고 "최대 5개 파일이 추가되었습니다" 텍스트로 대체)

5개 초과 시도 경고:
- 인라인 경고 배너 (드롭존 하단): "⚠ 최대 5개까지 업로드할 수 있습니다. X개를 제거해 주세요."
- 경고 배너는 해결되면 자동으로 사라짐

전체 진행 요약 (파일이 1개 이상일 때 파일 목록 상단):
- "3개 파일 · 해시 계산 완료 2 / 3" (업로드 중 상태 요약)
- 모두 완료되면 "3개 파일 업로드 완료 ✓"

"다음 단계" 버튼:
- 조건: 파일 1개 이상 + 모든 파일 업로드·해시 계산 완료
- 조건 미충족 시 비활성 + 이유 tooltip ("파일 업로드가 완료되지 않았습니다")

--- 스텝 2: 서명자 설정 ---

상단 업로드된 파일 요약 카드:
- 파일 수 + 총 용량 (예: "PDF 3개 · 총 7.2 MB")
- 파일명 목록 (pill 형태로 나열, 넘치면 "+N개 더" 표시)

서명자 입력 섹션:
- 이메일 입력 필드 + "추가" 버튼
- 추가된 서명자 목록 (이메일 pill + X 삭제)
- 최대 5명, 초과 시 입력 비활성 + "최대 5명까지 추가 가능합니다"
- 이메일 중복 입력 시 인라인 경고: "이미 추가된 이메일입니다"

메시지 (선택):
- 텍스트에어리어 "서명자에게 전달할 메시지 (선택사항)"
- 최대 500자 / 글자 수 카운터

"서명 요청 보내기" 버튼 (primary, full-width):
- 조건: 서명자 1명 이상
- 로딩 상태: "요청 생성 중..."

--- 스텝 3: 완료 ---

성공 화면:
- 대형 체크 애니메이션
- "서명 요청이 전송되었습니다"
- 문서별 서명 링크 목록 (파일명 + 링크 복사 버튼, 각 행)
  - 복사 완료 시 버튼 → "복사됨 ✓" 피드백 (2초 후 원복)
- "전체 링크 복사" 버튼 (모든 링크 한 번에 클립보드)
- "내 문서 목록으로" 버튼 (secondary)

상태 목록:
- 업로드 진행 중 (파일별 진행 바)
- 해시 계산 중 (스피너)
- 파일 오류 (오류 행 빨간 배경 + 재시도 버튼)
- 서버 오류 (파일 행에 "업로드 실패 — 재시도" 버튼)
- 5개 초과 경고 (인라인 배너)

라이트/다크 모드 지원, 모바일 375px 대응
```

---

## 8. 서명 요청 취소 (4-6)

**Vue 경로:** `frontend/src/views/DocumentDetailView.vue` (취소 버튼 추가), `frontend/src/views/SignView.vue` (취소 상태 화면 추가)  
**라우트:** `/documents/:id` (취소 버튼), `/sign/:token` (취소 안내)  
**인증:** 취소 버튼 — 필요 (JWT, 요청자 본인) / 취소 안내 — 불필요

```
QuSign 서명 요청 취소 UI를 만들어줘.

서비스: QuSign — NIST 표준 ML-DSA 기반 PQC 전자서명 SaaS
목적: 요청자가 PENDING 상태인 서명 요청을 철회하는 흐름 + 서명자가 취소된 링크에 접근했을 때의 안내 화면

--- 화면 A: 서명 요청 상세 페이지 — 취소 버튼 ---

기존 서명자 목록 행 (PENDING 상태 서명자)에 취소 버튼 추가:
- 위치: 서명자 행 우측 액션 영역 (서명 링크 복사 버튼 우측)
- 스타일: ghost 버튼, 빨간 계열 텍스트 ("요청 취소"), 아이콘: X 또는 slash-circle
- hover 시: 빨간 배경으로 전환 (destructive 액션임을 강조)
- SIGNED / EXPIRED 상태 서명자 행에는 표시하지 않음

--- 화면 B: 취소 확인 모달 ---

취소 버튼 클릭 시 모달 오버레이 표시:
- 모달 크기: 최대 400px, 중앙 정렬
- 상단 아이콘: 주황 경고 아이콘 (warning, 원형 배경)
- 제목: "서명 요청을 취소하시겠어요?"
- 설명:
  - "[서명자 이메일]에게 보낸 서명 요청이 취소됩니다"
  - "취소 후에는 되돌릴 수 없으며, 서명 링크가 즉시 만료됩니다"
- 경고 박스 (destructive 배경, 연한 빨간):
  - "⚠ 서명자가 링크를 클릭해도 서명할 수 없게 됩니다"
- 버튼 2개:
  - "취소하기" (destructive, 빨간 primary) — 확인 후 요청 취소 실행
  - "닫기" (secondary) — 모달 닫기
- 로딩 상태: "취소 처리 중..." (스피너, 버튼 비활성)

--- 화면 C: SignerView 취소 상태 안내 화면 ---

취소된 토큰으로 /sign/:token 접근 시:
(기존 이메일 인증 단계를 대체하여 이 화면만 표시)

레이아웃: 상단 미니 헤더 + 중앙 안내 카드

안내 카드:
- 상단 아이콘: 회색 X-circle (대형, 64px)
- 제목: "취소된 서명 요청입니다"
- 설명:
  - "요청자가 이 서명 요청을 취소했습니다"
  - "서명이 필요하다면 요청자에게 문의해 주세요"
- 카드 하단 정보 (옵션, 요청자가 누군지 알 수 있게):
  - "요청자: [이메일]" (있을 경우)
  - "취소 일시: [datetime]" (있을 경우)
- 버튼: 없음 (단순 안내 화면)

카드 스타일: 테두리 neutral 회색, 배경 약간 어두운 tone

라이트/다크 모드 지원, 모바일 375px 대응
```

---

## 9. 루트 대시보드 (4-9)

**Vue 경로:** `frontend/src/views/DashboardView.vue`  
**라우트:** `/` (로그인 시)  
**인증:** 필요 (JWT) — 비로그인 시 랜딩 페이지로 분기

```
QuSign 루트 대시보드 화면을 만들어줘.

서비스: QuSign — NIST 표준 ML-DSA 기반 PQC 전자서명 SaaS
사용자: 로그인한 기업 담당자
목적: 서비스 전체 현황을 한눈에 파악하고 주요 액션에 빠르게 접근

레이아웃: 상단 네비게이션 바 + 메인 컨텐츠 (최대 1024px 중앙 정렬)

네비게이션 바:
- 좌측: QuSign 로고 + 브랜드명
- 우측: 사용자 이메일 + "계정 설정" 링크 + 로그아웃 버튼 + 라이트/다크 토글

--- 섹션 1: 환영 메시지 ---
- "안녕하세요, [이메일 앞부분]님" (h1, 가볍게)
- 오늘 날짜 표시 (YYYY-MM-DD, 회색 소자)

--- 섹션 2: 요약 통계 카드 (상단 4-grid) ---
각 카드: 아이콘 + 수치 + 라벨 + 전주 대비 변화 (optional)

카드 1: 전체 문서
- 아이콘: 문서 스택
- 수치: 12 (예시)
- 라벨: "전체 문서"

카드 2: 서명 완료
- 아이콘: 체크 배지
- 수치: 8
- 라벨: "서명 완료"
- 색상: success (초록)

카드 3: 서명 대기
- 아이콘: 시계
- 수치: 3
- 라벨: "서명 대기 중"
- 색상: warning (주황)

카드 4: 만료됨
- 아이콘: X-circle
- 수치: 1
- 라벨: "만료된 요청"
- 색상: neutral (회색)

카드 스타일: 흰색 배경 카드, subtle 그림자, border-radius 12px

--- 섹션 3: 최근 서명 요청 (5건) ---
섹션 제목: "최근 요청" + "전체 보기 →" 링크 (우측)

테이블 또는 카드 리스트:
각 행:
- 파일명 (PDF 아이콘 포함, 긴 이름 말줄임)
- 서명자 이메일 (복수일 경우 "2명" 등으로 표시)
- 요청 일시 (상대 시간: "3시간 전", "어제")
- 상태 배지: 서명 완료 / 대기 중 / 만료됨
- 우측: "상세 보기" 링크 버튼 (ghost)

빈 상태:
- 중앙 아이콘 + "아직 서명 요청이 없어요"
- "첫 서명 요청 만들기" CTA 버튼 (primary)

--- 섹션 4: 빠른 액션 ---
섹션 제목: "빠른 액션"

2개 또는 3개 액션 카드 (가로 배열):
카드 A: 서명 요청 생성
- 아이콘: 문서 + 화살표
- 제목: "서명 요청 보내기"
- 설명: "PDF를 업로드하고 서명자에게 요청 링크를 발송합니다"
- 버튼: "시작하기" (primary)

카드 B: 무결성 검증
- 아이콘: 방패 + 체크
- 제목: "서명 검증하기"
- 설명: "서명된 PDF의 위변조 여부를 즉시 확인합니다"
- 버튼: "검증 페이지로" (secondary)

카드 C (optional): 계정 설정
- 아이콘: 사람 + 설정
- 제목: "계정 관리"
- 설명: "비밀번호 변경, 알림 설정"
- 버튼: "설정으로" (ghost)

로딩 상태:
- 통계 카드 4개 스켈레톤
- 최근 요청 행 3개 스켈레톤

라이트/다크 모드 지원, 모바일 375px 대응 (통계 카드: 2×2 그리드로 전환)
```

---

## 10. 계정 설정 (4-10)

**Vue 경로:** `frontend/src/views/AccountSettingsView.vue`  
**라우트:** `/settings`  
**인증:** 필요 (JWT)

```
QuSign 계정 설정 화면을 만들어줘.

서비스: QuSign — NIST 표준 ML-DSA 기반 PQC 전자서명 SaaS
사용자: 로그인한 사용자
목적: 비밀번호 변경 / 알림 수신 설정 / 계정 탈퇴

레이아웃: 상단 네비게이션 바 + 메인 컨텐츠 (단일 컬럼, 최대 640px 중앙 정렬)

네비게이션 바:
- 좌측: QuSign 로고 + "← 대시보드" 뒤로가기 링크
- 우측: 사용자 이메일 + 로그아웃 버튼 + 라이트/다크 토글

페이지 제목: "계정 설정"

--- 섹션 1: 계정 정보 ---
카드:
- 이메일 (읽기 전용, 회색 배경 입력 필드처럼 표시)
- 가입 일시 (읽기 전용)
- ML-DSA 공개키 앞 16자 (monospace, JetBrains Mono) + "전체 보기" 토글

--- 섹션 2: 비밀번호 변경 ---
카드 제목: "비밀번호 변경"

입력 필드:
- 현재 비밀번호 (password type, 눈 아이콘으로 표시/숨김)
- 새 비밀번호 (8자 이상, 강도 표시 바: weak/fair/strong)
- 새 비밀번호 확인 (불일치 시 인라인 에러)

버튼: "변경하기" (primary)
- 로딩 상태: "변경 중..."
- 성공 상태: 인라인 성공 메시지 "비밀번호가 변경되었습니다" (초록, 3초 후 사라짐)
- 오류 상태: "현재 비밀번호가 올바르지 않습니다" (빨간)

--- 섹션 3: 알림 설정 ---
카드 제목: "알림 설정"

토글 스위치 목록:
- "서명 완료 알림" — 서명자가 서명을 완료했을 때 이메일 수신 (기본: ON)
- "만료 D-1 리마인더" — 서명 요청 만료 24시간 전 이메일 수신 (기본: ON)
- "보안 알림" — 새 기기 로그인, 비밀번호 변경 시 이메일 수신 (기본: ON, 비활성화 불가 표시)

토글 변경 즉시 자동 저장 (저장 버튼 없음):
- 저장 중: 토글 옆 미니 스피너
- 저장 완료: "저장됨 ✓" (1초 후 사라짐)

--- 섹션 4: 계정 탈퇴 ---
카드 제목: "위험 영역" (danger zone)
카드 스타일: 빨간 테두리 (subtle), 배경 연한 빨간

설명:
- "계정을 탈퇴하면 로그인 정보가 삭제됩니다"
- "서명 이력과 문서는 법적 보존 의무에 따라 유지될 수 있습니다"

버튼: "계정 탈퇴" (destructive, 빨간 ghost 또는 outline)
클릭 시 확인 모달:
- 제목: "정말 탈퇴하시겠어요?"
- 설명: "이 작업은 되돌릴 수 없습니다"
- 확인 입력: "내 이메일 주소를 입력해 주세요" (이메일 일치 시에만 탈퇴 버튼 활성)
- 버튼: "탈퇴하기" (destructive, 이메일 불일치 시 비활성) / "취소" (secondary)

섹션 간 구분선 (hr)으로 명확한 영역 분리

라이트/다크 모드 지원, 모바일 375px 대응
```

---

## 11. 서비스 아키텍처 구성도

**용도:** 포트폴리오 / 발표 슬라이드 / README 첨부  
**참고 스타일:** AWS 레퍼런스 아키텍처 슬라이드 — 상단 EC2 그룹 + 하단 외부 서비스 2단 구조  
**디자인 시스템 토큰:** white canvas + surface-soft 카드 + hairline 테두리 + Figma 타이포그래피

```
Draw a "Service Architecture" diagram for QuSign, a PQC (Post-Quantum Cryptography)
electronic signature web service. Style: clean, light-background, AWS reference architecture
slide. One page, no scrolling.

━━━ CANVAS ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Size       : 1440 × 810px  (16:9)
Background : #ffffff
Title      : "Service Architecture"
             top-left, 36px Inter weight 700, #000000
Divider    : 1px solid #e6e6e6, full width below title

━━━ LAYOUT (2-row, matches AWS reference style) ━

                  [ROUTE 53 — small, above EC2 group]
                          ↓
Row 1 (top half):
  [CLIENT]  ──→  [EC2 GROUP — large center box]

Row 2 (bottom half, three boxes centered under EC2):
  [S3 STORAGE]    [AWS SES]    [GitHub Actions + ECR]

Vertical arrows from EC2 GROUP bottom edge down to each bottom box.
Bottom boxes are centered horizontally under the EC2 group.

━━━ COMPONENT CARD ━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Every technology = one card:
  Background  : #f7f7f5
  Border      : 1px solid #e6e6e6
  Border-radius: 8px
  Width/Height: 110 × 110px
  Content     : official brand logo (64px) centered + name below (13px Inter 500, #000000)
  NO sub-text, NO drop shadow

━━━ GROUP BOX ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Rounded rect enclosing related cards:
  Border-radius : 20px
  Border        : 2px solid [group color]
  Background    : [group tint]
  Label         : top of box, 12px JetBrains Mono ALL CAPS, [group color]

━━━ ROW 1 — LEFT: CLIENT ━━━━━━━━━━━━━━━━━━━━━━

No border box. Single card + label above.

  Label (above card) : "CLIENT"  12px JetBrains Mono, #888888
  Card 1 : [Vue.js green V logo]   name: "Vue 3 SPA"

Arrow → right toward EC2:
  Style : 2px solid #5aad6e, filled arrowhead
  Label : "HTTPS 443"  on arrow, 11px JetBrains Mono, #5aad6e

━━━ ROW 1 — CENTER: EC2 GROUP ━━━━━━━━━━━━━━━━━

Group color : #e8760a (AWS orange)
Group tint  : #fff8f2
Group label : [AWS EC2 icon 16px]  "EC2  ·  ap-southeast-1a  ·  t3.small"
Group width : ~780px  (dominant element, ~54% of canvas width)

Cards inside, arranged in THREE rows top → bottom:

  Row A — 요청 경로 (상단, 좌→우):
    Card: [Nginx green N]          name: "Nginx"
    Card: [Spring leaf + Kotlin K] name: "Spring Boot 3 / Kotlin"

    Arrow between: ←→ 2px #e8760a, label "proxy"

  Row B — 데이터 레이어 (중단, Spring Boot 바로 아래에 나란히):
    Card: [MariaDB gold logo]  name: "MariaDB 10.11"
    Card: [Redis red logo]     name: "Redis 7"

    Arrow UP: Spring Boot → MariaDB : 2px #aaaaaa, label "JDBC"
    Arrow UP: Spring Boot → Redis   : 2px #aaaaaa, label "Pub/Sub"

  Row C — PQC 암호 (하단, 점선 서브박스):
    Inner dashed box:
      Border: 1.5px dashed #9b7ee0, background: #f5f1fd, radius: 12px
      Label: "PQC CRYPTO"  11px JetBrains Mono, #9b7ee0
      Card: [shield icon]          name: "liboqs-java"
      Card: [Apache feather icon]  name: "PDFBox"

    Arrow UP: Spring Boot → PQC box : 2px dashed #9b7ee0, label "JNI"

  NOTE: Row A and Row B are visually separated — Row A shows the request path,
        Row B shows data storage. MariaDB/Redis are BELOW Spring Boot, not beside it.

━━━ ROUTE 53 (EC2 그룹 위, 중앙 상단) ━━━━━━━━━━━

Small floating element, centered above the EC2 group box. No group border.

  Label (above card) : "DNS"  12px JetBrains Mono, #888888
  Card: [Route 53 purple icon]  name: "Route 53"
  Sub  : "qusign.link"  11px Inter, #888888

  Arrow ↓ downward → EC2 group top edge:
    Style: 2px solid #aaaaaa, filled arrowhead pointing down
    Label: "qusign.link → 3.0.193.52"  11px JetBrains Mono, #888888

━━━ ROW 2 — BOTTOM THREE BOXES ━━━━━━━━━━━━━━━━

Three equal-width group boxes, horizontally centered under EC2 group.
Total width of three boxes = EC2 group width (~780px).
Each box ~244px wide. 24px gaps between them. (244×3 + 24×2 = 780px)

BOX A — S3 STORAGE
  Group color : #3d8f3d (green)
  Group tint  : #f2faf2
  Label       : "STORAGE"
  Card: [S3 green bucket icon]  name: "Amazon S3"
  Caption below card: "PDF 파일 저장 · VPC Endpoint"  11px Inter, #666666

BOX B — AWS SES
  Group color : #c47a1e (amber)
  Group tint  : #fffaf2
  Label       : "EMAIL"
  Card: [SES orange envelope icon]  name: "Amazon SES"
  Caption: "서명 요청 · 만료 알림 이메일"  11px Inter, #666666

BOX C — CI/CD
  Group color : #9b7ee0 (lilac)
  Group tint  : #f5f1fd
  Label       : "CI / CD"
  Card: [GitHub octocat]         name: "GitHub"
  Card: [GitHub Actions icon]    name: "GitHub Actions"
  Card: [AWS ECR orange icon]    name: "Amazon ECR"
  (3 cards in a row inside this box)

━━━ ARROWS — EC2 → BOTTOM BOXES ━━━━━━━━━━━━━━━

All downward from EC2 group bottom edge:
  → S3     : 2px solid #3d8f3d,  label "PDF 업로드 / 다운로드"
  → SES    : 2px solid #c47a1e,  label "서명 링크 이메일 발송"
  → CI/CD  : 2px dashed #9b7ee0, label "docker pull / SSM deploy"

━━━ ARROW LABEL STYLE ━━━━━━━━━━━━━━━━━━━━━━━━━

  Font       : 11px JetBrains Mono
  Color      : same as arrow line
  Background : white pill (#ffffff), 1px solid [arrow color] 40% opacity
  Padding    : 2px 8px, border-radius 50px
  Position   : centered on arrow line

━━━ TYPOGRAPHY ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  Page title      : 36px Inter 700, #000000
  Group label     : 12px JetBrains Mono ALL CAPS, letter-spacing 0.5px
  Card name       : 13px Inter 500, #000000
  Caption text    : 11px Inter 400, #666666
  Arrow label     : 11px JetBrains Mono, arrow color

━━━ CONSTRAINTS ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

- Total elements on canvas: ~14 cards, ~9 arrows — keep it this count, no more
- EC2 group is the dominant visual (~54% canvas width, ~45% canvas height)
- 48px white margin on all 4 edges
- 32px gap between all group boxes
- No gradients, no drop shadows, flat design
- All brand logos must be recognizable official icons (not generic shapes)
- Korean labels on arrows exactly as written above
```

---

## 공통 주의사항

- 모든 화면에 **라이트/다크 모드** 적용
- **모바일 375px** 기준 대응 (서명자 화면은 모바일 최우선)
- 버튼·입력 높이 **56px** 고정 (tokens.css 기준)
- 폰트: Pretendard (본문) + JetBrains Mono (해시값·코드)
- 완성 후 → `harness/DESIGN_HANDOFF.md` §5 포맷으로 전달
