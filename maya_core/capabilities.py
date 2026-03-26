#!/usr/bin/env python3
from __future__ import annotations

import importlib.util
import platform


def _has_module(name: str) -> bool:
    return importlib.util.find_spec(name) is not None


def detect_capabilities(permissions: dict) -> dict:
    os_name = platform.system().lower()
    return {
        "voice_input": permissions.get("always_mic", False),
        "voice_output": permissions.get("always_speaker", False),
        "offline_stt": _has_module("speech_recognition"),
        "offline_tts": _has_module("pyttsx3"),
        "device_search": permissions.get("device_search", False),
        "reminders": True,
        "task_automation": True,
        "call_management": os_name in {"android", "ios"} and permissions.get("make_calls", False),
        "phone_call_delegate": False,  # requires platform-native telephony integration
    }
