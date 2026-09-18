export function NotFoundPage() {
  return (
    <section class="rounded-2xl border border-line bg-panel p-8">
      <p class="text-xs font-semibold uppercase tracking-[0.16em] text-amber">
        404
      </p>
      <h1 class="mt-2 font-display text-4xl text-ink">Note not found</h1>
      <p class="mt-3 max-w-md text-sm leading-7 text-muted">
        That slug is not in the log. It may have been renamed, or the local
        database still needs seeding.
      </p>
      <p class="mt-6">
        <a href="/" class="text-amber no-underline hover:underline">
          ← Back to all notes
        </a>
      </p>
    </section>
  );
}
