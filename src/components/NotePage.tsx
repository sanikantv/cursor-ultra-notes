import type { Note } from "../lib/types";
import { formatNoteDate } from "../lib/format";
import { renderMarkdown } from "../lib/markdown";
import { TagList } from "./TagList";

export function NotePage({ note }: { note: Note }) {
  const html = renderMarkdown(note.body);

  return (
    <article>
      <p class="mb-6">
        <a href="/" class="text-sm text-muted no-underline hover:text-amber">
          ← All notes
        </a>
      </p>
      <p class="text-xs font-medium uppercase tracking-[0.16em] text-muted">
        {formatNoteDate(note.created_at)}
      </p>
      <h1 class="mt-2 font-display text-4xl leading-tight text-ink sm:text-5xl">
        {note.title}
      </h1>
      {note.summary ? (
        <p class="mt-4 text-lg leading-8 text-muted">{note.summary}</p>
      ) : null}
      <div class="mt-5 flex flex-wrap items-center gap-4">
        <TagList tags={note.tags} />
        {note.project_url ? (
          <a
            href={note.project_url}
            class="text-sm text-amber no-underline hover:underline"
            rel="noreferrer"
            target="_blank"
          >
            Project link
          </a>
        ) : null}
      </div>
      <div
        class="prose prose-invert prose-notes mt-10 max-w-none"
        dangerouslySetInnerHTML={{ __html: html }}
      />
    </article>
  );
}
