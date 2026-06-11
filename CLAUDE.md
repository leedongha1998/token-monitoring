# Token Monitoring Service

LLM 토큰 사용량 수집·집계·시각화 서비스. 멀티 프로젝트 지원(API key 인증),
batch ingestion API, Claude Code JSONL 파서, model pricing 이력, daily rollup, React 대시보드.

## 규칙 문서 (작업 전 필독 — 위반 시 빌드/hook이 차단함)

- @docs/rules/08-definition-of-done.md — **완료 기준. 모든 작업에 적용**
- @docs/rules/01-architecture.md — 모듈/레이어 규칙 (ArchUnit으로 강제)
- @docs/rules/02-coding-convention.md — 네이밍, Lombok 정책, 예외/로깅
- @docs/rules/03-git-convention.md — 커밋/브랜치/PR (push·머지는 사용자만)
- @docs/rules/04-testing.md — 레이어별 테스트 전략 (H2 금지, Testcontainers)
- @docs/rules/05-api-design.md — URL/에러 포맷/멱등성 명세

## 모듈 구조

```
monitoring-core   # 도메인 (web 의존 금지)
monitoring-api    # REST, 인증 필터    → core
monitoring-batch  # JSONL 파서, rollup → core
```

도메인 패키지: project / usage / pricing / rollup / common

## 핵심 명령어

```bash
./gradlew spotlessCheck build   # 완료 선언 전 필수 실행 — 출력을 직접 확인할 것
./gradlew :monitoring-api:test  # 모듈 단위 테스트
docker compose up -d            # PostgreSQL 16 (로컬)
```

## 작업 프로토콜

1. 작업 시작: 해당 도메인의 규칙 문서 섹션을 확인한다
2. 스키마 변경 시: Flyway 마이그레이션 파일을 함께 작성한다 (ddl-auto는 validate 고정)
3. 완료 선언 전: `./gradlew spotlessCheck build` 실행 → 출력 확인 → 통과 시에만 완료 보고
4. 기준 미충족 시: 거짓 완료 보고 금지. 08번 문서 §3 형식으로 미완료 보고
5. 범위 밖 발견 사항: 고치지 말고 보고만 한다

## 절대 금지 (요약)

- 테스트 약화/삭제/@Disabled, ArchUnit 규칙 수정
- build.gradle 의존성 추가 (사용자 승인 필수)
- main 직접 커밋, git push, --force, --no-verify
- .env / docker-compose.yml / CI 설정 수정
- API key 평문 저장·로깅
