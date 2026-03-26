#!/usr/bin/env python3
from __future__ import annotations

import json
import os
from collections import Counter
from pathlib import Path

PROFILE_PATH = Path(os.environ.get("MAYA_PROFILE_FILE", str(Path.home() / ".maya_profile.json")))


class PersonalityEngine:
    def __init__(self):
        self.profile = self._load()

    def _load(self) -> dict:
        if PROFILE_PATH.exists():
            try:
                return json.loads(PROFILE_PATH.read_text())
            except Exception:
                pass
        return {
            "name": "Maya",
            "tone": "warm, concise, proactive",
            "likes": [],
            "dislikes": [],
            "event_counts": {},
        }

    def save(self):
        PROFILE_PATH.write_text(json.dumps(self.profile, indent=2))

    def record_event(self, event: str, value: str = ""):
        counts = Counter(self.profile.get("event_counts", {}))
        counts[event] += 1
        self.profile["event_counts"] = dict(counts)
        if value:
            likes = set(self.profile.get("likes", []))
            likes.add(value)
            self.profile["likes"] = sorted(likes)
        self.save()

    def suggestions(self) -> list[str]:
        counts = Counter(self.profile.get("event_counts", {}))
        likes = self.profile.get("likes", [])[:5]
        sugg = []
        if counts.get("music_play", 0) > 0:
            sugg.append("You often listen to music. Want a daily playlist routine?")
        if counts.get("calendar_check", 0) > 0:
            sugg.append("I can summarize your day every morning.")
        if likes:
            sugg.append(f"You seem to like: {', '.join(likes)}. Want more suggestions around these?")
        if not sugg:
            sugg.append("Tell me what you do most and I’ll personalize suggestions.")
        return sugg
