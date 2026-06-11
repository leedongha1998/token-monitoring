#!/usr/bin/env python3
"""PreToolUse hook: blocks Edit/Write on protected paths."""

import json
import os
import sys


def is_forbidden(path: str) -> tuple[bool, str]:
    norm = "/" + path.replace("\\", "/")
    base = os.path.basename(norm)

    if base == ".env" or base.startswith(".env."):
        return True, ".env 파일"
    if base == "docker-compose.yml":
        return True, "docker-compose.yml"
    if "/.claude/" in norm:
        return True, ".claude/ 디렉토리"
    if "/.github/workflows/" in norm:
        return True, ".github/workflows/ 디렉토리"
    if "/docs/rules/" in norm:
        return True, "docs/rules/ 디렉토리"
    if "/frontend/src/shared/api/generated/" in norm:
        return True, "frontend/src/shared/api/generated/ 디렉토리"

    return False, ""


def main() -> None:
    try:
        data = json.load(sys.stdin)
    except (json.JSONDecodeError, ValueError):
        sys.exit(0)

    if data.get("tool_name") not in ("Edit", "Write"):
        sys.exit(0)

    file_path = data.get("tool_input", {}).get("file_path", "")
    if not file_path:
        sys.exit(0)

    blocked, reason = is_forbidden(file_path)
    if blocked:
        print(
            f"[BLOCKED] 수정 금지 경로: {reason} — {file_path}\n"
            "이 경로는 .claude/hooks/check_forbidden_paths.py에 의해 보호됩니다."
        )
        sys.exit(2)


if __name__ == "__main__":
    try:
        main()
    except Exception:
        sys.exit(0)
