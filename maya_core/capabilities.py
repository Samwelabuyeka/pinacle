#!/usr/bin/env python3
from __future__ import annotations

import importlib.util
import platform
from maya_core.os_bridge import OSBridge
from maya_core.runtime_registry import discover_runtime_components


def _has_module(name: str) -> bool:
    return importlib.util.find_spec(name) is not None


def detect_capabilities(permissions: dict) -> dict:
    os_name = platform.system().lower()
    bridge = OSBridge().capabilities()
    runtimes = {component.name: component.status for component in discover_runtime_components()}
    return {
        "voice_input": permissions.get("always_mic", False),
        "voice_output": permissions.get("always_speaker", False),
        "offline_stt": _has_module("speech_recognition") or runtimes.get("whisper_cpp") == "ready",
        "offline_tts": _has_module("pyttsx3") or runtimes.get("piper") == "ready",
        "device_search": permissions.get("device_search", False),
        "notification_read": permissions.get("notification_read", False),
        "reminders": True,
        "task_automation": True,
        "call_management": os_name in {"android", "ios"} and permissions.get("make_calls", False),
        "phone_call_delegate": bridge.get("call_screening", False),
        "os_level_control": permissions.get("os_level_control", False) and bridge.get("os_level_control", False),
        "wake_word": runtimes.get("openwakeword") == "ready",
        "local_llm_primary": runtimes.get("mlc_android_backend") in {"ready", "planned"},
        "local_llm_heavy": runtimes.get("bitnet_backend") == "ready",
        "smart_home_control": runtimes.get("home_assistant") == "ready",
    }
