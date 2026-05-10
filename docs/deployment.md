# 다너 배포 가이드 (daner.kr)

> 도메인: `daner.kr` (구매 완료, 등록기관: dothome.co.kr)
> 현재 운영 조합: **Vercel + Oracle Cloud (Always Free) + Neon**

## 무료 옵션 비교

| 조합 | 비용 | 특징 |
|---|---|---|
| **Vercel + Oracle Cloud + Neon** ✅ 현재 | **$0/월 영구** | 직접 셋업 필요. cold start 없음. ARM 4코어/24GB 또는 AMD 1코어/1GB |
| Vercel + Render free + Neon | $0/월 | 셋업 쉬움. 그러나 15분 무활동 후 ~30초 cold start |
| Vercel + Fly.io 256MB | ~$2/월 | always-on, 셋업 깔끔 |

→ "진짜 0원" 이면서 cold start 없는 게 Oracle. 단점은 셋업 시간(1~2시간) 과 카드 검증.

---

## 0. 사전 준비물

- GitHub 리포: `https://github.com/asle149/daner`
- 도메인 등록기관 콘솔 접근 (DNS 레코드 변경 가능해야 함)
- Google Cloud Console 접근 (OAuth 클라이언트 수정용)
- Vercel 계정 (https://vercel.com)
- **Oracle Cloud 계정** (https://cloud.oracle.com) — 신용카드 검증 필요
- Neon 계정 (https://neon.tech)

---

## 1. Neon — 무료 PostgreSQL

1. https://neon.tech → Sign up (GitHub 가능) → 무료 플랜
2. New Project → Region: **Asia Pacific (Singapore)** → Database name: `daner`
3. **Connection Details** 화면에서 connection string 복사:
   - 형태: `postgresql://username:password@ep-xxx.ap-southeast-1.aws.neon.tech/daner?sslmode=require`

> 무료 한도: 3GB 저장, 사용 안 하면 자동 슬립(다음 쿼리 시 자동 깨어남, 백엔드 입장에선 거의 무체감)

---

## 2. Oracle Cloud — 무료 백엔드 인스턴스

### 2-1. 가입 주의사항

- **Visa / Mastercard 신용카드** 권장 (체크카드는 거절 잦음)
- $1 임시 결제 후 환불됨
- **"Always Free"** 자원만 쓰면 평생 청구 0원. 30일/$300 평가판 끝나도 자동 다운그레이드되어 유료 자원만 정지됨
- "Upgrade and Pay As You Go" 버튼은 **절대 누르지 말 것**
- **Home Region 한 번 정하면 변경 불가** — `Korea Central (Seoul)` 또는 `Korea North (Chuncheon)` 권장

### 2-2. 인스턴스 생성

1. **Compute → Instances → Create Instance**
2. 이름: `daner-api` (자유)
3. Image and Shape:
   - **Image**: Ubuntu 22.04 (`Canonical-Ubuntu-22.04-...`) — Docker 자료 풍부
   - **Shape**: `VM.Standard.A1.Flex` 4 OCPU / 24GB (Always Free) ← ARM, 가장 좋음
   - 만약 "Out of host capacity" 에러: `VM.Standard.E2.1.Micro` (AMD, 1 OCPU/1GB) 도 Always Free
4. SSH Keys:
   - PowerShell 에서 `ssh-keygen -t ed25519 -f $HOME\.ssh\daner_oracle -N '""'`
   - `daner_oracle.pub` 내용을 **Paste public keys** 에 붙여넣기
   - 또는 콘솔에서 "Generate a key pair for me" → `.key` 파일 다운로드 (잃어버리면 영영 못 들어감)
5. Networking:
   - **Create new virtual cloud network** + **Create new public subnet**
   - **"Assign a public IPv4 address"** 반드시 체크
6. Boot volume: 50GB 기본
7. **Create** → 5~10분 후 `RUNNING` + Public IP 표시

### 2-3. Security List 에 80/443 추가 (필수)

1. **Networking → Virtual Cloud Networks → daner-vcn**
2. **Security Lists → Default Security List for daner-vcn**
3. **Ingress Rules → Add Ingress Rules** → 두 줄:

| Source CIDR | IP Protocol | Destination Port |
|---|---|---|
| `0.0.0.0/0` | TCP | `80` |
| `0.0.0.0/0` | TCP | `443` |

### 2-4. SSH 접속 + 서버 초기 셋업

PowerShell 에서:
```powershell
icacls $HOME\.ssh\daner_oracle /inheritance:r /grant:r "${env:USERNAME}:R"
ssh -i $HOME\.ssh\daner_oracle ubuntu@<PUBLIC_IP>
```

서버 안에서 (한 번에):
```bash
# 1) 시스템 업데이트
sudo apt update && sudo apt upgrade -y

# 2) swap 2GB (1GB RAM 인스턴스 안전망, 4코어/24GB 면 생략 가능)
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile && sudo mkswap /swapfile && sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# 3) ufw 22/80/443
sudo ufw allow 22/tcp && sudo ufw allow 80/tcp && sudo ufw allow 443/tcp
sudo ufw --force enable

# 4) Oracle Ubuntu 이미지의 iptables INPUT 에 REJECT 가 깔려 있어
#    ufw 만 열어도 80/443 이 막힘. ACCEPT 를 맨 위에 끼워 넣음.
sudo iptables -I INPUT 1 -p tcp --dport 80 -j ACCEPT
sudo iptables -I INPUT 1 -p tcp --dport 443 -j ACCEPT
sudo apt install -y iptables-persistent
sudo netfilter-persistent save

# 5) Docker
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
exit   # 그룹 적용 위해 SSH 재접속 필요
```

재접속 후 `docker run --rm hello-world` 으로 확인.

### 2-5. 백엔드 코드 + 환경변수 + 컨테이너

```bash
sudo apt install -y git
cd ~ && git clone https://github.com/asle149/daner.git
cd daner/backend
nano .env       # 아래 키들 채우기
```

`.env` 내용 (값은 본인 것):
```env
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://ep-xxx.ap-southeast-1.aws.neon.tech/daner?sslmode=require
DB_USERNAME=...
DB_PASSWORD=...
JWT_SECRET=...
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
GOOGLE_REDIRECT_URI=https://api.daner.kr/v1/auth/google/callback
CORS_ALLOWED_ORIGINS=https://daner.kr,https://www.daner.kr
FRONTEND_URL=https://daner.kr
```

```bash
echo ".env" >> .gitignore

# 빌드 (1GB RAM 이면 5~15분, ARM 24GB 면 1~2분)
docker build -t daner-api .

# 실행 — 재부팅에도 자동 시작
docker run -d \
  --name daner-api \
  --restart unless-stopped \
  -p 8080:8080 \
  --env-file .env \
  -e JAVA_TOOL_OPTIONS="-Xmx500m" \
  daner-api

# 헬스 확인
docker logs --tail 30 daner-api          # "Started DanerApplication" 보일 때까지
curl http://localhost:8080/v1/actuator/health   # {"status":"UP"}
```

### 2-6. Caddy — HTTPS 자동 발급

```bash
sudo apt install -y debian-keyring debian-archive-keyring apt-transport-https curl
curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/gpg.key' | sudo gpg --dearmor -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg
curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt' | sudo tee /etc/apt/sources.list.d/caddy-stable.list
sudo apt update && sudo apt install -y caddy
```

설정 (`/etc/caddy/Caddyfile`):
```caddy
api.daner.kr {
    reverse_proxy localhost:8080
}
```

```bash
sudo systemctl restart caddy
sudo journalctl -u caddy -f   # certificate obtained successfully 보이면 Ctrl+C
```

> Caddy 가 자동으로 Let's Encrypt 에 ACME challenge 시도. DNS 가 인스턴스 IP 를 가리키고 80/443 이 뚫려 있어야 발급 성공. 안 되면 인증서 발급 실패 로그가 찍히고 자동 재시도.

---

## 3. DNS — 도메인 등록기관 콘솔

`api.daner.kr` 가 인스턴스를 가리키게:

| Type | Host/Name | Value | TTL |
|---|---|---|---|
| **A** | `api` | `<인스턴스 PUBLIC IP>` | 3600 |

기존 다른 백엔드(Render 등) CNAME 이 있다면 삭제.

`apex` (`daner.kr`) 와 `www` 는 Vercel 쪽:
- `daner.kr` (apex): A 레코드 → `76.76.21.21`
- `www.daner.kr`: CNAME → `cname.vercel-dns.com`

전파 5~30분.

서버에서 확인:
```bash
dig +short api.daner.kr   # 인스턴스 IP 떠야 함
```

---

## 4. Vercel — 프론트엔드

### 4-1. 프로젝트 생성

1. https://vercel.com → Add New → **Project** → GitHub `asle149/daner` import
2. Configure Project:
   - **Root Directory**: `frontend` ← 핵심
   - **Framework Preset**: Next.js (자동)

### 4-2. 환경변수

| 키 | 값 |
|---|---|
| `NEXT_PUBLIC_API_BASE_URL` | `https://api.daner.kr/v1` |
| `NEXT_PUBLIC_OAUTH_START_URL` | `https://api.daner.kr/v1/auth/google` |
| `NEXT_PUBLIC_SITE_URL` | `https://daner.kr` |

### 4-3. 커스텀 도메인

1. Vercel → 프로젝트 → Settings → **Domains** → `daner.kr` + `www.daner.kr` 추가
2. DNS 가 위 3 단계대로 잡혀 있으면 자동 인증

---

## 5. Google OAuth Console

1. https://console.cloud.google.com → APIs & Services → Credentials
2. OAuth 2.0 클라이언트 → **Authorized redirect URIs** 에 `https://api.daner.kr/v1/auth/google/callback` 등록
3. **OAuth 동의 화면** → External 사용자 허용 (검토 신청 안 해도 테스트 사용자 등록한 계정은 로그인됨)

---

## 6. 동작 확인

- `https://api.daner.kr/v1/actuator/health` → `{"status":"UP"}`
- `https://daner.kr` → 첫 방문이면 `/welcome` → "시작하기" → `/`
- 로그인 → 구글 동의 → 닉네임 입력 → 가입 완료
- 단어 작성 → 댓글 → 답글 → 알림

---

## 7. 운영 — 코드 바뀔 때 재배포

```bash
ssh -i $HOME\.ssh\daner_oracle ubuntu@<IP>

cd ~/daner && git pull
cd backend && docker build -t daner-api .
docker rm -f daner-api
docker run -d \
  --name daner-api --restart unless-stopped \
  -p 8080:8080 --env-file .env \
  -e JAVA_TOOL_OPTIONS="-Xmx500m" \
  daner-api

docker logs --tail 30 daner-api   # "Started" 메시지 확인
```

Caddy 인증서는 자동 갱신(60일마다). 신경 안 써도 됨.

---

## 8. 트러블슈팅

| 증상 | 원인 / 해결 |
|---|---|
| Oracle 인스턴스 생성 시 "Out of host capacity" | ARM A1 자리 부족. AMD E2.1.Micro 로 우회하거나 며칠 재시도 |
| SSH "permissions too open" | PowerShell 에서 `icacls $key /inheritance:r /grant:r "${env:USERNAME}:R"` |
| `dig api.daner.kr` 안 잡힘 | DNS 전파 대기 (보통 5분, 최대 24시간) |
| Caddy `certificate obtained` 안 뜸 | 80번이 외부에서 안 보이는 것. Security List 80/443 + iptables INPUT 1 위치 확인 |
| `https://api.daner.kr` 가 502 | 백엔드 컨테이너 부팅 중 또는 죽음. `docker logs --tail 50 daner-api` |
| Spring Boot 부팅 시 OOM | swap 살아있는지 `free -h`. `JAVA_TOOL_OPTIONS=-Xmx500m` 더 줄이기 |
| `Connection refused` to Neon | `DB_URL` 끝에 `?sslmode=require`, 앞에 `jdbc:` 붙었는지 |
| 401 INVALID_TOKEN on first login | Google Console redirect URI 에 `https://api.daner.kr/v1/auth/google/callback` 등록 |
| CORS 차단 | `CORS_ALLOWED_ORIGINS` 에 정확히 `https://daner.kr` (콤마 구분, 스페이스 X) |
| 프론트가 백엔드 못 부름 | Vercel `NEXT_PUBLIC_*` 변경 후 Redeploy 필요 (build-time 박힘) |

---

## 9. 비용 (현재 조합)

| 서비스 | 비용 |
|---|---|
| daner.kr 도메인 | 연 ~1만원 |
| Vercel hobby | $0 |
| Oracle Cloud Always Free | $0 (영구) |
| Neon free | $0 (3GB 한도) |
| Google OAuth | $0 |
| **월 총합** | **$0** |

> 주의: Oracle 콘솔에서 "Upgrade and Pay As You Go" 누르면 그때부터 카드 청구 시작. 무시하면 영구 무료.

---

## 10. 보안 체크 (배포 전 1회)

- [ ] `JWT_SECRET` 충분히 긴 랜덤값 (`openssl rand -base64 48`)
- [ ] `application-local.yml` 깃에 안 올라감
- [ ] `application-prod.yml` 평문 시크릿 없음 (모두 `${ENV_VAR}` 참조)
- [ ] `.env` 가 `.gitignore` 에 포함
- [ ] Google OAuth secret 깃 히스토리에 없음
- [ ] CORS 가 `*` 아닌 실 도메인 only
- [ ] HTTPS 만 허용 (Caddy 가 80→443 자동 리다이렉트)

---

## 11. 옛 배포 옵션 (참고)

이전엔 Render free 로 운영했음. 15분 cold start 때문에 Oracle 로 이주. Render 셋업이 필요하면 이전 커밋 히스토리(`v0.2.6` 근처) 참고.
