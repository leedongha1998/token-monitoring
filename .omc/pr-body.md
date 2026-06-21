## 변경 요약

멀티모듈 Spring Boot 백엔드 + React 대시보드로 구성된 LLM 토큰 사용량 수집·집계·시각화 서비스 전체 구현.

- **인프라**: Gradle 멀티모듈, PostgreSQL 16 docker-compose, Claude Code hook(위험 명령 차단), Spotless/ArchUnit 빌드 게이트
- **Project·ApiKey 도메인**: 엔티티 + Flyway V1 마이그레이션 + X-API-Key SHA-256 인증 필터 + 프로젝트 관리 REST API
- **Usage 도메인**: UsageEvent 단건/배치 ingestion API, JSONL 파서, idempotency 처리
- **Pricing·Rollup 도메인**: ModelPricing 이력 + DailyRollup 배치 + Stats API (`/v1/stats/daily`, `/v1/stats/summary`)
- **스케줄러**: `@EnableScheduling` + RollupScheduler (매일 UTC 01:00 전날 rollup 실행)
- **React 대시보드**: Vite + React 18 + TypeScript, 날짜 범위 필터, 모델 필터, 요약 카드, SVG 일별 비용 차트, 일별 통계 테이블

## 검증
- [x] `./gradlew spotlessCheck build` 통과 (BUILD SUCCESSFUL, 20 tasks, 0 failures)
- [x] 신규 코드에 테스트 존재 (단위·슬라이스·통합·ArchUnit)
- [x] Flyway 마이그레이션 포함 여부 확인 (스키마 변경 포함)

## 리뷰 포인트
- `ApiKeyAuthenticationFilter`: `shouldNotFilter()` 미구현 — 관리 API(`/v1/projects`, `/v1/api-keys`)도 X-API-Key 인증이 적용됨. 초기엔 내부망 전제이나, 관리 API 전용 인증 추가 시 필터 경로 분리 필요
- `DailyChart.tsx`: 날짜 라벨은 14개 초과 시 일부 생략 (가독성 위해 `i % ceil(n/14) === 0` 조건으로 선택 표시)

## 작업 범위 외 발견 사항
없음
