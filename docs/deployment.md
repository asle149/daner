# 다너 배포 가이드 (daner.kr)

> 도메인: `daner.kr` (구매 완료)

## 무료 vs 유료

| 조합 | 비용 | 특징 |
|---|---|---|
| **Vercel + Render + Neon** | **$0/월 영구** | 백엔드가 15분 무활동 후 잠들어 첫 요청에 ~30초 cold start. 그 외 100% 무료 |
| Vercel + Fly.io + Neon | $0~$3/월 | Fly 무료 한도 좁음, 트래픽 늘면 청구 |
| Vercel + Railway + Railway Postgres | **$5 크레딧 후 유료** | Railway는 무료 아님. 한 달 $5 크레딧만, 이후 사용량 청구 |

**돈 안 쓰려면 1번 (Vercel + Render + Neon)**. 이 가이드는 그 기준.

---

## 0. 사전 준비물

- GitHub 리포: `https://github.com/asle149/daner` (이미 푸시됨)
- 도메인 등록기관 콘솔 접근 (네임서버/DNS 변경 가능해야 함)
- Google Cloud Console 접근 (OAuth 클라이언트 수정용)
- Vercel 계정 (https://vercel.com)
- Render 계정 (https://render.com)
- Neon 계정 (https://neon.tech)

---

## 1. Neon — 무료 PostgreSQL

1. https://neon.tech → Sign up (GitHub 가능) → 무료 플랜
2. New Project → Region: **Asia Pacific (Singapore)** (한국에서 가장 가까움) → Database name: `daner`
3. 생성되면 **Connection Details** 화면에서 connection string 복사:
   - 형태: `postgresql://username:password@ep-xxx.ap-southeast-1.aws.neon.tech/daner?sslmode=require`
4. 메모해둠 (Render에 넣을 거)

> **무료 한도**: 3GB 저장, 한 프로젝트, 사용 안 하면 자동 슬립(다음 쿼리 시 자동 깨어남)

---

## 2. Render — 무료 백엔드

### 2-1. 프로젝트 생성

1. https://render.com → New + → **Web Service**
2. GitHub `asle149/daner` 연결
3. 설정:
   - **Name**: `daner-api`
   - **Region**: Singapore
   - **Branch**: `main`
   - **Root Directory**: `backend` ← 모노레포 핵심
   - **Runtime**: **Docker** (Dockerfile 자동 감지) — 우리는 `backend/Dockerfile` 추가했음
   - **Plan**: **Free**

### 2-2. 환경변수 (Environment)

Render → 서비스 → Environment 탭. 아래 키 등록:

| 키 | 값 |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_URL` | `jdbc:postgresql://ep-xxx.ap-southeast-1.aws.neon.tech/daner?sslmode=require` ← Neon URL 앞에 `jdbc:` 붙이고 `?sslmode=require` 유지 |
| `DB_USERNAME` | (Neon connection의 user 부분) |
| `DB_PASSWORD` | (Neon connection의 password 부분) |
| `JWT_SECRET` | `openssl rand -base64 48` 결과값 |
| `GOOGLE_CLIENT_ID` | Google Console에서 발급 |
| `GOOGLE_CLIENT_SECRET` | Google Console에서 발급 |
| `GOOGLE_REDIRECT_URI` | `https://api.daner.kr/v1/auth/google/callback` |
| `CORS_ALLOWED_ORIGINS` | `https://daner.kr,https://www.daner.kr` |
| `FRONTEND_URL` | `https://daner.kr` |

### 2-3. 커스텀 도메인

1. Render → 서비스 → **Settings** → **Custom Domain** → `api.daner.kr` 추가
2. 표시되는 CNAME 타깃 메모 (예: `daner-api.onrender.com`)
3. 도메인 등록기관 DNS:
   - **Type**: CNAME, **Host**: `api`, **Value**: `daner-api.onrender.com`
4. Render가 인증서 자동 발급 (5~15분)

### 2-4. 무료 플랜의 제약

- **15분 무요청 시 잠듦**, 다음 요청에 cold start (~30초)
- 첫 사용자가 접속하면 살짝 느릴 수 있음
- 서비스가 깨어나면 빠름
- 트래픽 늘면 Starter ($7/월)로 업그레이드

---

## 3. Vercel — 프론트엔드

### 3-1. 프로젝트 생성

1. https://vercel.com → Add New → **Project** → GitHub `asle149/daner` import
2. Configure Project:
   - **Root Directory**: `frontend` ← 핵심
   - **Framework Preset**: Next.js (자동)
   - 나머지 기본

### 3-2. 환경변수

| 키 | 값 |
|---|---|
| `NEXT_PUBLIC_API_BASE_URL` | `https://api.daner.kr/v1` |
| `NEXT_PUBLIC_OAUTH_START_URL` | `https://api.daner.kr/v1/auth/google` |
| `NEXT_PUBLIC_SITE_URL` | `https://daner.kr` |

### 3-3. 커스텀 도메인

1. Vercel → 프로젝트 → Settings → **Domains** → `daner.kr` + `www.daner.kr` 추가
2. Vercel이 보여주는 DNS:
   - `daner.kr` (apex): **A** 레코드 → `76.76.21.21`
   - `www.daner.kr`: **CNAME** → `cname.vercel-dns.com`
3. 도메인 등록기관 DNS에 등록
4. 5~30분 후 인증서 자동 발급

---

## 4. Google OAuth Console

1. https://console.cloud.google.com → APIs & Services → Credentials
2. 기존 OAuth 2.0 클라이언트 ID 클릭 → **Authorized redirect URIs**에 추가:
   - `https://api.daner.kr/v1/auth/google/callback`
3. **OAuth 동의 화면** → External 사용자 허용. (검토 신청 안 해도 테스트 사용자에 등록한 계정은 로그인됨)

---

## 5. 동작 확인

- `https://daner.kr` → 첫 방문이면 `/welcome` → "시작하기!" → `/`
- `https://api.daner.kr/v1/actuator/health` → `{"status":"UP"}` (cold start면 첫 요청 30초 대기)
- 헤더 우측 로그인 → 구글 동의 → 닉네임 입력 → 가입 완료

---

## 6. 예상 월 비용 (free 조합)

| 서비스 | 비용 |
|---|---|
| 도메인 daner.kr | (이미 결제) ~연 1만~2만원 |
| Vercel hobby | $0 |
| Render free | $0 (cold start 제약) |
| Neon free | $0 (3GB 저장 한도) |
| Google OAuth | $0 |
| **월 총합** | **$0** |

트래픽이 늘어 free 한도 초과하면:
- Render Starter $7/월 (cold start 없음)
- Neon Pro 단계는 $19~/월 (지금 단계엔 불필요)

---

## 7. 트러블슈팅

### Railway 빌드 실패 (현재 사용자가 겪는 문제)

**증상**: Railway에서 "Failed to build an image" — Build > Build image 단계에서 멈춤

**원인 후보**:
1. **Root Directory 미설정** — Railway가 리포 루트에서 Gradle을 찾으려 함. `backend`로 지정 필요
2. **Nixpacks가 Java 버전 못 잡음** — Spring Boot 3.x는 Java 17 필요. Nixpacks 기본은 가끔 다름
3. **메모리 부족** — Gradle 빌드는 메모리 많이 먹음

**해결**: Railway 쓸 거면 우리가 추가한 `backend/Dockerfile`을 인식시키기. Railway 대시보드에서:
- Settings → **Builder** → **Dockerfile** 선택
- **Dockerfile Path**: `backend/Dockerfile`
- **Build Context**: `backend`

이러면 Nixpacks 우회하고 우리 Dockerfile로 직접 빌드. 안정적.

**또는 Render로 옮기기 권장** (이 가이드 본문 — 진짜 무료에 더 안정적).

### 일반 트러블

| 증상 | 원인·확인 |
|---|---|
| `daner.kr` DNS 안 풀림 | 도메인 등록기관 DNS 전파 대기 (보통 5분, 최대 24시간) |
| Vercel `404` | Root Directory `frontend` 설정 확인 |
| 백엔드 `Connection refused` to DB | DB_URL 형식 확인 (`jdbc:postgresql://...`로 시작, `?sslmode=require` 포함) |
| 401 INVALID_TOKEN on first login | OAuth redirect URI Google Console에 `https://api.daner.kr/v1/auth/google/callback` 등록 확인 |
| CORS 막힘 | `CORS_ALLOWED_ORIGINS`에 `https://daner.kr` 포함 확인 (콤마 구분, 스페이스 X) |
| Render 첫 요청 30초 | free 플랜 cold start. 정상. Starter 플랜이면 사라짐 |
| 프론트가 백엔드 못 부름 | Vercel `NEXT_PUBLIC_API_BASE_URL`이 `https://api.daner.kr/v1` (끝 `/v1`까지) 확인. `NEXT_PUBLIC_*`는 빌드 타임에 박히므로 변경 후 재배포 필요 |
| Neon DB 연결 안 됨 | connection string 끝의 `?sslmode=require` 제거하지 말 것. `jdbc:` 접두 필수 |

---

## 8. 보안 체크 (배포 전 1회)

- [ ] `JWT_SECRET` 충분히 긴 랜덤값 (32바이트+)
- [ ] `application-local.yml`은 깃에 안 올라감 (확인됨)
- [ ] `application-prod.yml`에 평문 시크릿 없음 (모두 `${ENV_VAR}` 참조)
- [ ] Google OAuth secret 깃 히스토리에 없음 (있으면 Reset Secret)
- [ ] CORS는 `*` 아님 (실 도메인만)
- [ ] HTTPS만 허용 (Vercel/Render 둘 다 자동)

---

## 9. 향후

- 트래픽 늘면 Render Starter $7/월 (cold start 제거)
- DB 백업: Neon은 free에서도 PITR 7일 유지
- 모니터링: Render/Vercel 기본 메트릭으로 시작, 부하 늘면 Sentry 추가
