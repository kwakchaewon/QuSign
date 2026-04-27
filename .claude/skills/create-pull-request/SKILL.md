---
name: create-pull-request
description: 현재 브랜치 → 사용자가 선택한 base 브랜치로 PR 타이틀·본문 자동 생성 및 생성
---
1. BASE 결정: 인자로 브랜치명 전달 시 그대로 사용. 없으면 `git branch -r --format="%(refname:short)" | sed 's|origin/||'` 로 목록 출력 후 AskUserQuestion으로 선택.
2. `git log origin/<BASE>..HEAD --no-merges --format="%s"` 실행. 결과 없으면 종료.
3. type 우선순위(feat>fix>refactor>chore>docs)로 dominant-type 결정. 타이틀: `<type>: <한글 40자 이내 핵심 요약>`.
4. 본문 고정 형식: Summary(feat·refactor·docs·chore 커밋), Bug Fixes(fix 커밋만, 없으면 섹션 생략), Test Plan(변경 기반 1~3개 추론).
5. 타이틀·본문 출력 후 확인 요청. 거절 시 피드백 반영 후 재출력.
6. 승인 시 `gh pr create --base <BASE> --title "..." --body "..."` 실행. push·force·--no-verify 금지.

