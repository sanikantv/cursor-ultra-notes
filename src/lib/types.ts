export type Note = {
  id: number;
  title: string;
  slug: string;
  summary: string;
  body: string;
  tags: string[];
  created_at: string;
  updated_at: string;
  project_url: string | null;
};

export type NoteRow = {
  id: number;
  title: string;
  slug: string;
  summary: string;
  body: string;
  tags: string;
  created_at: string;
  updated_at: string;
  project_url: string | null;
};

export type NoteInput = {
  title: string;
  slug?: string;
  summary?: string;
  body: string;
  tags?: string[];
  created_at?: string;
  updated_at?: string;
  project_url?: string | null;
};
