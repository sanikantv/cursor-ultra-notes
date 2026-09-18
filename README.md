# Ultra Notes

Private notes for apps and projects [Sani Verma](https://github.com/sanikantv) (`@sanikantv`) built with Cursor after buying **Cursor Ultra on 18 September 2026**.

The site is a **Cloudflare Pages** app at **[https://ultra-notes.pages.dev](https://ultra-notes.pages.dev)**. Every page and API route requires a login. Notes are stored in Cloudflare D1 (SQLite).

Repo: [github.com/sanikantv/cursor-ultra-notes](https://github.com/sanikantv/cursor-ultra-notes)

## Stack

- Cloudflare Pages (project name `ultra-notes` → `ultra-notes.pages.dev`)
- Pages Functions via Hono
- Cloudflare D1
- Owner-only login (PBKDF2 password hash, hashed server-side sessions, login lockout)
- Tailwind CSS
- Markdown notes from `content/notes/`

## Local run

```bash
npm install
cp .dev.vars.example .dev.vars
npm run db:setup
npm run dev
```

Open [http://localhost:8787](http://localhost:8787). You should land on the sign-in page.

Local login (from `.dev.vars.example` only — not for production):

| Field | Value |
| --- | --- |
| Email | `sani@localhost` |
| Password | `local-dev-only` |

## Production deploy (Cloudflare Pages)

One-time on a machine that can open a browser:

```bash
npx wrangler login
```

Create the D1 database and paste the returned `database_id` into `wrangler.jsonc` (replace the local placeholder):

```bash
npx wrangler d1 create cursor-ultra-notes
```

Hash a strong password and a session secret:

```bash
npm run auth:hash -- 'your-strong-password'
openssl rand -hex 32
```

Set Pages secrets (these never go in git):

```bash
printf '%s' 'you@your-email.com' | npx wrangler pages secret put AUTH_EMAIL --project-name=ultra-notes
printf '%s' 'pbkdf2$sha256$...' | npx wrangler pages secret put AUTH_PASSWORD_HASH --project-name=ultra-notes
printf '%s' 'hex-from-openssl' | npx wrangler pages secret put SESSION_SECRET --project-name=ultra-notes
```

Then:

```bash
npm run db:migrate:remote
npm run notes:sync -- --remote
npm run deploy
```

`npm run deploy` publishes to the **ultra-notes** Pages project. Production URL:

**https://ultra-notes.pages.dev**

GitHub Actions (`.github/workflows/deploy-pages.yml`) deploys on push to `main` once these repository secrets exist:

- `CLOUDFLARE_API_TOKEN` (Account → API tokens, edit Cloudflare Pages + D1)
- `CLOUDFLARE_ACCOUNT_ID`

Optional custom domain in the Pages dashboard: **Custom domains → `ultra-notes.your-domain.com`**.

Optional extra lock: Cloudflare Zero Trust **Access** on `ultra-notes.pages.dev` (and any custom domain) so even the login page sits behind Cloudflare’s identity gate.

## Security model

- Fail closed: missing auth secrets never expose notes
- HttpOnly + SameSite=Strict session cookie (`__Host-` prefix on HTTPS)
- Session token is random; D1 stores only `SHA-256(token)`
- PBKDF2-SHA-256 password hashes (100,000 iterations)
- Same-origin check on login/logout POST
- 5 failed logins / 15 minutes / IP (IP is HMAC’d, not stored raw)
- Security headers: CSP with `script-src 'none'`, HSTS on HTTPS, `X-Frame-Options: DENY`, `no-store`, `X-Robots-Tag: noindex`
- No third-party fonts or scripts
- `/api/*` returns 401 without a valid session
- `robots.txt` disallows all crawlers

## Adding notes (Grok Bot / automation)

Commit a file under `content/notes/` and sync D1:

```bash
npm run notes:sync
npm run notes:sync -- --remote
```

Or upsert one file:

```bash
npm run notes:add -- content/notes/my-new-app.md
```

See [`content/examples/`](content/examples/) for the markdown / JSON shape.

## Routes

| Path | Purpose |
| --- | --- |
| `/login` | Owner sign-in |
| `/logout` | POST only, clears session |
| `/` | Note list (auth required). `?q=` search, `?tag=` filter |
| `/notes/:slug` | Note detail (auth required) |
| `/api/notes` | JSON list (auth required) |
| `/api/notes/:slug` | JSON detail (auth required) |

## Project layout

```
content/notes/          # source files a bot can commit
functions/[[path]].ts   # Cloudflare Pages catch-all → Hono
migrations/             # wrangler D1 migrations (notes + auth)
src/index.tsx           # Hono app: auth, list, detail, JSON API
wrangler.jsonc          # Pages project ultra-notes + D1 binding
```
