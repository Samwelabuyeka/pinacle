"""Minimal HiveMind integration shim for Maya."""
from pathlib import Path


def hivemind_available() -> bool:
    return (Path(__file__).resolve().parent / "vendor" / "hivemind-core").exists()
