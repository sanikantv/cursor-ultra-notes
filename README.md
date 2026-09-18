# Cursor Ultra Build Notes

A personal notes / log site for apps and projects [Sani Verma](https://github.com/sanikantv) (`@sanikantv`) built with Cursor after buying **Cursor Ultra on 18 September 2026**.

The homepage lists notes newest first (title, date, summary, tags). Each note has a detail page with a markdown body. You can search by text or filter by tag.

**Repo:** [github.com/sanikantv/cursor-ultra-notes](https://github.com/sanikantv/cursor-ultra-notes)

## Stack

- **Cloudflare Workers** with static assets (current Workers / Pages deploy path)
- **Cloudflare D1** (SQLite) in production; the same schema locally via `wrangler d1` / local SQLite
- **Hono** + TypeScript
- **Tailwind CSS**
- Markdown bodies rendered with `marked`

No secrets are required to run or deploy this repo. Cloudflare login is only needed for remote D1 and `wrangler deploy`.

## Local run

```bash
npm install
npm run db:setup
npm run dev
```

Then open [http://localhost:8787](http://localhost:8787).

What those commands do:

| Command | What it does |
| --- | --- |
| `npm install` | Installs dependencies (creates `package-lock.json` on first run) |
| `npm run db:setup` | Applies D1 migrations to the **local** SQLite DB, then upserts every file in `content/notes/` |
| `npm run dev` | Builds CSS and starts `wrangler dev` on port 8787 |

Useful extras:

```bash
npm run db:migrate          # local migrations only
npm run notes:sync          # re-read content/notes into local D1
npm run typecheck
```

If the homepage says the database is empty, you skipped `npm run db:setup`.

## Adding notes (for Grok Bot / automation)

There is no CMS and no secret-gated write API in the repo. A bot adds notes by **committing a file** and/or **running a small CLI** that upserts into D1 by `slug`.

### Option A — commit a markdown or JSON file (preferred)

1. Create `content/notes/<slug>.md` (or `.json`).
2. Use the frontmatter / JSON shape below.
3. Sync into the database:

```bash
# local D1 (development)
npm run notes:sync

# production D1 (after wrangler login)
npm run notes:sync -- --remote
```

Markdown example (`content/notes/my-new-app.md`):

```markdown
---
title: My new app
slug: my-new-app
summary: One or two sentences for the homepage card.
tags: [cursor-ultra, workers]
created_at: 2026-09-18
project_url: https://github.com/sanikantv/my-new-app
---

The markdown body. GitHub-flavored markdown is fine.
```

JSON example — see [`content/examples/note.example.json`](content/examples/note.example.json).

Seed notes already in the repo:

- [`content/notes/starting-cursor-ultra.md`](content/notes/starting-cursor-ultra.md)
- [`content/notes/cursor-ultra-build-notes.md`](content/notes/cursor-ultra-build-notes.md)

### Option B — CLI insert from a single file or stdin

```bash
# from a file
npm run notes:add -- content/notes/my-new-app.md
npm run notes:add -- path/to/note.json

# from stdin (good for a bot that already has JSON)
cat path/to/note.json | npm run notes:add -- --stdin

# production D1
npm run notes:add -- --remote content/notes/my-new-app.md
```

Exact JSON fields:

| Field | Required | Notes |
| --- | --- | --- |
| `title` | yes | Homepage + detail heading |
| `body` | yes | Markdown |
| `slug` | no | Auto-generated from title if omitted; unique key |
| `summary` | no | Short card text |
| `tags` | no | Array of strings, stored as JSON in D1 |
| `created_at` | no | `YYYY-MM-DD` (defaults to today) |
| `updated_at` | no | Defaults to `created_at` |
| `project_url` | no | Optional repo or live URL |

Re-running the CLI or sync **updates** an existing row with the same `slug`.

### Verify

```bash
# after npm run dev
curl -s http://localhost:8787/api/notes | head
curl -s http://localhost:8787/api/notes/starting-cursor-ultra
```

## Database schema

Canonical SQL: [`schema.sql`](schema.sql)  
Wrangler migration: [`migrations/0001_create_notes.sql`](migrations/0001_create_notes.sql)

```sql
CREATE TABLE notes (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  title TEXT NOT NULL,
  slug TEXT NOT NULL UNIQUE,
  summary TEXT NOT NULL DEFAULT '',
  body TEXT NOT NULL,              -- markdown
  tags TEXT NOT NULL DEFAULT '[]', -- JSON array of strings
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,
  project_url TEXT
);
```

## Deploy (Cloudflare Workers / Pages)

The project is deploy-ready. Cloudflare login cannot live in this repo — run this once on a machine that can open a browser:

```bash
npx wrangler login
```

Create the remote D1 database and paste its id into `wrangler.toml` (replace the local placeholder `00000000-0000-4000-8000-000000000001`):

```bash
npx wrangler d1 create cursor-ultra-notes
```

Then:

```bash
npm run db:migrate:remote
npm run notes:sync -- --remote
npm run deploy
```

`npm run deploy` and `npm run pages:deploy` both run `wrangler deploy` (Workers with static assets — the current Cloudflare Pages / Workers path). After login that is the one command that publishes the site.

Dashboard alternative: **Workers & Pages → Create → connect this GitHub repo**.

- Build command: `npm run css`
- Deploy command: `npx wrangler deploy`

No API tokens or other secrets are required in the repository.

## Project layout

```
content/notes/          # source files a bot can commit
content/examples/       # JSON + markdown templates
migrations/             # wrangler D1 migrations
schema.sql              # readable schema copy
scripts/add-note.ts     # CLI upsert from one file or stdin
scripts/sync-notes.ts   # upsert every file in content/notes
src/index.tsx           # Hono app: list, detail, search, JSON API
wrangler.toml           # D1 binding + assets + observability
```

## Routes

| Path | Purpose |
| --- | --- |
| `/` | Note list, newest first. `?q=` text search, `?tag=` tag filter |
| `/notes/:slug` | Note detail (rendered markdown) |
| `/api/notes` | JSON list (same filters) |
| `/api/notes/:slug` | JSON detail |
