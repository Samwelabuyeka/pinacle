"""Minimal HiveMind integration shim for Maya."""
from pathlib import Path
from integrations.vendor_registry import vendor_component_registry


def hivemind_available() -> bool:
    return (Path(__file__).resolve().parent / "vendor" / "hivemind-core").exists()


def hivemind_reuse_profile() -> dict:
    for item in vendor_component_registry():
        if item.name == "hivemind-core":
            return {
                "available": item.available,
                "source": item.source,
                "local_path": item.local_path,
                "reusable_for": item.reusable_for,
                "maya_role": "assistant mesh, delegation, and remote execution",
            }
    return {
        "available": False,
        "source": "HiveMind",
        "local_path": "",
        "reusable_for": [],
        "maya_role": "assistant mesh, delegation, and remote execution",
    }
