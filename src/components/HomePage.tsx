import type { Note } from "../lib/types";
import { NoteCard } from "./NoteCard";
import { SearchForm } from "./SearchForm";
import { TagList } from "./TagList";

type HomePageProps = {
  notes: Note[];
  tags: string[];
  q: string;
  tag: string;
  setupNeeded?: boolean;
};

export function HomePage({
  notes,
  tags,
  q,
  tag,
  setupNeeded = false,
}: HomePageProps) {
  const filtering = Boolean(q || tag);

  return (
    <div class="space-y-8">
      <SearchForm q={q} tag={tag} />

      {tags.length > 0 ? (
        <div class="space-y-2">
          <p class="text-xs font-semibold uppercase tracking-[0.16em] text-muted">
            Filter by tag
          </p>
          <TagList
            tags={tags}
            active={tag}
            hrefFor={(value) =>
              value === tag
                ? q
                  ? `/?q=${encodeURIComponent(q)}`
                  : "/"
                : `/?tag=${encodeURIComponent(value)}${q ? `&q=${encodeURIComponent(q)}` : ""}`
            }
          />
        </div>
      ) : null}

      {filtering ? (
        <p class="text-sm text-muted">
          {notes.length} {notes.length === 1 ? "note" : "notes"}
          {q ? ` matching “${q}”` : ""}
          {tag ? ` tagged ${tag}` : ""}.{" "}
          <a href="/" class="text-amber no-underline hover:underline">
            Clear filters
          </a>
        </p>
      ) : null}

      {setupNeeded ? (
        <section class="rounded-2xl border border-dashed border-amber/40 bg-panel p-6 text-sm leading-7 text-muted">
          <h2 class="font-display text-2xl text-ink">Database is empty</h2>
          <p class="mt-3">
            The notes table is missing or has no rows. From the project root
            run:
          </p>
          <pre class="mt-3 overflow-x-auto rounded-xl bg-paper px-4 py-3 text-ink">
            npm run db:setup
          </pre>
        </section>
      ) : notes.length === 0 ? (
        <section class="rounded-2xl border border-line bg-panel p-6 text-muted">
          <h2 class="font-display text-2xl text-ink">No matching notes</h2>
          <p class="mt-2 text-sm leading-7">
            Nothing in the log matches this search. Try another word or{" "}
            <a href="/" class="text-amber">
              view all notes
            </a>
            .
          </p>
        </section>
      ) : (
        <div class="space-y-4">
          {notes.map((note) => (
            <NoteCard note={note} />
          ))}
        </div>
      )}
    </div>
  );
}
