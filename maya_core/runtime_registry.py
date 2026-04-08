#!/usr/bin/env python3
from __future__ import annotations

from dataclasses import asdict, dataclass
from pathlib import Path
import os
import platform

from maya_core.local_llm import bitnet_ready, find_bitnet_binary, find_bitnet_model


@dataclass
class RuntimeComponent:
    name: str
    kind: str
    provider: str
    status: str
    path: str | None = None
    notes: str | None = None


def _status_for_path(path: Path) -> str:
    return "ready" if path.exists() else "missing"


def discover_runtime_components() -> list[RuntimeComponent]:
    home = Path.home()
    bitnet_root = home / "bitnet.cpp"
    bitnet_binary = find_bitnet_binary()
    bitnet_model = find_bitnet_model()
    vendor_root = Path(os.environ.get("MAYA_VENDOR_DIR", str(home / "maya_vendor")))
    workspace_vendor_root = Path(__file__).resolve().parents[1] / "vendor"
    llvm_root = Path(r"C:\Program Files\LLVM\bin\clang.exe")
    cmake_root = Path(r"C:\Program Files\CMake\bin\cmake.exe")
    vs_cl = Path(
        r"C:\Program Files (x86)\Microsoft Visual Studio\2022\BuildTools\VC\Tools\MSVC\14.44.35207\bin\Hostx64\x64\cl.exe"
    )

    components = [
        RuntimeComponent(
            name="bitnet_backend",
            kind="llm",
            provider="Microsoft BitNet",
            status="ready" if bitnet_ready() else "missing",
            path=str(bitnet_root),
            notes=(
                "Heavy local fallback backend for CPU inference. "
                f"binary={bitnet_binary if bitnet_binary else 'missing'}, "
                f"model={bitnet_model if bitnet_model else 'missing'}"
            ),
        ),
        RuntimeComponent(
            name="mlc_android_backend",
            kind="llm",
            provider="MLC LLM Android",
            status="planned",
            notes="Primary phone-native inference path referenced by the Android runtime profile.",
        ),
        RuntimeComponent(
            name="whisper_cpp",
            kind="stt",
            provider="whisper.cpp",
            status=_status_for_path(vendor_root / "whisper.cpp"),
            path=str(vendor_root / "whisper.cpp"),
            notes="Offline speech recognition backend.",
        ),
        RuntimeComponent(
            name="piper",
            kind="tts",
            provider="Rhasspy Piper",
            status=_status_for_path(vendor_root / "piper"),
            path=str(vendor_root / "piper"),
            notes="Offline speech synthesis backend.",
        ),
        RuntimeComponent(
            name="openwakeword",
            kind="wake_word",
            provider="openWakeWord",
            status=_status_for_path(vendor_root / "openWakeWord"),
            path=str(vendor_root / "openWakeWord"),
            notes="Always-on wake-word detection path.",
        ),
        RuntimeComponent(
            name="ovos_core",
            kind="assistant_stack",
            provider="OpenVoiceOS",
            status=_status_for_path(vendor_root / "ovos-core"),
            path=str(vendor_root / "ovos-core"),
            notes="Intent/service reuse candidate for voice assistant flows.",
        ),
        RuntimeComponent(
            name="hivemind_core",
            kind="assistant_mesh",
            provider="HiveMind",
            status=_status_for_path(vendor_root / "hivemind-core"),
            path=str(vendor_root / "hivemind-core"),
            notes="Distributed agent/messaging reuse candidate.",
        ),
        RuntimeComponent(
            name="home_assistant",
            kind="smart_home",
            provider="Home Assistant",
            status=_status_for_path(vendor_root / "home-assistant-core"),
            path=str(vendor_root / "home-assistant-core"),
            notes="Smart-home and automation integration path.",
        ),
        RuntimeComponent(
            name="mediapipe_vision",
            kind="vision_privacy",
            provider="MediaPipe",
            status=_status_for_path(workspace_vendor_root / "mediapipe"),
            path=str(workspace_vendor_root / "mediapipe"),
            notes="Face and vision pipeline source for bystander-aware privacy flows.",
        ),
        RuntimeComponent(
            name="tensorflow_examples",
            kind="voice_privacy",
            provider="TensorFlow Examples",
            status=_status_for_path(workspace_vendor_root / "tensorflow-examples"),
            path=str(workspace_vendor_root / "tensorflow-examples"),
            notes="Reference source for compact TFLite audio and model integration paths.",
        ),
        RuntimeComponent(
            name="clang",
            kind="toolchain",
            provider="LLVM",
            status=_status_for_path(llvm_root),
            path=str(llvm_root),
            notes="Native compilation support for local backends.",
        ),
        RuntimeComponent(
            name="cmake",
            kind="toolchain",
            provider="Kitware",
            status=_status_for_path(cmake_root),
            path=str(cmake_root),
            notes="Build system support for BitNet and native code.",
        ),
        RuntimeComponent(
            name="msvc",
            kind="toolchain",
            provider="Visual Studio Build Tools",
            status=_status_for_path(vs_cl),
            path=str(vs_cl),
            notes="MSVC compiler required on Windows for BitNet build workflows.",
        ),
    ]
    return components


def runtime_summary() -> dict:
    return {
        "platform": platform.platform(),
        "components": [asdict(component) for component in discover_runtime_components()],
    }
