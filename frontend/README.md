# 다너 (Daner) Frontend

Next.js 16 + React 19 + TypeScript + Tailwind 4 + TanStack Query.

백엔드 API에 의존합니다 — `../backend/` 가 `http://localhost:8080/v1`에서 실행 중이어야 합니다.

## 로컬 실행

```bash
# 1. 의존성 설치
npm install

# 2. 환경변수
cp .env.local.example .env.local

# 3. 개발 서버
npm run dev
```

`http://localhost:3000` 접속.

## 환경변수

`.env.local`:

| 키 | 의미 | 기본값 |
|---|---|---|
| `NEXT_PUBLIC_API_BASE_URL` | 백엔드 API 베이스 | `http://localhost:8080/v1` |
| `NEXT_PUBLIC_OAUTH_START_URL` | OAuth 시작 URL (백엔드의 `/auth/google`) | `http://localhost:8080/v1/auth/google` |
| `NEXT_PUBLIC_SITE_URL` (배포 시) | sitemap.xml 베이스 | `http://localhost:3000` |

## 빌드

```bash
npm run build
npm run start
```

## Vercel 배포

1. Vercel 새 프로젝트 → 이 리포 연결, **Root Directory 를 `frontend`로 설정** (모노레포)
2. Environment Variables:
   - `NEXT_PUBLIC_API_BASE_URL` = 배포된 백엔드 URL (예: `https://api.daner.com/v1`)
   - `NEXT_PUBLIC_OAUTH_START_URL` = `${API_BASE}/auth/google`
   - `NEXT_PUBLIC_SITE_URL` = 프론트 도메인
3. Build Command, Output Directory는 기본값
4. Deploy

배포 후 백엔드 측에서 두 가지 갱신 필요:
- Google Cloud Console redirect URI에 프로덕션 콜백 추가: `${BACKEND}/auth/google/callback`
- 백엔드 `app.cors.allowed-origins` 환경변수에 프론트 도메인 추가
- 백엔드 `app.frontend.url` 도 프론트 도메인으로

## 폴더 구조

```
src/
├── app/                  # 라우팅 (App Router)
│   ├── auth/{success,signup}/
│   ├── me/{notifications,}/
│   ├── words/[word]/
│   ├── layout.tsx
│   ├── page.tsx          # 홈
│   ├── robots.ts
│   └── sitemap.ts
├── components/
│   ├── me/Bookshelf
│   ├── ui/{Header, NotificationBell, Skeleton}
│   └── word/{CommentList, CommentItem, ReplyList,
│              Composer, LikeButton, DeleteButton, AuthorLine}
├── lib/
│   ├── api/{client, endpoints}
│   ├── auth/{tokens, AuthContext}
│   ├── hooks/QueryProvider
│   └── util/{normalizeWord, timeAgo}
└── types/api.ts          # 백엔드 ApiResponse / 도메인 타입
```
