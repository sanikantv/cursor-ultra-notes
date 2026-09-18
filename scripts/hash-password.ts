#!/usr/bin/env npx tsx
import { hashPassword } from "../src/lib/password.ts";

const password = process.argv.slice(2).join(" ");
if (!password) {
  console.error("Usage: npm run auth:hash -- 'your-strong-password'");
  process.exit(1);
}

const hash = await hashPassword(password);
console.log(hash);
