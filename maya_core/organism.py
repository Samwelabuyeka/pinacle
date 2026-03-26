#!/usr/bin/env python3
"""Maya organism core: long-running memory + task executor loop."""

from __future__ import annotations
import json
import os
import sqlite3
import subprocess
import time
from pathlib import Path

HOME = Path.home()
BITNET_DIR = Path(os.environ.get("BITNET_DIR", str(HOME / "bitnet.cpp")))
MODEL_PATH = Path(os.environ.get("BITNET_MODEL", str(BITNET_DIR / "models/BitNet-b1.58-2B-4T/ggml-model-i2_s.gguf")))
TASK_FILE = Path(os.environ.get("MAYA_TASK_FILE", str(HOME / ".maya_tasks.json")))
DB_PATH = Path(os.environ.get("MAYA_MEMORY_DB", str(HOME / ".maya_memory.db")))


class MayaOrganism:
    def __init__(self):
        self.db = sqlite3.connect(DB_PATH)
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
        cmd = [
            "python",
            str(BITNET_DIR / "run_inference.py"),
            "-m",
            str(MODEL_PATH),
            "-p",
            prompt,
            "-n",
            "96",
        ]
        proc = subprocess.run(cmd, cwd=BITNET_DIR, capture_output=True, text=True)
        if proc.returncode != 0:
            return proc.stderr.strip()[-1000:] or "inference_failed"
        return proc.stdout.strip()

    def _load_tasks(self) -> list[dict]:
        if not TASK_FILE.exists():
            return []
        try:
            return json.loads(TASK_FILE.read_text())
        except Exception:
            return []

    def _save_tasks(self, tasks: list[dict]):
        TASK_FILE.write_text(json.dumps(tasks, indent=2))

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
        return done

    def run_forever(self, interval_sec: int = 10):
        while True:
            self.step()
            time.sleep(interval_sec)
