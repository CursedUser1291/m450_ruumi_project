# Security Policy

## Project Overview

This document describes the security considerations, configurations, and best practices for the **m450 Ruumi Project** — a full-stack reservation management application consisting of a Spring Boot backend, React frontend, and MySQL database, all orchestrated via Docker Compose.

---

## Supported Versions

Since this is a school/module project (M450), only the latest version on the `main` branch receives security attention.

| Version | Supported |
|---------|-----------|
| `main` (latest) | ✅ Yes |
| Older branches  | ❌ No    |

---

## Reporting a Vulnerability

If you discover a security issue in this project, please **do not open a public GitHub issue**.

Instead, contact the repository owner directly via GitHub: [@CursedUser1291](https://github.com/CursedUser1291)

Please include:
- A description of the vulnerability
- Steps to reproduce it
- Potential impact
- (Optional) A suggested fix

You can expect an acknowledgement within **3 business days**.

---

## Security Architecture

### Infrastructure

| Component  | Port  | Exposure         |
|------------|-------|------------------|
| Backend    | 8080  | Internal Docker network (not publicly exposed) |
| Frontend   | 4173  | Accessible via browser |
| MySQL DB   | 3306  | Internal Docker network only |

The MySQL port **3306 must never be exposed** to the host or public internet in production-like environments. The `db_data` Docker volume should be treated as sensitive.

### Docker Security

- Use `.env` files for secrets (DB credentials, API URLs) — **never hardcode credentials** in `docker-compose.yml`.
- Ensure `.env` is listed in `.gitignore` and never committed.
- Run containers as **non-root users** where possible.
- Keep base images (e.g., `openjdk`, `node`, `mysql`) up to date and use specific version tags instead of `latest`.

Example `.env` structure (never commit actual values):
```env
MYSQL_ROOT_PASSWORD=changeme
MYSQL_DATABASE=ruumi
MYSQL_USER=appuser
MYSQL_PASSWORD=changeme
VITE_API_URL=http://localhost:8080
```

---

## Backend Security (Spring Boot / Java)

- **Input Validation**: All API inputs must be validated server-side. Do not rely solely on frontend validation.
- **SQL Injection**: Use JPA/Hibernate parameterized queries — never concatenate raw SQL strings with user input.
- **Health Endpoint**: The `/health` endpoint should not expose internal system details (e.g., DB credentials, stack traces) in its response body. Return only `{ "status": "ok" }` or `{ "status": "degraded" }`.
- **Error Handling**: Return generic error messages to the client; log detailed errors server-side only.
- **Dependencies**: Regularly check for vulnerable dependencies using:
  ```bash
  cd backend && mvn dependency-check:check
  ```

---

## Frontend Security (React / Vite)

- **Environment Variables**: Use `VITE_API_URL` via `.env` — never expose secret keys in frontend code, as all Vite env vars are bundled into the client.
- **XSS Prevention**: Avoid using `dangerouslySetInnerHTML`. React escapes output by default — do not bypass this.
- **API Communication**: Always send requests to the backend over the configured `VITE_API_URL`. Do not hardcode IPs or ports in source files.
- **Dependencies**: Audit for vulnerabilities regularly:
  ```bash
  cd frontend && npm audit
  ```

---

## Database Security (MySQL)

- The database is only accessible within the Docker internal network (`db_data` volume).
- Use a **dedicated application user** with minimal privileges (e.g., only `SELECT`, `INSERT`, `UPDATE`, `DELETE` on the app database — no `GRANT` or `DROP`).
- Test data (past/ongoing/upcoming/conflict reservations via SQL script) must not include real personal data.
- Regularly back up the `db_data` volume if used in a persistent environment.

---

## Test Environment Security

- The test database spun up via Docker is **ephemeral** — it is destroyed after `docker compose down`.
- Test SQL scripts must only contain **fictional/synthetic data**, never real user information.
- CI/CD pipelines (if configured) should not store DB credentials in plain-text environment variables; use secrets management.

---

## Known Limitations (School Project Scope)

This project is developed as part of the **M450 module** (Testing) in a controlled school environment. The following security aspects are intentionally **out of scope**:

- Authentication & Authorization (no login system implemented)
- HTTPS / TLS (HTTP only in local dev)
- Rate limiting or DDoS protection
- Production deployment hardening

These would be addressed in a production-grade application.

---

## Security Checklist Before Any Deployment

- [ ] `.env` file is in `.gitignore` and not committed
- [ ] No hardcoded credentials in source code
- [ ] MySQL port 3306 not exposed externally
- [ ] `/health` endpoint returns no sensitive data
- [ ] `npm audit` and `mvn dependency-check` pass without critical issues
- [ ] Test data contains no real personal information
