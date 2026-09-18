import { readFileSync } from "node:fs";
import { slugify } from "../src/lib/slug.ts";
import type { NoteInput } from "../src/lib/types.ts";

export type ParsedNote = Required<
  Pick<NoteInput, "title" | "slug" | "summary" | "body" | "tags">
> & {
  created_at: string;
  updated_at: string;
  project_url: string | null;
};

function todayIsoDate(): string {
  return new Date().toISOString().slice(0, 10);
}

function asString(value: unknown): string | undefined {
  if (typeof value === "string") return value;
  if (typeof value === "number") return String(value);
  return undefined;
}

function asTags(value: unknown): string[] {
  if (Array.isArray(value)) {
    return value.map((tag) => String(tag).trim()).filter(Boolean);
  }
  if (typeof value === "string") {
    return value
      .split(",")
      .map((tag) => tag.trim())
      .filter(Boolean);
  }
  return [];
}

function parseFrontmatter(raw: string): { data: Record<string, unknown>; body: string } {
  const match = raw.match(/^---\r?\n([\s\S]*?)\r?\n---\r?\n?([\s\S]*)$/);
  if (!match) return { data: {}, body: raw.trim() };

  const data: Record<string, unknown> = {};
  const lines = match[1].split(/\r?\n/);
  let currentKey: string | null = null;

  for (const line of lines) {
    const listItem = line.match(/^\s+-\s+(.+)$/);
    if (listItem && currentKey) {
      const existing = data[currentKey];
      data[currentKey] = [
        ...(Array.isArray(existing) ? existing : []),
        listItem[1].trim(),
      ];
      continue;
    }

    const kv = line.match(/^([A-Za-z0-9_]+):\s*(.*)$/);
    if (!kv) continue;
    currentKey = kv[1];
    const rawValue = kv[2].trim();
    if (rawValue === "") {
      data[currentKey] = [];
      continue;
    }
    if (rawValue.startsWith("[") && rawValue.endsWith("]")) {
      data[currentKey] = rawValue
        .slice(1, -1)
        .split(",")
        .map((part) => part.trim().replace(/^['"]|['"]$/g, ""))
        .filter(Boolean);
      continue;
    }
    data[currentKey] = rawValue.replace(/^['"]|['"]$/g, "");
  }

  return { data, body: match[2].trim() };
}

export function normalizeNote(input: NoteInput): ParsedNote {
  const title = input.title?.trim();
  if (!title) throw new Error("Note is missing a title");
  const body = input.body?.trim();
  if (!body) throw new Error("Note is missing a markdown body");

  const created = input.created_at?.trim() || todayIsoDate();
  return {
    title,
    slug: (input.slug?.trim() || slugify(title)).toLowerCase(),
    summary: input.summary?.trim() || "",
    body,
    tags: (input.tags ?? []).map((tag) => tag.trim()).filter(Boolean),
    created_at: created,
    updated_at: input.updated_at?.trim() || created,
    project_url: input.project_url?.trim() || null,
  };
}

export function parseNoteSource(raw: string, hint = "json"): ParsedNote {
  const trimmed = raw.trim();
  if (hint === "md" || trimmed.startsWith("---")) {
    const { data, body } = parseFrontmatter(trimmed);
    return normalizeNote({
      title: asString(data.title) ?? "",
      slug: asString(data.slug),
      summary: asString(data.summary),
      body: asString(data.body) || body,
      tags: asTags(data.tags),
      created_at: asString(data.created_at) ?? asString(data.date),
      updated_at: asString(data.updated_at),
      project_url: asString(data.project_url) ?? null,
    });
  }

  const parsed = JSON.parse(trimmed) as NoteInput;
  return normalizeNote(parsed);
}

export function readNoteFile(filePath: string): ParsedNote {
  const raw = readFileSync(filePath, "utf8");
  const hint = filePath.endsWith(".md") ? "md" : "json";
  return parseNoteSource(raw, hint);
}

export function sqlString(value: string | null): string {
  if (value === null) return "NULL";
  return `'${value.replace(/'/g, "''")}'`;
}

export function upsertNoteSql(note: ParsedNote): string {
  return `INSERT INTO notes (title, slug, summary, body, tags, created_at, updated_at, project_url)
VALUES (
  ${sqlString(note.title)},
  ${sqlString(note.slug)},
  ${sqlString(note.summary)},
  ${sqlString(note.body)},
  ${sqlString(JSON.stringify(note.tags))},
  ${sqlString(note.created_at)},
  ${sqlString(note.updated_at)},
  ${sqlString(note.project_url)}
)
ON CONFLICT(slug) DO UPDATE SET
  title = excluded.title,
  summary = excluded.summary,
  body = excluded.body,
  tags = excluded.tags,
  created_at = excluded.created_at,
  updated_at = excluded.updated_at,
  project_url = excluded.project_url;
`;
}
