"""Registry describing how Maya reuses upstream assistant components."""
from __future__ import annotations

from dataclasses import asdict, dataclass
from pathlib import Path
import os


@dataclass
class VendorComponent:
    name: str
    source: str
    reusable_for: list[str]
    local_path: str
    available: bool


def _vendor_root() -> Path:
    return Path(os.environ.get("MAYA_VENDOR_DIR", str(Path.home() / "maya_vendor")))


def _workspace_vendor_root() -> Path:
    return Path(__file__).resolve().parents[1] / "vendor"


def _resolve_vendor_path(folder: str) -> Path:
    workspace_path = _workspace_vendor_root() / folder
    if workspace_path.exists():
        return workspace_path
    return _vendor_root() / folder


def vendor_component_registry() -> list[VendorComponent]:
    specs = [
        ("ovos-core", "OpenVoiceOS", ["intent routing", "service bus", "assistant skills"]),
        ("hivemind-core", "HiveMind", ["agent mesh", "message routing", "delegation"]),
        ("whisper.cpp", "whisper.cpp", ["offline speech recognition"]),
        ("piper", "Piper", ["offline speech synthesis"]),
        ("openWakeWord", "openWakeWord", ["wake-word detection"]),
        ("home-assistant-core", "Home Assistant", ["smart-home control", "device automations"]),
        ("mediapipe", "MediaPipe", ["face detection", "face landmarks", "on-device vision privacy"]),
        ("tensorflow-examples", "TensorFlow Examples", ["small TFLite examples", "audio embeddings", "local model integration"]),
    ]
    items: list[VendorComponent] = []
    for folder, source, reusable_for in specs:
        path = _resolve_vendor_path(folder)
        items.append(
            VendorComponent(
                name=folder,
                source=source,
                reusable_for=reusable_for,
                local_path=str(path),
                available=path.exists(),
            )
        )
    return items


def vendor_registry_as_dict() -> list[dict]:
    return [asdict(item) for item in vendor_component_registry()]
