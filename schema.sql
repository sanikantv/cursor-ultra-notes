-- Canonical SQLite / Cloudflare D1 schema for Cursor Ultra Build Notes.
-- Applied in production and locally via wrangler migrations
-- (see migrations/0001_create_notes.sql). This file is the human-readable
-- reference copy of that schema.

CREATE TABLE IF NOT EXISTS notes (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  title TEXT NOT NULL,
  slug TEXT NOT NULL UNIQUE,
  summary TEXT NOT NULL DEFAULT '',
  body TEXT NOT NULL,
  tags TEXT NOT NULL DEFAULT '[]',
  created_at TEXT NOT NULL DEFAULT (datetime('now')),
  updated_at TEXT NOT NULL DEFAULT (datetime('now')),
  project_url TEXT
);

CREATE INDEX IF NOT EXISTS idx_notes_created_at ON notes (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notes_slug ON notes (slug);
