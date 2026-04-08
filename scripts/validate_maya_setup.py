#!/usr/bin/env python3
import json
import os
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from integrations.hivemind_adapter import hivemind_reuse_profile
from integrations.ovos_adapter import ovos_reuse_profile
from integrations.vendor_registry import vendor_registry_as_dict
from maya_core.runtime_registry import runtime_summary

home = Path.home()
checks = {
    "bitnet_dir_exists": (home / "bitnet.cpp").exists(),
    "llama_cli_exists": (
        (home / "bitnet.cpp" / "build" / "bin" / "llama-cli").exists()
        or (home / "bitnet.cpp" / "build" / "bin" / "llama-cli.exe").exists()
    ),
    "permissions_file_exists": (home / ".maya_permissions.json").exists(),
    "tasks_file_exists": (home / ".maya_tasks.json").exists(),
    "reminders_file_exists": (home / ".maya_reminders.json").exists(),
}

report = {
    "checks": checks,
    "runtime": runtime_summary(),
    "vendor_registry": vendor_registry_as_dict(),
    "ovos_profile": ovos_reuse_profile(),
    "hivemind_profile": hivemind_reuse_profile(),
}

print(json.dumps(report, indent=2))
if not all(checks.values()):
    raise SystemExit(1)
