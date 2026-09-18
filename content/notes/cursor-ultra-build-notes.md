---
title: This notes site
slug: cursor-ultra-build-notes
summary: First project on Cursor Ultra — a Cloudflare Workers + D1 notes site that a bot can extend by committing a file or running a CLI.
tags: [cursor-ultra, cloudflare, d1, hono]
created_at: 2026-09-18
updated_at: 2026-09-18
project_url: https://github.com/sanikantv/cursor-ultra-notes
---

The first thing I wanted after buying Cursor Ultra was a place to write down what I build. This repository *is* that place.

## Stack

- **Hono** on Cloudflare Workers, with static assets for CSS
- **D1** (SQLite) for notes — same schema locally via `wrangler d1` and in production
- **Tailwind** for a dark, readable, mobile-friendly layout
- Markdown bodies rendered at request time

## How a new note gets in

Notes are not edited in a CMS. A bot or I can:

1. Commit a file under `content/notes/` and run `npm run notes:sync`
2. Or run `npm run notes:add -- path/to/note.json`

Both upsert into D1 by `slug`. The homepage reads newest first; `/notes/:slug` is the detail page; `?q=` and `?tag=` filter the list.
