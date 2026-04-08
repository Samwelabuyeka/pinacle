#!/usr/bin/env python3
from __future__ import annotations

from maya_core.context_engine import ContextEngine
from maya_core.fallback_llm import generate_fallback
from maya_core.local_llm import bitnet_ready, generate_with_bitnet


def _looks_degraded(text: str) -> bool:
    stripped = text.strip()
    if not stripped:
        return True
    if stripped.count("G") > max(12, len(stripped) // 2):
        return True
    if "User:" in stripped and "Maya:" in stripped and stripped.endswith("G"):
        return True
    if len(stripped) > 20 and len(set(stripped)) < 8:
        return True
    return False


def generate_reply(prompt: str, context: ContextEngine | None = None, *, tokens: int = 96) -> str:
    context_summary = context.recent_summary() if context else "No prior context."
    if bitnet_ready():
        try:
            response = generate_with_bitnet(
                f"Recent context:\n{context_summary}\n\nCurrent request: {prompt}",
                n_predict=tokens,
            )
            if not _looks_degraded(response):
                return response
        except Exception:
            pass
    return generate_fallback(prompt, context_summary=context_summary)
