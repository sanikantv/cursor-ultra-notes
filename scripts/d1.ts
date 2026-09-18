import { mkdtempSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { spawnSync } from "node:child_process";

export const DEFAULT_DB_NAME = "cursor-ultra-notes";

export function parseArgs(argv: string[]) {
  const flags = new Set<string>();
  const options: Record<string, string> = {};
  const positionals: string[] = [];

  for (let i = 0; i < argv.length; i += 1) {
    const arg = argv[i];
    if (arg === "--remote" || arg === "--local" || arg === "--stdin") {
      flags.add(arg);
      continue;
    }
    if (arg === "--db" && argv[i + 1]) {
      options.db = argv[i + 1];
      i += 1;
      continue;
    }
    if (arg.startsWith("--db=")) {
      options.db = arg.slice(5);
      continue;
    }
    positionals.push(arg);
  }

  return {
    remote: flags.has("--remote"),
    stdin: flags.has("--stdin"),
    dbName: options.db || DEFAULT_DB_NAME,
    positionals,
  };
}

export function executeSql(sql: string, opts: { remote: boolean; dbName: string }) {
  const dir = mkdtempSync(join(tmpdir(), "cursor-ultra-notes-"));
  const file = join(dir, "upsert.sql");
  writeFileSync(file, sql, "utf8");

  const args = [
    "wrangler",
    "d1",
    "execute",
    opts.dbName,
    opts.remote ? "--remote" : "--local",
    "--file",
    file,
  ];

  const result = spawnSync("npx", args, {
    stdio: "inherit",
    cwd: process.cwd(),
  });

  if (result.status !== 0) {
    throw new Error(
      `wrangler d1 execute failed with exit code ${result.status ?? "unknown"}`,
    );
  }
}
