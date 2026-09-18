import { Hono } from "hono";
import { HomePage } from "./components/HomePage";
import { Layout } from "./components/Layout";
import { LoginPage } from "./components/LoginPage";
import { NotFoundPage } from "./components/NotFoundPage";
import { NotePage } from "./components/NotePage";
import {
  authConfigured,
  authenticate,
  clearSessionCookieHeader,
  createSession,
  destroySession,
  getSession,
  loginLocked,
  recordLoginAttempt,
  safeNextPath,
  sameOriginPost,
  sessionCookieHeader,
  type SessionInfo,
} from "./lib/auth";
import { MAX_PASSWORD_LENGTH } from "./lib/password";
import { getNoteBySlug, listNotes, listTags } from "./lib/notes";
import { baseSecurityHeaders, noStoreAndRobots } from "./lib/security-headers";

type AppEnv = {
  Bindings: Env;
  Variables: {
    session?: SessionInfo;
  };
};

const app = new Hono<AppEnv>();

function siteMeta(env: Env) {
  return {
    owner: env.SITE_OWNER || "Sani Verma",
    github: env.SITE_OWNER_GITHUB || "sanikantv",
  };
}

function isMissingTable(error: unknown): boolean {
  const message = error instanceof Error ? error.message : String(error);
  return /no such table/i.test(message);
}

app.use("*", baseSecurityHeaders);
app.use("*", noStoreAndRobots);

app.use("*", async (c, next) => {
  const path = new URL(c.req.url).pathname;
  if (path === "/login" || path === "/logout") {
    return next();
  }

  try {
    const session = await getSession(c.env, c.req.raw);
    if (session) {
      c.set("session", session);
      return next();
    }
  } catch (error) {
    if (!isMissingTable(error)) throw error;
  }

  if (path.startsWith("/api/")) {
    return c.json({ error: "Unauthorized" }, 401);
  }

  const nextPath = safeNextPath(`${new URL(c.req.url).pathname}${new URL(c.req.url).search}`);
  return c.redirect(`/login?next=${encodeURIComponent(nextPath)}`);
});

app.get("/login", async (c) => {
  const { owner, github } = siteMeta(c.env);
  const existing = await getSession(c.env, c.req.raw).catch(() => null);
  const nextPath = safeNextPath(c.req.query("next"));
  if (existing) return c.redirect(nextPath);

  return c.html(
    <Layout title="Sign in" owner={owner} github={github}>
      <LoginPage
        nextPath={nextPath}
        setupNeeded={!authConfigured(c.env)}
      />
    </Layout>,
  );
});

app.post("/login", async (c) => {
  const { owner, github } = siteMeta(c.env);
  const form = await c.req.parseBody();
  const nextPath = safeNextPath(typeof form.next === "string" ? form.next : "/");
  const email = typeof form.email === "string" ? form.email : "";
  const password = typeof form.password === "string" ? form.password : "";

  const fail = (error: string, status: 401 | 403 | 429 | 503 = 401) =>
    c.html(
      <Layout title="Sign in" owner={owner} github={github}>
        <LoginPage
          error={error}
          nextPath={nextPath}
          setupNeeded={!authConfigured(c.env)}
        />
      </Layout>,
      status,
    );

  if (!sameOriginPost(c.req.raw)) {
    return fail("Sign-in was blocked. Try again from this site.", 403);
  }
  if (!authConfigured(c.env)) {
    return fail("Login is not configured yet.", 503);
  }
  if (password.length > MAX_PASSWORD_LENGTH) {
    return fail("Email or password is wrong.");
  }

  try {
    if (await loginLocked(c.env, c.req.raw)) {
      return fail("Too many attempts. Try again in 15 minutes.", 429);
    }

    const ok = await authenticate(c.env, email, password);
    await recordLoginAttempt(c.env, c.req.raw, ok);
    if (!ok) {
      return fail("Email or password is wrong.");
    }

    const token = await createSession(c.env, email);
    c.header("Set-Cookie", sessionCookieHeader(c.req.raw, token));
    return c.redirect(nextPath);
  } catch (error) {
    if (isMissingTable(error)) {
      return fail("Database is missing auth tables. Run npm run db:setup.", 503);
    }
    throw error;
  }
});

app.post("/logout", async (c) => {
  if (!sameOriginPost(c.req.raw)) {
    return c.redirect("/login");
  }
  try {
    await destroySession(c.env, c.req.raw);
  } catch (error) {
    if (!isMissingTable(error)) throw error;
  }
  c.header("Set-Cookie", clearSessionCookieHeader(c.req.raw));
  return c.redirect("/login");
});

app.get("/", async (c) => {
  const q = c.req.query("q") ?? "";
  const tag = c.req.query("tag") ?? "";
  const { owner, github } = siteMeta(c.env);
  const email = c.get("session")?.email;

  try {
    const [notes, tags] = await Promise.all([
      listNotes(c.env.DB, { q, tag }),
      listTags(c.env.DB),
    ]);

    return c.html(
      <Layout
        title="Ultra Notes"
        description="Private log of apps and projects Sani Verma built with Cursor Ultra after 18 September 2026."
        owner={owner}
        github={github}
        email={email}
      >
        <HomePage
          notes={notes}
          tags={tags}
          q={q}
          tag={tag}
          setupNeeded={notes.length === 0 && !q && !tag}
        />
      </Layout>,
    );
  } catch (error) {
    if (!isMissingTable(error)) throw error;
    return c.html(
      <Layout title="Ultra Notes" owner={owner} github={github} email={email}>
        <HomePage notes={[]} tags={[]} q={q} tag={tag} setupNeeded />
      </Layout>,
    );
  }
});

app.get("/notes/:slug", async (c) => {
  const { owner, github } = siteMeta(c.env);
  const email = c.get("session")?.email;
  const note = await getNoteBySlug(c.env.DB, c.req.param("slug"));

  if (!note) {
    return c.html(
      <Layout title="Note not found" owner={owner} github={github} email={email}>
        <NotFoundPage />
      </Layout>,
      404,
    );
  }

  return c.html(
    <Layout
      title={note.title}
      description={note.summary || note.title}
      owner={owner}
      github={github}
      email={email}
    >
      <NotePage note={note} />
    </Layout>,
  );
});

app.get("/api/notes", async (c) => {
  const q = c.req.query("q") ?? "";
  const tag = c.req.query("tag") ?? "";
  const notes = await listNotes(c.env.DB, { q, tag });
  return c.json({ notes });
});

app.get("/api/notes/:slug", async (c) => {
  const note = await getNoteBySlug(c.env.DB, c.req.param("slug"));
  if (!note) return c.json({ error: "Note not found" }, 404);
  return c.json({ note });
});

app.notFound((c) => {
  const { owner, github } = siteMeta(c.env);
  const email = c.get("session")?.email;
  if (!email && !new URL(c.req.url).pathname.startsWith("/api/")) {
    return c.redirect("/login");
  }
  return c.html(
    <Layout title="Note not found" owner={owner} github={github} email={email}>
      <NotFoundPage />
    </Layout>,
    404,
  );
});

export default app;
