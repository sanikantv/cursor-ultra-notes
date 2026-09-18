// Generated shape for wrangler.toml bindings.
// Re-run `npm run cf-typegen` after changing wrangler.toml.

interface CloudflareBindings {
  DB: D1Database;
  ASSETS: Fetcher;
  SITE_TITLE: string;
  SITE_OWNER: string;
  SITE_OWNER_GITHUB: string;
}

interface Env extends CloudflareBindings {}
