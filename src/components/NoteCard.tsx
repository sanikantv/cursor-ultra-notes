import type { Note } from "../lib/types";
import { formatNoteDate } from "../lib/format";
import { TagList } from "./TagList";

export function NoteCard({ note }: { note: Note }) {
  return (
    <article class="group rounded-2xl border border-line bg-panel p-5 shadow-card transition hover:border-amber/35 sm:p-6">
      <p class="text-xs font-medium uppercase tracking-[0.16em] text-muted">
        {formatNoteDate(note.created_at)}
      </p>
      <h2 class="mt-2 font-display text-2xl leading-tight text-ink sm:text-[1.7rem]">
        <a href={`/notes/${note.slug}`} class="no-underline hover:text-amber">
          {note.title}
        </a>
      </h2>
      {note.summary ? (
        <p class="mt-3 text-[0.95rem] leading-7 text-muted">{note.summary}</p>
      ) : null}
      <div class="mt-4 flex flex-wrap items-center justify-between gap-3">
        <TagList tags={note.tags} />
        <a
          href={`/notes/${note.slug}`}
          class="text-sm text-amber no-underline group-hover:underline"
        >
          Read note
        </a>
      </div>
    </article>
  );
}
