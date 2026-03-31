# 🔐 AuthForge — Production Java Full-Stack Auth System

**Spring Boot 3.2 + React (Vite) + MySQL + Docker Compose**

A production-ready, zero-config authentication system with JWT, OAuth2 (Google), RBAC, refresh token rotation, rate limiting, Flyway migrations, and a full React dashboard.

---

## 🚀 Quick Start (One Command)

```bash
# 1. Clone your repo
git clone https://github.com/YOUR_USERNAME/authforge.git && cd authforge

# 2. Set up environment
cp .env.example .env
# Edit .env — at minimum set the JWT secrets:
# openssl rand -base64 64   (run twice, paste as JWT_ACCESS_SECRET and JWT_REFRESH_SECRET)

# 3. Launch everything
docker compose up --build -d

# 4. Open in browser
open http://localhost
```

That's it. MySQL starts, Flyway runs migrations, Spring Boot starts, React is served via Nginx.

---

## 📁 Project Structure

```
authforge/
├── backend/                    # Spring Boot 3.2 (Java 21)
│   ├── src/main/java/com/authforge/
│   │   ├── config/             # Security, CORS, Rate limiting, AppProperties
│   │   ├── controller/         # AuthController, UserController
│   │   ├── dto/                # Request/Response DTOs
│   │   ├── entity/             # User, RefreshToken, AuditLog
│   │   ├── exception/          # GlobalExceptionHandler + custom exceptions
│   │   ├── repository/         # Spring Data JPA repositories
│   │   ├── security/
│   │   │   ├── jwt/            # JwtProvider, JwtAuthenticationFilter
│   │   │   └── oauth2/         # Google OAuth2, UserPrincipal, Handlers
│   │   └── service/            # AuthService, RefreshTokenService, UserService, AuditService
│   ├── src/main/resources/
│   │   ├── application.yml     # Full config (env-var driven)
│   │   └── db/migration/       # Flyway SQL migrations
│   └── Dockerfile              # Multi-stage build (JDK builder → JRE runtime)
│
├── frontend/                   # React 18 + Vite + TypeScript + Tailwind
│   ├── src/
│   │   ├── api/                # Axios client (auto-refresh) + API calls
│   │   ├── components/         # Layout, FormElements UI
│   │   ├── hooks/              # useAuth hook
│   │   ├── pages/              # Login, Signup, Dashboard, Admin, OAuth2Redirect
│   │   ├── store/              # Zustand auth store (persisted)
│   │   └── types/              # TypeScript interfaces
│   ├── nginx.conf              # SPA routing + API proxy
│   └── Dockerfile              # Multi-stage: Node builder → Nginx runtime
│
├── docker/mysql/init.sql       # MySQL init script
├── docker-compose.yml          # Full stack: DB + Backend + Frontend
├── .github/workflows/ci-cd.yml # GitHub Actions: Test → Build → Docker → Deploy
├── .env.example                # All environment variables documented
└── .gitignore
```

---

## 🔑 API Reference

### Auth Endpoints (public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/signup` | Register new user |
| POST | `/api/auth/login` | Login, receive JWT + refresh token |
| POST | `/api/auth/refresh` | Rotate refresh token, get new access token |
| POST | `/api/auth/logout` | Revoke refresh token (`?allSessions=true` for all) |
| GET  | `/api/auth/oauth2/authorize/google` | Start Google OAuth2 flow |

### Protected Endpoints (require `Authorization: Bearer <token>`)

| Method | Endpoint | Role Required |
|--------|----------|--------------|
| GET | `/api/user/profile` | Any authenticated user |
| GET | `/api/admin/users` | ADMIN, SUPERADMIN |
| PATCH | `/api/admin/users/{uuid}/role` | ADMIN, SUPERADMIN |
| DELETE | `/api/admin/users/{uuid}` | SUPERADMIN only |

---

## 🛡 Security Features

| Feature | Implementation |
|---------|---------------|
| Password hashing | BCrypt, cost=12 |
| JWT access tokens | HS256, 15-min TTL, issuer+audience validation |
| Refresh tokens | Opaque (64 random bytes), 7-day TTL, stored in MySQL |
| Token rotation | Old refresh token revoked on every use |
| Reuse attack detection | Reuse of a revoked token revokes ALL user sessions |
| Rate limiting | Bucket4j — 10 logins/15min, 5 signups/hr per IP |
| Timing-safe login | bcrypt runs even when user not found |
| RBAC | 4 roles (USER → SUPERADMIN), method-level `@PreAuthorize` |
| Audit logging | Async DB logging for all auth events |
| CORS | Strict origin whitelist, configurable per env |
| Token cleanup | Scheduled job purges expired/revoked tokens hourly |

---

## ⚙️ Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_NAME` | `authforge` | MySQL database name |
| `DB_USERNAME` | `authforge` | MySQL user |
| `DB_PASSWORD` | `authforge_secret` | MySQL password |
| `JWT_ACCESS_SECRET` | **required** | 64-byte base64 secret |
| `JWT_REFRESH_SECRET` | **required** | 64-byte base64 secret (different from access) |
| `JWT_ACCESS_EXP_MS` | `900000` | Access token TTL in ms (15 min) |
| `JWT_REFRESH_EXP_MS` | `604800000` | Refresh token TTL in ms (7 days) |
| `GOOGLE_CLIENT_ID` | — | From Google Cloud Console |
| `GOOGLE_CLIENT_SECRET` | — | From Google Cloud Console |
| `CORS_ALLOWED_ORIGINS` | `http://localhost` | Comma-separated allowed origins |
| `APP_PORT` | `80` | Host port to expose frontend |

Generate secrets:
```bash
openssl rand -base64 64
```

---

## 🚢 CI/CD Pipeline (GitHub Actions)

On every push to `main`:
1. **Backend**: Maven test → package JAR
2. **Frontend**: ESLint → Vite build
3. **Docker**: Build & push images to GitHub Container Registry
4. **Deploy**: SSH to VPS → pull images → `docker compose up`

### Required GitHub Secrets

| Secret | Description |
|--------|-------------|
| `VPS_HOST` | Your server IP or hostname |
| `VPS_USER` | SSH username |
| `VPS_SSH_KEY` | Private SSH key (add public key to server) |
| `DB_*` | All database credentials |
| `JWT_ACCESS_SECRET` | Access token signing key |
| `JWT_REFRESH_SECRET` | Refresh token signing key |
| `GOOGLE_CLIENT_ID/SECRET` | OAuth2 credentials |
| `CORS_ALLOWED_ORIGINS` | Your production domain |
| `OAUTH2_REDIRECT_URIS` | `https://yourdomain.com/oauth2/redirect` |

---

## 🧪 Running Tests

```bash
cd backend
mvn test
```

Tests use H2 in-memory DB — no MySQL needed for testing.

---

## 🔧 Local Development (without Docker)

```bash
# Backend
cd backend
cp src/test/resources/application.yml src/main/resources/application-dev.yml
# Edit application-dev.yml with your local MySQL details
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Frontend
cd frontend
npm install
npm run dev   # http://localhost:5173 — proxies /api to localhost:8080
```

---

## 📋 Production Checklist

- [ ] Set strong `JWT_ACCESS_SECRET` and `JWT_REFRESH_SECRET` (64+ bytes)
- [ ] Change all default DB passwords
- [ ] Set up HTTPS (add an Nginx reverse proxy or use Cloudflare)
- [ ] Configure `CORS_ALLOWED_ORIGINS` to your actual domain
- [ ] Register Google OAuth2 credentials with your production redirect URI
- [ ] Remove exposed DB port (`3306`) from `docker-compose.yml`
- [ ] Set up log rotation and monitoring (e.g. Grafana + Loki)
- [ ] Enable MySQL backups
