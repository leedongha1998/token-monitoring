# 프론트엔드 규칙 (React + TypeScript)

원칙: 규칙은 가능한 한 도구(tsc, ESLint)로 강제한다. 이 문서는 도구 설정의 근거와
도구가 못 잡는 설계 규칙만 정의한다. 완료 기준은 `npm run verify` 통과다.

## 1. 기술 스택 (고정 — 버전 포함. 추가/변경은 사용자 승인 필수)

| 영역 | 선택 | 비고 |
|---|---|---|
| 빌드 | Vite | CRA 금지 (deprecated) |
| 언어 | TypeScript strict | any 금지 (ESLint error) |
| 서버 상태 | TanStack Query v5 | **v4 문법 금지** (isLoading→isPending 등 혼용 주의) |
| 클라이언트 상태 | Zustand | 전역 상태는 최후 수단 (§4) |
| 라우팅 | React Router v7 | v5 문법(Switch, useHistory) 금지 |
| API 클라이언트 | openapi-typescript 자동 생성 | **수동 fetch/axios 작성 금지** (§3) |
| 차트 | Recharts | |
| 스타일 | Tailwind CSS | 인라인 style 객체 금지 |
| 테스트 | Vitest + Testing Library | |

package.json에 명시된 버전이 유일한 진실이다. 학습 데이터의 기억과 충돌하면
**package.json과 node_modules의 실제 타입 정의를 따른다.**

## 2. 폴더 구조 (백엔드와 동일하게 도메인 기준)

```
src/
├── features/
│   ├── project/        # 컴포넌트, hooks, 타입을 도메인 안에 함께 둔다
│   │   ├── components/
│   │   ├── hooks/      # useProjects, useCreateProject 등 query hooks
│   │   └── types.ts    # 이 도메인의 UI 전용 타입만 (API 타입은 생성됨)
│   ├── usage/
│   ├── pricing/
│   └── dashboard/
├── shared/
│   ├── api/
│   │   ├── generated/  # openapi-typescript 산출물 — 직접 수정 절대 금지
│   │   └── client.ts   # 공통 fetch 래퍼 (인증 헤더, 에러 변환)
│   ├── components/     # 2개 이상 feature에서 쓰는 것만
│   └── utils/
└── app/                # 라우터, 전역 Provider
```

- feature 간 직접 import 금지 (필요하면 shared로 승격) — ESLint boundaries로 강제
- `shared/api/generated/`는 `npm run codegen`으로만 갱신한다.
  **이 디렉토리 파일을 수정하는 행위는 가장 심각한 위반이다**

## 3. API 레이어 (할루시네이션 차단의 핵심)

- 백엔드 호출은 **생성된 타입 + 공통 client만** 사용한다.
  컴포넌트/훅에서 fetch, axios를 직접 호출하는 코드 금지
- 존재하지 않는 엔드포인트/필드가 필요해 보이면 코드를 지어내지 말고
  **"백엔드에 이 API가 필요하다"고 보고**한다 (docs/rules/05-api-design.md 대조)
- 모든 서버 데이터 접근은 TanStack Query를 통한다. useEffect + fetch 패턴 금지
- queryKey는 도메인별 팩토리로 중앙 관리한다:
  ```ts
  export const usageKeys = {
    all: ['usage'] as const,
    daily: (f: DailyStatsFilter) => [...usageKeys.all, 'daily', f] as const,
  };
  ```
  문자열 리터럴 queryKey 산재 금지 (invalidate 누락의 주범)

## 4. 상태 관리 규칙

우선순위가 낮은 수단을 쓰기 전에 높은 수단으로 해결 불가함을 확인한다:

1. 서버 데이터 → TanStack Query (전역 상태에 복사 금지)
2. 지역 UI 상태 → useState
3. 하위 트리 공유 → props 또는 컴포넌트 합성
4. 진짜 전역(테마, 인증 세션) → Zustand

- 서버 응답을 Zustand에 넣는 패턴 금지 — Query 캐시가 단일 진실
- URL로 표현 가능한 상태(필터, 페이지, 기간)는 searchParams에 둔다

## 5. TypeScript 규칙 (tsconfig/ESLint로 강제)

- `strict: true`, `noUncheckedIndexedAccess: true` 고정. 변경 금지
- `any` 금지 (ESLint error). 불가피하면 `unknown` + 타입 가드
- `as` 단언 최소화. **에러를 단언으로 침묵시키는 행위 금지** —
  타입 에러는 대부분 API 스펙과 코드의 불일치 신호다. 침묵 대신 보고한다
- `// @ts-ignore`, `// eslint-disable` 사용 금지 (정당하면 사유 보고 후 승인)
- 컴포넌트 Props는 `interface {Component}Props`로 명시. 암시적 any props 금지

## 6. 컴포넌트 규칙

- **새 컴포넌트 작성 전에 기존 컴포넌트를 검색한다** (`shared/components/`,
  해당 feature). 유사 컴포넌트가 있으면 중복 생성하지 말고 재사용/확장한다
- 함수 컴포넌트만. 클래스 컴포넌트 금지
- 200줄 초과 시 분리 검토 (soft limit — 사유 보고로 초과 허용)
- 데이터 로딩은 isPending / isError / 빈 데이터 3상태를 모두 처리한다.
  성공 경로만 구현하고 완료 선언하는 것 금지
- 차트 컴포넌트는 데이터 변환 로직(rollup → 차트 포맷)을 컴포넌트 밖
  순수 함수로 분리한다 (테스트 대상)

## 7. 검증 (Definition of Done — 프론트엔드)

```bash
npm run verify   # = type-check + lint + test + build 를 순차 실행하는 스크립트
```

- 완료 선언 전 위 명령을 **실제 실행하고 출력 확인** (백엔드 DoD §3과 동일)
- 다음 행위 금지: tsconfig/eslint 설정 완화, 테스트 약화,
  generated 디렉토리 수동 수정, package.json 의존성 추가
- 신규 훅과 데이터 변환 함수에는 테스트를 작성한다.
  단순 표시 컴포넌트는 테스트 강제하지 않는다 (핵심 인터랙션만)
