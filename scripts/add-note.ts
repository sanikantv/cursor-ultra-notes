#!/usr/bin/env npx tsx
import { readFileSync } from "node:fs";
import { executeSql, parseArgs } from "./d1.ts";
import { parseNoteSource, readNoteFile, upsertNoteSql } from "./note-file.ts";

function printUsage(): void {
  console.log(`Add or update a note in D1 (local SQLite by default).

Usage:
  npm run notes:add -- <file.json|file.md>
  npm run notes:add -- --remote <file.json|file.md>
  npm run notes:add -- --stdin < note.json
  npm run notes:add -- --db cursor-ultra-notes content/notes/my-note.md

JSON shape:
  {
    "title": "Built a notes site",
    "slug": "built-a-notes-site",
    "summary": "Optional short summary",
    "body": "# Markdown body",
    "tags": ["cursor-ultra", "cloudflare"],
    "created_at": "2026-09-18",
    "project_url": "https://github.com/sanikantv/cursor-ultra-notes"
  }
`);
  process.exit(1);
}

const args = parseArgs(process.argv.slice(2));
if (!args.stdin && args.positionals.length === 0) {
  printUsage();
  process.exit(1);
}

const note = args.stdin
  ? parseNoteSource(readFileSync(0, "utf8"), "json")
  : readNoteFile(args.positionals[0]);

executeSql(upsertNoteSql(note), { remote: args.remote, dbName: args.dbName });
console.log(
  `Upserted note "${note.title}" (slug: ${note.slug}) into ${args.dbName} (${args.remote ? "remote" : "local"}).`,
);
