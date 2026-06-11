#!/usr/bin/env python3
"""PreToolUse hook: blocks dangerous git commands per docs/rules/03-git-convention.md §3."""

import json
import re
import sys

BLOCKED = [
    (re.compile(r"git\s+push\s+.*--force(?!-with-lease)"), "강제 push 금지 (--force)"),
    (re.compile(r"git\s+push\s+-f\b"), "강제 push 금지 (-f)"),
    (re.compile(r"git\s+push\s+\S+\s+main\b"), "main 브랜치 push 금지"),
    (re.compile(r"git\s+push\s+\S+\s+master\b"), "master 브랜치 push 금지"),
    (re.compile(r"--no-verify\b"), "hook 우회(--no-verify) 금지"),
    (re.compile(r"git\s+config\s"), "git config 변경 금지"),
    (re.compile(r"git\s+reset\s+--hard\b"), "git reset --hard 금지"),
]


def main() -> None:
    try:
        data = json.load(sys.stdin)
    except (json.JSONDecodeError, ValueError):
        sys.exit(0)

    if data.get("tool_name") != "Bash":
        sys.exit(0)

    command = data.get("tool_input", {}).get("command", "")

    for pattern, reason in BLOCKED:
        if pattern.search(command):
            print(
                f"[BLOCKED] {reason} (docs/rules/03-git-convention.md §3)\n명령: {command}"
            )
            sys.exit(2)


if __name__ == "__main__":
    try:
        main()
    except Exception:
        sys.exit(0)
