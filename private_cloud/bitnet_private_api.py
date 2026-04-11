#!/usr/bin/env python3
import json
import os
import subprocess
import sys
from http.server import BaseHTTPRequestHandler, HTTPServer
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from maya_core.personality import PersonalityEngine
from maya_core.privacy_guard import can_access_path, search_files
from maya_core.capabilities import detect_capabilities
from maya_core.os_bridge import OSBridge
from maya_core.context_engine import ContextEngine
from maya_core.fallback_llm import generate_fallback

HOST = os.environ.get("BITNET_CLOUD_HOST", "127.0.0.1")
PORT = int(os.environ.get("BITNET_CLOUD_PORT", "8080"))
API_KEY = os.environ.get("BITNET_CLOUD_API_KEY", "")
BITNET_DIR = os.environ.get("BITNET_DIR", os.path.expanduser("~/bitnet.cpp"))
MODEL_PATH = os.environ.get(
    "BITNET_MODEL",
    os.path.join(BITNET_DIR, "models/BitNet-b1.58-2B-4T/ggml-model-i2_s.gguf"),
)
PERM_FILE = Path(os.environ.get("MAYA_PERMISSIONS_FILE", os.path.expanduser("~/.maya_permissions.json")))
TASK_FILE = Path(os.environ.get("MAYA_TASK_FILE", os.path.expanduser("~/.maya_tasks.json")))
REMINDER_FILE = Path(os.environ.get("MAYA_REMINDER_FILE", os.path.expanduser("~/.maya_reminders.json")))
NOTIF_FILE = Path(os.environ.get("MAYA_NOTIFICATION_FILE", os.path.expanduser("~/.maya_notifications.json")))

DEFAULT_PERMISSIONS = {
    "ai_chat": True,
    "send_sms": False,
    "make_calls": False,
    "manage_calendar": False,
    "location_access": False,
    "run_when_phone_off": False,
    "device_search": False,
    "always_mic": True,
    "always_speaker": True,
    "os_level_control": False,
    "notification_read": False,
}

PERSONA = PersonalityEngine()
OSB = OSBridge()
CTX = ContextEngine()

REF_FILE = ROOT / "config" / "siri_capabilities_reference.json"
if REF_FILE.exists():
    SIRI_CAPABILITY_CATALOG = json.loads(REF_FILE.read_text()).get("capabilities", [])
else:
    SIRI_CAPABILITY_CATALOG = []

MATRIX_FILE = ROOT / "config" / "maya_capability_matrix.json"
if MATRIX_FILE.exists():
    MAYA_CAPABILITY_MATRIX = json.loads(MATRIX_FILE.read_text()).get("capabilities", [])
else:
    MAYA_CAPABILITY_MATRIX = []


def read_json(path: Path, default):
    if not path.exists():
        return default
    try:
        return json.loads(path.read_text())
    except Exception:
        return default


def write_json(path: Path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2))


def get_permissions():
    perms = read_json(PERM_FILE, DEFAULT_PERMISSIONS.copy())
    for key, value in DEFAULT_PERMISSIONS.items():
        perms.setdefault(key, value)
    return perms


class Handler(BaseHTTPRequestHandler):
    def _send(self, code: int, payload: dict):
        body = json.dumps(payload).encode("utf-8")
        self.send_response(code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def _auth(self):
        if API_KEY and self.headers.get("Authorization") != f"Bearer {API_KEY}":
            self._send(401, {"error": "unauthorized"})
            return False
        return True

    def do_GET(self):
        if not self._auth():
            return
        # Handle root path
        if self.path == "/":
            return self._send(200, {
                "message": "Maya BitNet Private API",
                "endpoints": [
                    "GET /health", "GET /capabilities", "GET /permissions",
                    "GET /tasks", "GET /reminders", "GET /notifications",
                    "GET /suggestions", "GET /context", "GET /siri_capabilities",
                    "GET /capability_matrix",
                    "POST /generate", "POST /permissions", "POST /events",
                    "POST /tasks", "POST /reminders", "POST /notifications",
                    "POST /os_action", "POST /device_search"
                ]
            })
        if self.path == "/permissions":
            return self._send(200, {"permissions": get_permissions()})
        if self.path == "/tasks":
            return self._send(200, {"tasks": read_json(TASK_FILE, [])})
        if self.path == "/reminders":
            return self._send(200, {"reminders": read_json(REMINDER_FILE, [])})
        if self.path == "/suggestions":
            return self._send(200, {"suggestions": PERSONA.suggestions()})
        if self.path == "/capabilities":
            return self._send(200, {"capabilities": detect_capabilities(get_permissions())})
        if self.path == "/siri_capabilities":
            return self._send(200, {"catalog": SIRI_CAPABILITY_CATALOG})
        if self.path == "/notifications":
            return self._send(200, {"notifications": read_json(NOTIF_FILE, [])})
        if self.path == "/capability_matrix":
            return self._send(200, {"capabilities": MAYA_CAPABILITY_MATRIX})
        if self.path == "/context":
            return self._send(200, {"recent": CTX.recent_summary()})
        if self.path == "/health":
            health = {
                "bitnet_dir": os.path.exists(BITNET_DIR),
                "llama_cli": os.path.exists(os.path.join(BITNET_DIR, "build/bin/llama-cli")),
                "permissions": PERM_FILE.exists(),
            }
            return self._send(200, health)
        return self._send(404, {"error": "not_found", "available_endpoints": ["GET /", "GET /health", "GET /capabilities"]})

    def do_POST(self):
        if not self._auth():
            return

        length = int(self.headers.get("Content-Length", "0"))
        data = json.loads(self.rfile.read(length) or b"{}")[

        if self.path == "/permissions":
            current = get_permissions()
            for key in DEFAULT_PERMISSIONS:
                if key in data and isinstance(data[key], bool):
                    current[key] = data[key]
            write_json(PERM_FILE, current)
            return self._send(200, {"permissions": current})

        if self.path == "/events":
            PERSONA.record_event(data.get("event", "generic"), data.get("value", ""))
            return self._send(200, {"ok": True})

        if self.path == "/tasks":
            task = {
                "title": data.get("title", "Untitled task"),
                "details": data.get("details", ""),
                "mode": "cloud" if get_permissions().get("run_when_phone_off") else "device-only",
            }
            tasks = read_json(TASK_FILE, [])
            tasks.append(task)
            write_json(TASK_FILE, tasks)
            return self._send(200, {"task": task, "message": "Task queued"})

        if self.path == "/reminders":
            reminder = {
                "title": data.get("title", "Reminder"),
                "at": data.get("at", "unspecified"),
                "note": data.get("note", ""),
                "status": "scheduled",
            }
            reminders = read_json(REMINDER_FILE, [])
            reminders.append(reminder)
            write_json(REMINDER_FILE, reminders)
            PERSONA.record_event("reminder_set", reminder["title"])
            return self._send(200, {"reminder": reminder})

        if self.path == "/notifications":
            perms = get_permissions()
            if not perms.get("notification_read"):
                return self._send(403, {"error": "permission_denied: notification_read"})
            notif = {
                "title": data.get("title", "Notification"),
                "body": data.get("body", ""),
                "status": "new",
            }
            notifications = read_json(NOTIF_FILE, [])
            notifications.append(notif)
            write_json(NOTIF_FILE, notifications)
            PERSONA.record_event("notification_received", notif["title"])
            return self._send(200, {"notification": notif})

        if self.path == "/os_action":
            perms = get_permissions()
            if not perms.get("os_level_control"):
                return self._send(403, {"error": "permission_denied: os_level_control"})
            action = data.get("action", "")
            needs, msg = CTX.clarification_needed(action, action_sensitive=True)
            if needs and not data.get("confirmed", False):
                return self._send(409, {"clarification_required": True, "message": msg})
            result = OSB.run_action(action, data)
            CTX.add_turn(f"os_action:{action}", str(result))
            return self._send(200, result)

        if self.path == "/device_search":
            perms = get_permissions()
            if not perms.get("device_search"):
                return self._send(403, {"error": "permission_denied: device_search"})
            base = data.get("base_path", str(Path.home() / "Documents"))
            query = data.get("query", "")
            if not query:
                return self._send(400, {"error": "query_required"})
            if not can_access_path(base):
                return self._send(403, {"error": "privacy_guard_blocked_path"})
            return self._send(200, {"results": search_files(base, query)})

        if self.path == "/generate":
            perms = get_permissions()
            if not perms.get("ai_chat"):
                return self._send(403, {"error": "permission_denied: ai_chat"})

            prompt = data.get("prompt")
            if not prompt:
                return self._send(400, {"error": "prompt_required"})
            needs, msg = CTX.clarification_needed(prompt, action_sensitive=False)
            if needs and not data.get("confirmed", False):
                return self._send(409, {"clarification_required": True, "message": msg})

            llama_cli = os.path.join(BITNET_DIR, "build/bin/llama-cli")
            if not os.path.exists(llama_cli):
                return self._send(503, {"error": f"missing_binary: {llama_cli}. Build BitNet first."})
            if not os.path.exists(MODEL_PATH):
                return self._send(503, {"error": f"missing_model: {MODEL_PATH}. Download model and run setup_env.py."})

            contextual_prompt = f"Recent context:\n{CTX.recent_summary()}\n\nUser request:\n{prompt}"
            cmd = [
                "python",
                os.path.join(BITNET_DIR, "run_inference.py"),
                "-m",
                MODEL_PATH,
                "-p",
                contextual_prompt,
                "-n",
                str(data.get("n_predict", 64)),
            ]

            try:
                proc = subprocess.run(cmd, capture_output=True, text=True, check=True, cwd=BITNET_DIR)
                out = proc.stdout.strip()
                CTX.add_turn(prompt, out)
                return self._send(200, {"output": out, "suggestions": PERSONA.suggestions()})
            except subprocess.CalledProcessError as exc:
                fb = generate_fallback(prompt)
                CTX.add_turn(prompt, fb)
                return self._send(200, {"output": fb, "fallback": True, "error": exc.stderr[-280:]})

        # Catch-all for unknown POST routes
        return self._send(404, {
            "error": "not_found",
            "available_endpoints": [
                "POST /generate", "POST /permissions", "POST /events",
                "POST /tasks", "POST /reminders", "POST /notifications",
                "POST /os_action", "POST /device_search"
            ]
        })


def main():
    if not API_KEY:
        raise SystemExit("BITNET_CLOUD_API_KEY must be set")
    write_json(PERM_FILE, get_permissions())
    HTTPServer((HOST, PORT), Handler).serve_forever()


if __name__ == "__main__":
    main()