#!/usr/bin/env python3
from __future__ import annotations

import json
import os
from pathlib import Path

CTX_FILE = Path(os.environ.get("MAYA_CONTEXT_FILE", str(Path.home() / ".maya_context.json")))


class ContextEngine:
    def __init__(self):
        self.data = self._load()

    def _load(self):
        if CTX_FILE.exists():
            try:
                return json.loads(CTX_FILE.read_text())
            except Exception:
                pass
        return {"turns": [], "preferences": {}, "last_action": None}

    def save(self):
        CTX_FILE.write_text(json.dumps(self.data, indent=2))

    def add_turn(self, user: str, assistant: str = ""):
        turns = self.data.setdefault("turns", [])
        turns.append({"user": user, "assistant": assistant})
        self.data["turns"] = turns[-8:]
        self.save()

    def recent_summary(self) -> str:
        turns = self.data.get("turns", [])[-4:]
        if not turns:
            return "No prior context."
        lines = []
        for t in turns:
            lines.append(f"U:{t.get('user','')} | A:{t.get('assistant','')[:80]}")
        return "\n".join(lines)

    def confidence(self, text: str) -> float:
        text = text.strip()
        if len(text) < 4:
            return 0.2
        if text.endswith("?"):
            return 0.85
        if any(w in text.lower() for w in ["call", "send", "delete", "pay", "transfer"]):
            return 0.55
        return 0.78

    def clarification_needed(self, text: str, action_sensitive: bool = False) -> tuple[bool, str]:
        c = self.confidence(text)
        if action_sensitive and c < 0.8:
            return True, "I might have misheard. Do you want me to proceed with this action?"
        if c < 0.5:
            return True, "I’m not fully sure I heard you correctly. Can you rephrase?"
        return False, ""
