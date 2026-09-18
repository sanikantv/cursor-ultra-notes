type SearchFormProps = {
  q: string;
  tag: string;
};

export function SearchForm({ q, tag }: SearchFormProps) {
  return (
    <form
      action="/"
      method="get"
      class="flex flex-col gap-3 sm:flex-row sm:items-center"
    >
      <label class="sr-only" for="q">
        Search notes
      </label>
      <input
        id="q"
        name="q"
        type="search"
        value={q}
        placeholder="Search titles, summaries, or body…"
        class="w-full rounded-xl border border-line bg-panel px-4 py-3 text-sm text-ink outline-none placeholder:text-muted/70 focus:border-amber"
      />
      {tag ? <input type="hidden" name="tag" value={tag} /> : null}
      <button
        type="submit"
        class="rounded-xl bg-amber px-5 py-3 text-sm font-semibold text-paper hover:bg-copper"
      >
        Search
      </button>
    </form>
  );
}
