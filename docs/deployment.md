# 다너 배포 가이드 (daner.kr)

> 도메인: `daner.kr` (구매 완료)
> 프론트: Vercel — `daner.kr`
> 백엔드: Railway — `api.daner.kr`
> DB: Railway PostgreSQL 플러그인

---

## 0. 사전 준비물

- GitHub 리포: `https://github.com/asle149/daner` (이미 푸시됨)
- 도메인 등록기관 콘솔 접근 (네임서버/DNS 변경 가능해야 함)
- Google Cloud Console 접근 (OAuth 클라이언트 수정용)
- Vercel 계정
- Railway 계정 (또는 Fly.io)

---

## 1. 백엔드 배포 (Railway)

### 1-1. 프로젝트 생성

1. https://railway.app → New Project → **Deploy from GitHub repo**
2. `asle149/daner` 선택
3. Root Directory를 **`backend`** 로 지정 (모노레포라 중요)
4. Build / Start command는 자동 감지되지만 안 되면:
   - Build: `./gradlew bootJar -x test`
   - Start: `java -jar build/libs/daner-0.0.1-SNAPSHOT.jar`

### 1-2. PostgreSQL 추가

1. 같은 프로젝트에서 → New → **Database → PostgreSQL**
2. Railway가 자동으로 `DATABASE_URL` 등 환경변수를 백엔드 서비스에 주입

### 1-3. 백엔드 환경변수

Railway → 백엔드 서비스 → Variables. 아래 키들 등록:

| 키 | 값 | 비고 |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` | application-prod.yml 활성화 |
| `DB_URL` | `${{Postgres.DATABASE_URL}}` | Railway 변수 참조. `jdbc:postgresql://...` 형태로 와야 함. 안 되면 `${{Postgres.DATABASE_PUBLIC_URL}}` 사용 후 `jdbc:` 접두 보정 |
| `DB_USERNAME` | `${{Postgres.PGUSER}}` | |
| `DB_PASSWORD` | `${{Postgres.PGPASSWORD}}` | |
| `JWT_SECRET` | (32바이트+ 랜덤) | `openssl rand -base64 48` |
| `GOOGLE_CLIENT_ID` | (Google Console에서 발급) | |
| `GOOGLE_CLIENT_SECRET` | (Google Console에서 발급) | |
| `GOOGLE_REDIRECT_URI` | `https://api.daner.kr/v1/auth/google/callback` | 아래 1-4 끝낸 후 |
| `CORS_ALLOWED_ORIGINS` | `https://daner.kr,https://www.daner.kr` | 프론트 도메인 |
| `FRONTEND_URL` | `https://daner.kr` | OAuth 콜백 후 redirect |

> **DB_URL 주의**: Railway의 `DATABASE_URL`은 `postgresql://...` 형식이라 Spring이 받기 위해선 `jdbc:postgresql://...`이 필요. Railway 환경변수 화면에서 `${{Postgres.DATABASE_URL}}` 참조 후 확인 — 안 맞으면 직접 `jdbc:postgresql://${PGHOST}:${PGPORT}/${PGDATABASE}` 형태로 조립.

### 1-4. 커스텀 도메인

1. Railway → 백엔드 서비스 → Settings → Networking → **Custom Domain** → `api.daner.kr`
2. 표시되는 CNAME 타깃 (예: `xxxxx.up.railway.app`) 메모
3. 도메인 등록기관 DNS 설정에 가서:
   - **Type**: CNAME
   - **Host**: `api`
   - **Value**: `xxxxx.up.railway.app`
4. 확인 후 Railway가 인증서 자동 발급 (1~5분)

### 1-5. 배포 트리거

GitHub `main`에 push하면 Railway가 자동 빌드·배포. 첫 배포 시 Flyway가 V1~V4를 자동 적용.

---

## 2. 프론트엔드 배포 (Vercel)

### 2-1. 프로젝트 생성

1. https://vercel.com → Add New → **Project** → GitHub `asle149/daner` import
2. Configure Project:
   - **Root Directory**: `frontend`
   - **Framework Preset**: Next.js (자동 감지)
   - **Build Command**: 기본값
   - **Output Directory**: 기본값

### 2-2. 환경변수

| 키 | 값 |
|---|---|
| `NEXT_PUBLIC_API_BASE_URL` | `https://api.daner.kr/v1` |
| `NEXT_PUBLIC_OAUTH_START_URL` | `https://api.daner.kr/v1/auth/google` |
| `NEXT_PUBLIC_SITE_URL` | `https://daner.kr` |

### 2-3. 커스텀 도메인

1. Vercel → 프로젝트 → Settings → **Domains** → `daner.kr` 추가
2. 같은 화면에 `www.daner.kr`도 추가 (root → www 또는 www → root 자동 redirect 설정 가능)
3. Vercel이 보여주는 DNS 레코드:
   - `daner.kr` (apex): **A** 레코드 → `76.76.21.21`
   - `www.daner.kr`: **CNAME** → `cname.vercel-dns.com`
4. 도메인 등록기관 DNS에 등록
5. 5~30분 후 인증서 자동 발급, `https://daner.kr` 접속 가능

### 2-4. 배포 트리거

`main` push → Vercel 자동 빌드·배포.

---

## 3. Google OAuth 콘솔 갱신

1. https://console.cloud.google.com → APIs & Services → Credentials
2. 기존 OAuth 2.0 클라이언트 ID 클릭 → **Authorized redirect URIs**에 추가:
   - `https://api.daner.kr/v1/auth/google/callback`
3. **OAuth 동의 화면** → 외부 사용자 허용 (Publishing status: In production)
   - 동의 화면 검토 신청 필요할 수 있음 (테스트 사용자만 허용해도 일단 동작)

---

## 4. 동작 확인

- `https://daner.kr` 접속 → 첫 방문이면 `/welcome` 노출 → "시작하기!" → `/`
- `https://api.daner.kr/v1/actuator/health` → `{"status":"UP"}`
- `https://api.daner.kr/v1/swagger-ui.html` → OpenAPI UI
- 헤더 우측 로그인 클릭 → 구글 동의 → 신규면 닉네임 페이지 → 가입 후 홈

---

## 5. 트러블슈팅

| 증상 | 원인·확인 |
|---|---|
| `daner.kr` DNS 안 풀림 | 도메인 등록기관 DNS 전파 대기 (보통 5분, 최대 24시간) |
| Vercel `404` | Root Directory `frontend` 설정 확인 |
| 백엔드 `Connection refused` to DB | Railway Variables에서 DB_URL 형식 확인 (`jdbc:` 접두) |
| 401 INVALID_TOKEN on first login | OAuth redirect URI Google Console에 `https://api.daner.kr/v1/auth/google/callback` 등록 확인 |
| CORS 막힘 | 백엔드 `CORS_ALLOWED_ORIGINS`에 `https://daner.kr` 포함 확인 (콤마 구분, 스페이스 X) |
| OAuth 콜백 후 프론트 못 찾음 | 백엔드 `FRONTEND_URL`이 `https://daner.kr`인지 확인 |
| 프론트가 백엔드 못 부름 | Vercel `NEXT_PUBLIC_API_BASE_URL`이 `https://api.daner.kr/v1` (끝 `/v1`까지) 확인. `NEXT_PUBLIC_*`는 빌드 타임에 박히므로 변경 후 재배포 필요 |

---

## 6. 보안 체크 (배포 전 1회)

- [ ] `JWT_SECRET` 충분히 긴 랜덤값 (32바이트+)
- [ ] `application-local.yml`은 깃에 안 올라감 (`.gitignore` 확인됨)
- [ ] `application-prod.yml`에 평문 시크릿 없음 (모두 `${ENV_VAR}` 참조)
- [ ] Google OAuth secret 깃 히스토리에 없음 (있으면 Reset Secret)
- [ ] CORS는 `*` 아님 (실 도메인만)
- [ ] HTTPS만 허용 (Vercel/Railway 둘 다 자동)

---

## 7. 향후

- 도메인 이메일 (`hello@daner.kr` 등) 필요시 등록기관 메일 호스팅 또는 Cloudflare Email Routing
- 모니터링: Railway/Vercel 기본 메트릭으로 시작, 부하 늘면 Sentry 추가
- DB 백업: Railway Postgres 자동 백업 활성화 (요금제 따라)
- 알림 이메일 (회원가입 후 인사 등): 추후 v0.3.x에서 검토
