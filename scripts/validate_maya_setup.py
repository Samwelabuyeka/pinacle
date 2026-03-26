#!/usr/bin/env python3
import json
import os
from pathlib import Path

home = Path.home()
checks = {
    "bitnet_dir_exists": (home / "bitnet.cpp").exists(),
    "llama_cli_exists": (home / "bitnet.cpp" / "build/bin/llama-cli").exists(),
    "permissions_file_exists": (home / ".maya_permissions.json").exists(),
    "tasks_file_exists": (home / ".maya_tasks.json").exists(),
    "reminders_file_exists": (home / ".maya_reminders.json").exists(),
}

print(json.dumps(checks, indent=2))
if not all(checks.values()):
    raise SystemExit(1)
