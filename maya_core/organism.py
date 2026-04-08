#!/usr/bin/env python3
"""Maya organism core: long-running memory + task executor loop."""

from __future__ import annotations
import json
import os
import sqlite3
import time
from pathlib import Path

from maya_core.assistant_engine import generate_reply
from maya_core.context_engine import ContextEngine

HOME = Path.home()
BITNET_DIR = Path(os.environ.get("BITNET_DIR", str(HOME / "bitnet.cpp")))
MODEL_PATH = Path(os.environ.get("BITNET_MODEL", str(BITNET_DIR / "models/BitNet-b1.58-2B-4T/ggml-model-i2_s.gguf")))
TASK_FILE = Path(os.environ.get("MAYA_TASK_FILE", str(HOME / ".maya_tasks.json")))
REMINDER_FILE = Path(os.environ.get("MAYA_REMINDER_FILE", str(HOME / ".maya_reminders.json")))
NOTIF_FILE = Path(os.environ.get("MAYA_NOTIFICATION_FILE", str(HOME / ".maya_notifications.json")))
DB_PATH = Path(os.environ.get("MAYA_MEMORY_DB", str(HOME / ".maya_memory.db")))


class MayaOrganism:
    def __init__(self):
        self.db = sqlite3.connect(DB_PATH)
        self.context = ContextEngine()
        self.db.execute(
            "create table if not exists memory (id integer primary key, ts real, kind text, payload text)"
        )
        self.db.commit()

    def remember(self, kind: str, payload: dict):
        self.db.execute(
            "insert into memory(ts, kind, payload) values(?,?,?)",
            (time.time(), kind, json.dumps(payload)),
        )
        self.db.commit()

    def infer(self, prompt: str) -> str:
        response = generate_reply(prompt, context=self.context)
        self.context.add_turn(prompt, response)
        return response

    def _load_tasks(self) -> list[dict]:
        if not TASK_FILE.exists():
            return []
        try:
            return json.loads(TASK_FILE.read_text())
        except Exception:
            return []

    def _save_tasks(self, tasks: list[dict]):
        TASK_FILE.write_text(json.dumps(tasks, indent=2))

    def _load_reminders(self) -> list[dict]:
        if not REMINDER_FILE.exists():
            return []
        try:
            return json.loads(REMINDER_FILE.read_text())
        except Exception:
            return []

    def _save_reminders(self, reminders: list[dict]):
        REMINDER_FILE.write_text(json.dumps(reminders, indent=2))

    def _load_notifications(self) -> list[dict]:
        if not NOTIF_FILE.exists():
            return []
        try:
            return json.loads(NOTIF_FILE.read_text())
        except Exception:
            return []

    def _save_notifications(self, notifications: list[dict]):
        NOTIF_FILE.write_text(json.dumps(notifications, indent=2))

    def step(self) -> list[dict]:
        tasks = self._load_tasks()
        done = []
        remaining = []
        for t in tasks:
            if t.get("status") == "done":
                remaining.append(t)
                continue
            prompt = f"Task: {t.get('title')}\nDetails: {t.get('details', '')}\nProvide actionable plan."
            result = self.infer(prompt)
            t["status"] = "done"
            t["result"] = result
            t["completed_at"] = time.time()
            done.append(t)
            self.remember("task_completed", t)
        self._save_tasks(remaining + done)

        reminders = self._load_reminders()
        for r in reminders:
            if r.get("status") == "scheduled":
                r["status"] = "announced"
                r["announced_at"] = time.time()
                self.remember("reminder_announced", r)
        self._save_reminders(reminders)

        notifications = self._load_notifications()
        for n in notifications:
            if n.get("status") == "new":
                n["status"] = "announced"
                n["announced_at"] = time.time()
                self.remember("notification_announced", n)
        self._save_notifications(notifications)
        return done

    def run_forever(self, interval_sec: int = 10):
        while True:
            self.step()
            time.sleep(interval_sec)
