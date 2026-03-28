"""Minimal OVOS integration shim for Maya.

Loads OVOS-related vendor metadata and exposes a stable adapter surface.
"""
from pathlib import Path


def ovos_available() -> bool:
    return (Path(__file__).resolve().parent / "vendor" / "ovos-core").exists()
