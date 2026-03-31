# AuthForge

A full-stack authentication system I built to learn and implement JWT, OAuth2 and role-based access control properly — not just copy-paste from a tutorial.

Stack: **Spring Boot 3.2** (Java 21) · **React + Vite** · **MySQL** · **Docker Compose**

---

## Why I built this

I kept running into auth implementations that either used magic libraries that hid everything, or were too simple to be useful. I wanted to actually understand what happens between a user clicking "login" and a protected API returning data — token generation, refresh rotation, session revocation, all of it.

---

## What it does

- signup and login with bcrypt password hashing
- JWT access tokens (15 min) + refresh tokens (7 days) stored in MySQL
- refresh token rotation — every time you refresh, the old token is invalidated. if someone tries to reuse a revoked token, all sessions get wiped
- google OAuth2 login
- role-based access control — USER, EDITOR, ADMIN, SUPERADMIN
- rate limiting per IP on login and signup endpoints
- audit log for all auth events
- flyway handles all DB migrations so the schema is versioned
- full react frontend — login, signup, dashboard, admin panel

---

## Running locally

```bash
cp .env.example .env
# open .env and set your JWT secrets (see below)

docker compose up --build -d
```

open `http://localhost` — that's it. MySQL, backend and frontend all start together.

to generate JWT secrets:
```bash
openssl rand -base64 64
# run it twice, paste results as JWT_ACCESS_SECRET and JWT_REFRESH_SECRET in .env
```

---

## Project structure

```
authforge/
├── backend/                 # Spring Boot
│   └── src/main/java/com/authforge/
│       ├── config/          # security config, CORS, rate limiting
│       ├── controller/      # auth and user endpoints
│       ├── entity/          # User, RefreshToken, AuditLog
│       ├── repository/      # JPA repos
│       ├── security/
│       │   ├── jwt/         # token generation and filter
│       │   └── oauth2/      # google oauth2 flow
│       └── service/         # business logic
│
├── frontend/                # React + Vite + TypeScript
│   └── src/
│       ├── api/             # axios client with auto token refresh
│       ├── pages/           # login, signup, dashboard, admin
│       ├── store/           # zustand auth store
│       └── hooks/           # useAuth
│
├── docker-compose.yml
├── .github/workflows/       # CI/CD pipeline
└── .env.example
```

---

## API endpoints

**public**
```
POST /api/auth/signup
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
GET  /api/auth/oauth2/authorize/google
```

**protected** (need `Authorization: Bearer <token>`)
```
GET    /api/user/profile
GET    /api/admin/users          → admin only
PATCH  /api/admin/users/:id/role → admin only
DELETE /api/admin/users/:id      → superadmin only
```

---

## Environment variables

| variable | default | notes |
|----------|---------|-------|
| `DB_NAME` | authforge | |
| `DB_USERNAME` | authforge | |
| `DB_PASSWORD` | authforge_secret | change this |
| `JWT_ACCESS_SECRET` | — | required, 64 byte base64 |
| `JWT_REFRESH_SECRET` | — | required, different from access |
| `JWT_ACCESS_EXP_MS` | 900000 | 15 min |
| `JWT_REFRESH_EXP_MS` | 604800000 | 7 days |
| `GOOGLE_CLIENT_ID` | — | from google cloud console |
| `GOOGLE_CLIENT_SECRET` | — | from google cloud console |
| `CORS_ALLOWED_ORIGINS` | http://localhost | comma separated |
| `APP_PORT` | 80 | |

---

## CI/CD

github actions workflow runs on push to main:

1. runs backend tests (uses H2 so no DB needed)
2. builds frontend and checks for lint errors
3. builds docker images and pushes to github container registry
4. SSHs into the server and runs `docker compose up`

you need these secrets set in your github repo settings:

```
VPS_HOST, VPS_USER, VPS_SSH_KEY
DB_NAME, DB_USERNAME, DB_PASSWORD, DB_ROOT_PASSWORD
JWT_ACCESS_SECRET, JWT_REFRESH_SECRET
GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET
CORS_ALLOWED_ORIGINS, OAUTH2_REDIRECT_URIS
```

---

## Running tests

```bash
cd backend
mvn test
```

tests run against H2 in-memory so you don't need MySQL running locally.

---

## local dev without docker

```bash
# backend
cd backend
mvn spring-boot:run

# frontend (separate terminal)
cd frontend
npm install
npm run dev
# runs on localhost:5173, proxies /api to localhost:8080
```

---

## things I'd add next

- email verification on signup
- MFA with TOTP
- password reset flow
- more OAuth providers (github at least)
- move rate limit counters to Redis so it works across multiple instances
