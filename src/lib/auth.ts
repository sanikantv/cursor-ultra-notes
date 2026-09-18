import {
  hmacSha256Hex,
  MAX_PASSWORD_LENGTH,
  randomToken,
  sha256Hex,
  timingSafeEqualText,
  verifyPassword,
} from "./password";

export const SESSION_COOKIE = "un_session";
export const SESSION_TTL_SECONDS = 60 * 60 * 24 * 7;
const LOGIN_WINDOW_MS = 15 * 60 * 1000;
const MAX_FAILED_LOGINS = 5;

export type SessionInfo = {
  email: string;
};

type AuthEnv = {
  DB: D1Database;
  AUTH_EMAIL?: string;
  AUTH_PASSWORD_HASH?: string;
  SESSION_SECRET?: string;
};

export function authConfigured(env: AuthEnv): boolean {
  return Boolean(
    env.AUTH_EMAIL?.trim() && env.AUTH_PASSWORD_HASH?.trim() && env.SESSION_SECRET?.trim(),
  );
}

export function isHttps(request: Request): boolean {
  return new URL(request.url).protocol === "https:";
}

export function cookieName(request: Request): string {
  return isHttps(request) ? `__Host-${SESSION_COOKIE}` : SESSION_COOKIE;
}

export function sessionCookieHeader(
  request: Request,
  token: string,
  maxAge = SESSION_TTL_SECONDS,
): string {
  const parts = [
    `${cookieName(request)}=${token}`,
    "Path=/",
    "HttpOnly",
    "SameSite=Strict",
    `Max-Age=${maxAge}`,
  ];
  if (isHttps(request)) parts.push("Secure");
  return parts.join("; ");
}

export function clearSessionCookieHeader(request: Request): string {
  const parts = [
    `${cookieName(request)}=`,
    "Path=/",
    "HttpOnly",
    "SameSite=Strict",
    "Max-Age=0",
  ];
  if (isHttps(request)) parts.push("Secure");
  return parts.join("; ");
}

export function readCookie(request: Request, name: string): string | null {
  const header = request.headers.get("Cookie");
  if (!header) return null;
  for (const part of header.split(";")) {
    const [rawName, ...rest] = part.trim().split("=");
    if (rawName === name) return rest.join("=");
  }
  return null;
}

export function sameOriginPost(request: Request): boolean {
  const url = new URL(request.url);
  const origin = request.headers.get("Origin");
  if (origin) return origin === url.origin;

  const referer = request.headers.get("Referer");
  if (!referer) return false;
  try {
    return new URL(referer).origin === url.origin;
  } catch {
    return false;
  }
}

export function safeNextPath(raw: string | null | undefined): string {
  if (!raw) return "/";
  let value = raw.trim();
  try {
    value = decodeURIComponent(value);
  } catch {
    return "/";
  }
  if (!value.startsWith("/") || value.startsWith("//") || value.startsWith("/\\")) {
    return "/";
  }
  if (value.startsWith("/login") || value.includes("\\") || value.includes("@")) {
    return "/";
  }
  return value;
}

export function clientIp(request: Request): string {
  return request.headers.get("CF-Connecting-IP") || request.headers.get("X-Forwarded-For") || "local";
}

async function attemptKey(env: AuthEnv, request: Request): Promise<string> {
  const secret = env.SESSION_SECRET || "missing-session-secret";
  return hmacSha256Hex(secret, `login:${clientIp(request)}`);
}

export async function loginLocked(env: AuthEnv, request: Request): Promise<boolean> {
  const key = await attemptKey(env, request);
  const since = Date.now() - LOGIN_WINDOW_MS;
  const row = await env.DB.prepare(
    "SELECT COUNT(*) AS n FROM login_attempts WHERE key = ? AND success = 0 AND attempted_at > ?",
  )
    .bind(key, since)
    .first<{ n: number }>();
  return Number(row?.n ?? 0) >= MAX_FAILED_LOGINS;
}

export async function recordLoginAttempt(
  env: AuthEnv,
  request: Request,
  success: boolean,
): Promise<void> {
  const key = await attemptKey(env, request);
  await env.DB.prepare(
    "INSERT INTO login_attempts (key, attempted_at, success) VALUES (?, ?, ?)",
  )
    .bind(key, Date.now(), success ? 1 : 0)
    .run();
  await env.DB.prepare("DELETE FROM login_attempts WHERE attempted_at < ?")
    .bind(Date.now() - LOGIN_WINDOW_MS * 4)
    .run();
}

export async function authenticate(
  env: AuthEnv,
  email: string,
  password: string,
): Promise<boolean> {
  const expectedEmail = (env.AUTH_EMAIL || "").trim().toLowerCase();
  const storedHash = env.AUTH_PASSWORD_HASH || "";
  const submittedEmail = email.trim().toLowerCase();
  const emailOk = expectedEmail.length > 0 && timingSafeEqualText(submittedEmail, expectedEmail);
  const passwordOk =
    password.length > 0 &&
    password.length <= MAX_PASSWORD_LENGTH &&
    (await verifyPassword(password, storedHash));
  return emailOk && passwordOk;
}

export async function createSession(env: AuthEnv, email: string): Promise<string> {
  const token = randomToken(32);
  const tokenHash = await sha256Hex(token);
  const now = Date.now();
  await env.DB.prepare(
    `INSERT INTO sessions (token_hash, email, created_at, expires_at, last_seen_at)
     VALUES (?, ?, ?, ?, ?)`,
  )
    .bind(tokenHash, email.trim().toLowerCase(), now, now + SESSION_TTL_SECONDS * 1000, now)
    .run();
  await env.DB.prepare("DELETE FROM sessions WHERE expires_at < ?").bind(now).run();
  return token;
}

export async function getSession(env: AuthEnv, request: Request): Promise<SessionInfo | null> {
  const token = readCookie(request, cookieName(request));
  if (!token) return null;
  const tokenHash = await sha256Hex(token);
  const now = Date.now();
  const row = await env.DB.prepare(
    `SELECT email, expires_at FROM sessions WHERE token_hash = ? LIMIT 1`,
  )
    .bind(tokenHash)
    .first<{ email: string; expires_at: number }>();
  if (!row || row.expires_at <= now) {
    if (row) {
      await env.DB.prepare("DELETE FROM sessions WHERE token_hash = ?").bind(tokenHash).run();
    }
    return null;
  }
  await env.DB.prepare("UPDATE sessions SET last_seen_at = ? WHERE token_hash = ?")
    .bind(now, tokenHash)
    .run();
  return { email: row.email };
}

export async function destroySession(env: AuthEnv, request: Request): Promise<void> {
  const token = readCookie(request, cookieName(request));
  if (!token) return;
  const tokenHash = await sha256Hex(token);
  await env.DB.prepare("DELETE FROM sessions WHERE token_hash = ?").bind(tokenHash).run();
}
