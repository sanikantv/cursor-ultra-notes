import assert from "node:assert/strict";
import { test } from "node:test";
import {
  hashPassword,
  timingSafeEqualText,
  verifyPassword,
} from "./password.ts";
import { safeNextPath } from "./auth.ts";

test("password hash verifies and rejects the wrong secret", async () => {
  const hash = await hashPassword("correct-horse-battery");
  assert.match(hash, /^pbkdf2\$sha256\$100000\$/);
  assert.equal(await verifyPassword("correct-horse-battery", hash), true);
  assert.equal(await verifyPassword("wrong-password", hash), false);
});

test("timingSafeEqualText is length-safe", () => {
  assert.equal(timingSafeEqualText("abc", "abc"), true);
  assert.equal(timingSafeEqualText("abc", "abd"), false);
  assert.equal(timingSafeEqualText("abc", "ab"), false);
});

test("safeNextPath blocks open redirects", () => {
  assert.equal(safeNextPath("/notes/hello"), "/notes/hello");
  assert.equal(safeNextPath("//evil.test"), "/");
  assert.equal(safeNextPath("https://evil.test"), "/");
  assert.equal(safeNextPath("/login"), "/");
  assert.equal(safeNextPath("/\\evil"), "/");
});
