#!/usr/bin/env node
// PreToolUse hook: blocks Edit/Write on protected paths.

const path = require("path");

function isForbidden(filePath) {
  const norm = "/" + filePath.replace(/\\/g, "/");
  const base = path.basename(norm);

  if (base === ".env" || base.startsWith(".env.")) return [true, ".env 파일"];
  if (base === "docker-compose.yml") return [true, "docker-compose.yml"];
  if (norm.includes("/.claude/")) return [true, ".claude/ 디렉토리"];
  if (norm.includes("/.github/workflows/")) return [true, ".github/workflows/ 디렉토리"];
  if (norm.includes("/docs/rules/")) return [true, "docs/rules/ 디렉토리"];
  if (norm.includes("/frontend/src/shared/api/generated/"))
    return [true, "frontend/src/shared/api/generated/ 디렉토리"];

  return [false, ""];
}

let raw = "";
process.stdin.setEncoding("utf8");
process.stdin.on("data", (chunk) => (raw += chunk));
process.stdin.on("end", () => {
  let data;
  try {
    data = JSON.parse(raw);
  } catch {
    process.exit(0);
  }

  if (!["Edit", "Write"].includes(data.tool_name)) process.exit(0);

  const filePath = data.tool_input?.file_path ?? "";
  if (!filePath) process.exit(0);

  const [blocked, reason] = isForbidden(filePath);
  if (blocked) {
    console.log(
      `[BLOCKED] 수정 금지 경로: ${reason} — ${filePath}\n` +
        "이 경로는 .claude/hooks/check_forbidden_paths.js에 의해 보호됩니다."
    );
    process.exit(2);
  }
  process.exit(0);
});
