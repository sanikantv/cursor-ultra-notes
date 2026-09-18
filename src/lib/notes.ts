import type { Note, NoteRow } from "./types";

function parseTags(raw: string | null | undefined): string[] {
  if (!raw) return [];
  try {
    const parsed = JSON.parse(raw) as unknown;
    if (Array.isArray(parsed)) {
      return parsed
        .map((tag) => String(tag).trim())
        .filter((tag) => tag.length > 0);
    }
  } catch {
    // fall through to comma-separated
  }
  return raw
    .split(",")
    .map((tag) => tag.trim())
    .filter((tag) => tag.length > 0);
}

export function rowToNote(row: NoteRow): Note {
  return {
    ...row,
    tags: parseTags(row.tags),
    project_url: row.project_url || null,
  };
}

function likeNeedle(value: string): string {
  return `%${value.replace(/[%_]/g, "")}%`;
}

export async function listNotes(
  db: D1Database,
  filters: { q?: string; tag?: string } = {},
): Promise<Note[]> {
  const q = filters.q?.trim() ?? "";
  const tag = filters.tag?.trim() ?? "";
  const conditions: string[] = [];
  const params: string[] = [];

  if (q) {
    conditions.push(
      "(title LIKE ? OR summary LIKE ? OR body LIKE ? OR tags LIKE ?)",
    );
    const needle = likeNeedle(q);
    params.push(needle, needle, needle, needle);
  }

  if (tag) {
    conditions.push(
      "EXISTS (SELECT 1 FROM json_each(notes.tags) WHERE json_each.value = ?)",
    );
    params.push(tag);
  }

  const where = conditions.length > 0 ? `WHERE ${conditions.join(" AND ")}` : "";
  const sql = `SELECT * FROM notes ${where} ORDER BY datetime(created_at) DESC, id DESC`;
  const statement = db.prepare(sql);
  const result = await (params.length > 0
    ? statement.bind(...params)
    : statement
  ).all<NoteRow>();

  return (result.results ?? []).map(rowToNote);
}

export async function getNoteBySlug(
  db: D1Database,
  slug: string,
): Promise<Note | null> {
  const row = await db
    .prepare("SELECT * FROM notes WHERE slug = ? LIMIT 1")
    .bind(slug)
    .first<NoteRow>();
  return row ? rowToNote(row) : null;
}

export async function listTags(db: D1Database): Promise<string[]> {
  const result = await db
    .prepare(
      `SELECT DISTINCT json_each.value AS tag
       FROM notes, json_each(notes.tags)
       WHERE json_each.value IS NOT NULL AND json_each.value != ''
       ORDER BY tag COLLATE NOCASE`,
    )
    .all<{ tag: string }>();

  return (result.results ?? []).map((row) => row.tag);
}
