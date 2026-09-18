#!/usr/bin/env npx tsx
import { readdirSync } from "node:fs";
import { join } from "node:path";
import { executeSql, parseArgs } from "./d1.ts";
import { readNoteFile, upsertNoteSql } from "./note-file.ts";

const CONTENT_DIR = join(process.cwd(), "content", "notes");

const args = parseArgs(process.argv.slice(2));
const files = readdirSync(CONTENT_DIR)
  .filter((name) => name.endsWith(".md") || name.endsWith(".json"))
  .sort();

if (files.length === 0) {
  console.log(`No note files found in ${CONTENT_DIR}`);
  process.exit(0);
}

const statements = files.map((name) => {
  const note = readNoteFile(join(CONTENT_DIR, name));
  console.log(`- ${name} → ${note.slug}`);
  return upsertNoteSql(note);
});

executeSql(statements.join("\n"), {
  remote: args.remote,
  dbName: args.dbName,
});

console.log(
  `Synced ${files.length} note file(s) into ${args.dbName} (${args.remote ? "remote" : "local"}).`,
);
