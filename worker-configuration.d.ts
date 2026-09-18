// Generated shape for wrangler.jsonc bindings.
// Re-run `npm run cf-typegen` after changing wrangler.jsonc.
// AUTH_* and SESSION_SECRET are Pages secrets, not committed vars.

interface CloudflareBindings {
  DB: D1Database;
  ASSETS?: Fetcher;
  SITE_TITLE: string;
  SITE_OWNER: string;
  SITE_OWNER_GITHUB: string;
  AUTH_EMAIL?: string;
  AUTH_PASSWORD_HASH?: string;
  SESSION_SECRET?: string;
}

interface Env extends CloudflareBindings {}
