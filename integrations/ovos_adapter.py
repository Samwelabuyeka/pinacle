"""Minimal OVOS integration shim for Maya.

Loads OVOS-related vendor metadata and exposes a stable adapter surface.
"""
from pathlib import Path
from integrations.vendor_registry import vendor_component_registry


def ovos_available() -> bool:
    return (Path(__file__).resolve().parent / "vendor" / "ovos-core").exists()


def ovos_reuse_profile() -> dict:
    for item in vendor_component_registry():
        if item.name == "ovos-core":
            return {
                "available": item.available,
                "source": item.source,
                "local_path": item.local_path,
                "reusable_for": item.reusable_for,
                "maya_role": "voice assistant intent and service orchestration",
            }
    return {
        "available": False,
        "source": "OpenVoiceOS",
        "local_path": "",
        "reusable_for": [],
        "maya_role": "voice assistant intent and service orchestration",
    }
