type TagListProps = {
  tags: string[];
  active?: string;
  hrefFor?: (tag: string) => string;
};

export function TagList({ tags, active, hrefFor }: TagListProps) {
  if (tags.length === 0) return null;

  return (
    <ul class="flex flex-wrap gap-2">
      {tags.map((tag) => {
        const isActive = active === tag;
        const href = hrefFor ? hrefFor(tag) : `/?tag=${encodeURIComponent(tag)}`;
        return (
          <li>
            <a
              href={href}
              class={`rounded-full border px-3 py-1 text-xs tracking-wide no-underline ${
                isActive
                  ? "border-amber bg-amber/15 text-amber"
                  : "border-line text-muted hover:border-amber/50 hover:text-ink"
              }`}
            >
              {tag}
            </a>
          </li>
        );
      })}
    </ul>
  );
}
