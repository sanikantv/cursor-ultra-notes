import type { Child } from "hono/jsx";

type LayoutProps = {
  title: string;
  description?: string;
  owner: string;
  github: string;
  children: Child;
};

export function Layout({
  title,
  description = "A personal log of apps and projects built with Cursor Ultra.",
  owner,
  github,
  children,
}: LayoutProps) {
  const pageTitle =
    title === "Cursor Ultra Build Notes"
      ? title
      : `${title} · Cursor Ultra Build Notes`;

  return (
    <html lang="en">
      <head>
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <meta name="description" content={description} />
        <title>{pageTitle}</title>
        <link rel="icon" href="/favicon.svg" type="image/svg+xml" />
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin="" />
        <link
          href="https://fonts.googleapis.com/css2?family=Figtree:ital,wght@0,400;0,500;0,600;0,700;1,400&family=Instrument+Serif:ital@0;1&display=swap"
          rel="stylesheet"
        />
        <link rel="stylesheet" href="/styles.css" />
      </head>
      <body class="min-h-screen bg-paper text-ink antialiased">
        <div class="pointer-events-none fixed inset-x-0 top-0 z-20 h-1 bg-gradient-to-r from-amber via-copper to-amber" />
        <div class="grain" />
        <div class="relative mx-auto flex min-h-screen w-full max-w-3xl flex-col px-5 pb-16 pt-8 sm:px-8">
          <header class="mb-10 flex flex-wrap items-end justify-between gap-4 border-b border-line pb-6">
            <div>
              <p class="mb-1 text-[0.7rem] font-semibold uppercase tracking-[0.22em] text-amber">
                Field notes
              </p>
              <a
                href="/"
                class="font-display text-3xl text-ink no-underline sm:text-4xl"
              >
                Cursor Ultra Build Notes
              </a>
              <p class="mt-2 max-w-xl text-sm leading-6 text-muted">
                Apps and projects {owner} built with Cursor after buying Cursor
                Ultra on 18 September 2026.
              </p>
            </div>
            <a
              href={`https://github.com/${github}`}
              class="shrink-0 text-sm text-muted no-underline hover:text-amber"
              rel="noreferrer"
              target="_blank"
            >
              @{github}
            </a>
          </header>
          <main class="flex-1">{children}</main>
          <footer class="mt-16 border-t border-line pt-6 text-sm text-muted">
            <p>
              Logged by {owner} ·{" "}
              <a
                href={`https://github.com/${github}`}
                class="text-ink no-underline hover:text-amber"
                rel="noreferrer"
                target="_blank"
              >
                GitHub @{github}
              </a>
            </p>
          </footer>
        </div>
      </body>
    </html>
  );
}
