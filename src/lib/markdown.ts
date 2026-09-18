import { marked } from "marked";

marked.setOptions({
  gfm: true,
  breaks: false,
});

function sanitizeHtml(html: string): string {
  return html
    .replace(/<script[\s\S]*?>[\s\S]*?<\/script>/gi, "")
    .replace(/\son\w+\s*=\s*("[^"]*"|'[^']*'|[^\s>]+)/gi, "")
    .replace(/\s(href|src)\s*=\s*(['"])\s*javascript:[\s\S]*?\2/gi, " $1=$2#$2");
}

export function renderMarkdown(markdown: string): string {
  const html = marked.parse(markdown ?? "", { async: false }) as string;
  return sanitizeHtml(html);
}
