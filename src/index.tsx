import { Hono } from "hono";
import { HomePage } from "./components/HomePage";
import { Layout } from "./components/Layout";
import { NotFoundPage } from "./components/NotFoundPage";
import { NotePage } from "./components/NotePage";
import { getNoteBySlug, listNotes, listTags } from "./lib/notes";

const app = new Hono<{ Bindings: Env }>();

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

app.get("/", async (c) => {
  const q = c.req.query("q") ?? "";
  const tag = c.req.query("tag") ?? "";
  const { owner, github } = siteMeta(c.env);

  try {
    const [notes, tags] = await Promise.all([
      listNotes(c.env.DB, { q, tag }),
      listTags(c.env.DB),
    ]);

    return c.html(
      <Layout
        title="Cursor Ultra Build Notes"
        description="A personal log of apps and projects Sani Verma built with Cursor Ultra after 18 September 2026."
        owner={owner}
        github={github}
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
      <Layout
        title="Cursor Ultra Build Notes"
        owner={owner}
        github={github}
      >
        <HomePage notes={[]} tags={[]} q={q} tag={tag} setupNeeded />
      </Layout>,
    );
  }
});

app.get("/notes/:slug", async (c) => {
  const { owner, github } = siteMeta(c.env);
  const note = await getNoteBySlug(c.env.DB, c.req.param("slug"));

  if (!note) {
    return c.html(
      <Layout title="Note not found" owner={owner} github={github}>
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
  return c.html(
    <Layout title="Note not found" owner={owner} github={github}>
      <NotFoundPage />
    </Layout>,
    404,
  );
});

export default app;
