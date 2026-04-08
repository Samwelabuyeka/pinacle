#!/usr/bin/env python3
from __future__ import annotations

import os
import subprocess
from pathlib import Path


BITNET_DIR = Path(os.environ.get("BITNET_DIR", str(Path.home() / "bitnet.cpp")))
BITNET_MODEL = Path(
    os.environ.get(
        "BITNET_MODEL",
        str(BITNET_DIR / "models" / "BitNet-b1.58-2B-4T" / "ggml-model-i2_s.gguf"),
    )
)
BITNET_BINARY_CANDIDATES = (
    BITNET_DIR / "build" / "bin" / "llama-cli.exe",
    BITNET_DIR / "build" / "bin" / "llama-cli",
)


def find_bitnet_binary() -> Path | None:
    for candidate in BITNET_BINARY_CANDIDATES:
        if candidate.exists():
            return candidate
    return None


def find_bitnet_model() -> Path | None:
    return BITNET_MODEL if BITNET_MODEL.exists() else None


def bitnet_ready() -> bool:
    return find_bitnet_binary() is not None and find_bitnet_model() is not None


def build_assistant_prompt(user_prompt: str) -> str:
    return (
        "You are Maya, a private offline native assistant. "
        "Be concise, capable, calm, and action-oriented. "
        "Prefer practical answers, keep context local, and never claim cloud access.\n\n"
        f"User: {user_prompt}\n"
        "Maya:"
    )


def generate_with_bitnet(
    prompt: str,
    *,
    n_predict: int = 96,
    threads: int = 2,
    temperature: float = 0.7,
) -> str:
    binary = find_bitnet_binary()
    model = find_bitnet_model()
    if binary is None:
        raise FileNotFoundError(f"BitNet binary not found under {BITNET_DIR}")
    if model is None:
        raise FileNotFoundError(f"BitNet model not found under {BITNET_MODEL}")

    command = [
        str(binary),
        "-m",
        str(model),
        "-p",
        build_assistant_prompt(prompt),
        "-n",
        str(n_predict),
        "-t",
        str(threads),
        "--temp",
        str(temperature),
        "--no-warmup",
        "--simple-io",
    ]
    proc = subprocess.run(
        command,
        cwd=BITNET_DIR,
        capture_output=True,
        text=True,
        check=False,
    )
    if proc.returncode != 0:
        error_text = proc.stderr.strip() or proc.stdout.strip() or "BitNet inference failed"
        raise RuntimeError(error_text[-2000:])
    output = proc.stdout.strip()
    return output or "Maya is online, but the model returned an empty response."
