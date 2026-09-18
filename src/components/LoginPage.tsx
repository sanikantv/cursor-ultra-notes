type LoginPageProps = {
  error?: string;
  nextPath: string;
  setupNeeded?: boolean;
};

export function LoginPage({ error, nextPath, setupNeeded = false }: LoginPageProps) {
  if (setupNeeded) {
    return (
      <section class="rounded-2xl border border-amber/40 bg-panel p-6 sm:p-8">
        <p class="text-xs font-semibold uppercase tracking-[0.16em] text-amber">
          Setup required
        </p>
        <h1 class="mt-2 font-display text-4xl text-ink">Auth secrets missing</h1>
        <p class="mt-4 max-w-xl text-sm leading-7 text-muted">
          Ultra Notes stays locked until login secrets exist. For local
          development copy <code class="text-ink">.dev.vars.example</code> to{" "}
          <code class="text-ink">.dev.vars</code>. For production:
        </p>
        <pre class="mt-4 overflow-x-auto rounded-xl bg-paper px-4 py-3 text-xs leading-6 text-ink">
          {`npx wrangler pages secret put AUTH_EMAIL --project-name=ultra-notes
npx wrangler pages secret put AUTH_PASSWORD_HASH --project-name=ultra-notes
npx wrangler pages secret put SESSION_SECRET --project-name=ultra-notes`}
        </pre>
        <p class="mt-4 text-sm leading-7 text-muted">
          Hash a password with <code class="text-ink">npm run auth:hash -- 'your password'</code>.
        </p>
      </section>
    );
  }

  return (
    <section class="mx-auto max-w-md rounded-2xl border border-line bg-panel p-6 shadow-card sm:p-8">
      <p class="text-xs font-semibold uppercase tracking-[0.16em] text-amber">
        Owner access
      </p>
      <h1 class="mt-2 font-display text-4xl text-ink">Sign in</h1>
      <p class="mt-3 text-sm leading-7 text-muted">
        This notes log is private. Only the configured owner account can open
        it.
      </p>
      {error ? (
        <p class="mt-4 rounded-xl border border-amber/30 bg-amber/10 px-4 py-3 text-sm text-amber" role="alert">
          {error}
        </p>
      ) : null}
      <form method="post" action="/login" class="mt-6 space-y-4">
        <input type="hidden" name="next" value={nextPath} />
        <div>
          <label class="mb-1 block text-xs font-semibold uppercase tracking-[0.16em] text-muted" for="email">
            Email
          </label>
          <input
            id="email"
            name="email"
            type="email"
            autocomplete="username"
            required
            maxlength={254}
            class="w-full rounded-xl border border-line bg-paper px-4 py-3 text-sm text-ink outline-none placeholder:text-muted/70 focus:border-amber"
          />
        </div>
        <div>
          <label class="mb-1 block text-xs font-semibold uppercase tracking-[0.16em] text-muted" for="password">
            Password
          </label>
          <input
            id="password"
            name="password"
            type="password"
            autocomplete="current-password"
            required
            minlength={8}
            maxlength={128}
            class="w-full rounded-xl border border-line bg-paper px-4 py-3 text-sm text-ink outline-none focus:border-amber"
          />
        </div>
        <button
          type="submit"
          class="w-full rounded-xl bg-amber px-5 py-3 text-sm font-semibold text-paper hover:bg-copper"
        >
          Sign in
        </button>
      </form>
    </section>
  );
}
