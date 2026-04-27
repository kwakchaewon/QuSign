---
name: auto-commit
description: Conventional Commits 형식 자동 커밋
---
1. `git status`·`git diff` 병렬. 변경 없으면 종료.
2. 변경 파일을 경로·성격별로 논리 그룹화, 그룹당 개별 커밋.
3. 개별 `git add <files>` → `git commit -m "<type>: <한글 subject>"`.
4. type=feat|fix|refactor|chore|docs|style. subject=한글 30자 이내, 명사형 종결.
5. 논리 그룹 10개 초과 시에만 AskUserQuestion. push·amend·--no-verify 금지.
