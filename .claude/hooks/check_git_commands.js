#!/usr/bin/env node
// PreToolUse hook: blocks dangerous git commands per docs/rules/03-git-convention.md §3.

const BLOCKED = [
  [/git\s+push\s+.*--force(?!-with-lease)/, "강제 push 금지 (--force)"],
  [/git\s+push\s+-f\b/, "강제 push 금지 (-f)"],
  [/git\s+push\s+\S+\s+main\b/, "main 브랜치 push 금지"],
  [/git\s+push\s+\S+\s+master\b/, "master 브랜치 push 금지"],
  [/--no-verify\b/, "hook 우회(--no-verify) 금지"],
  [/git\s+config\s/, "git config 변경 금지"],
  [/git\s+reset\s+--hard\b/, "git reset --hard 금지"],
];

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

  if (data.tool_name !== "Bash") process.exit(0);

  const command = data.tool_input?.command ?? "";

  for (const [pattern, reason] of BLOCKED) {
    if (pattern.test(command)) {
      console.log(
        `[BLOCKED] ${reason} (docs/rules/03-git-convention.md §3)\n명령: ${command}`
      );
      process.exit(2);
    }
  }
  process.exit(0);
});
