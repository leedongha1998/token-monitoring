# Git 컨벤션 (커밋 / 브랜치 / PR)

이 규칙은 개발자와 AI 에이전트 모두에게 적용된다.
커밋 메시지 포맷은 commitlint(git hook)로 검증된다. 위반 시 커밋이 거부된다.

## 1. 브랜치 규칙

```
main                          # 보호 브랜치. 직접 커밋 절대 금지
feature/{도메인}-{설명}        # 예: feature/usage-batch-ingestion
fix/{도메인}-{설명}            # 예: fix/pricing-effective-date
chore/{설명}                  # 예: chore/spotless-setup
```

- **main에 직접 커밋하는 것을 금지한다.** 모든 변경은 브랜치 → PR을 거친다
- 브랜치 이름은 소문자 kebab-case, 영문만 사용
- 하나의 브랜치 = 하나의 작업 단위. 여러 도메인을 한 브랜치에 섞지 않는다
- 작업 완료 후 머지된 브랜치는 삭제한다

## 2. 커밋 메시지 포맷 (Conventional Commits)

```
{type}({scope}): {subject}

{body — 선택}
```

### type (이 목록 외 사용 금지)

| type | 용도 |
|---|---|
| feat | 기능 추가 |
| fix | 버그 수정 |
| refactor | 동작 변경 없는 구조 개선 |
| test | 테스트 추가/수정 |
| docs | 문서 변경 |
| chore | 빌드, 설정, 의존성 |
| perf | 성능 개선 |

### scope (도메인 패키지 기준)

`project`, `usage`, `pricing`, `rollup`, `common`, `batch`, `api`, `infra`

### 규칙

- subject는 한국어 허용, 50자 이내, 마침표 없이, 명령형으로
  - 좋음: `feat(usage): batch ingestion API에 idempotency 처리 추가`
  - 나쁨: `feat: 작업했습니다.` / `update code` / `fix bug`
- body는 **왜** 변경했는지를 쓴다. 무엇을 바꿨는지는 diff가 말해준다
- 하나의 커밋 = 하나의 논리적 변경. "여러 가지 수정"류 커밋 금지
- 빌드가 깨진 상태로 커밋하지 않는다 (커밋 전 `./gradlew build` 통과 확인)

## 3. AI 에이전트 추가 제약

- 에이전트가 작성한 커밋에는 트레일러를 포함한다:
  ```
  Co-Authored-By: Claude <noreply@anthropic.com>
  ```
- **에이전트의 다음 행위를 금지한다:**
  - `git push --force` (모든 브랜치)
  - `git push` to main
  - `git commit --no-verify` (hook 우회)
  - `git rebase` / `git reset --hard` (이미 push된 커밋 대상)
  - `git config` 변경
  - 원격 브랜치 삭제
- 에이전트는 커밋까지만 수행한다. **push와 머지는 사용자가 직접** 실행한다
- 충돌(conflict) 해결이 필요한 경우 임의로 해결하지 말고 충돌 내용을 보고한다

## 4. PR 규칙

- PR 크기: 변경 **500줄 이내**를 목표로 한다 (자동 생성 파일 제외).
  넘으면 작업을 쪼개지 못한 것이다 — 다음부터 브랜치를 더 작게 나눈다
- PR 제목은 커밋 메시지와 같은 포맷: `feat(usage): JSONL 파서 구현`
- PR 본문 템플릿 (`.github/PULL_REQUEST_TEMPLATE.md`):

```markdown
## 변경 요약
<!-- 무엇을, 왜 -->

## 검증
- [ ] `./gradlew spotlessCheck build` 통과 (출력 확인함)
- [ ] 신규 코드에 테스트 존재
- [ ] Flyway 마이그레이션 포함 여부 확인 (스키마 변경 시)

## 리뷰 포인트
<!-- 리뷰어(미래의 나)가 집중해서 봐야 할 부분 -->

## 작업 범위 외 발견 사항
<!-- 고치지 않고 보고만 하는 항목. 없으면 "없음" -->
```

- 에이전트가 PR 본문을 작성할 때 "검증" 체크박스는 **실제로 명령을 실행하고
  출력을 확인한 경우에만** 체크한다. 확인 없이 체크하는 것은
  08-definition-of-done.md §3의 거짓 완료 보고에 해당한다

## 5. 금지 파일

다음 파일은 어떤 경우에도 커밋하지 않는다:

- `.env`, `*.pem`, `*.key`, credentials 류 일체
- IDE 설정 (`.idea/`, `*.iml`)
- 로컬 산출물 (`build/`, `logs/`, `*.log`)
- API key, 토큰이 하드코딩된 모든 파일 (커밋 전 diff에서 secret 패턴 자가 검사)
