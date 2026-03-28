#!/usr/bin/env python3
"""Fallback responder when local model is missing/invalid."""


def generate_fallback(prompt: str) -> str:
    p = prompt.strip()
    if not p:
        return "Hi, I'm Maya. How can I help you today?"
    if "remind" in p.lower():
        return "Got it — I can help set that reminder. Tell me time and note."
    if "call" in p.lower():
        return "I can prepare the call action, then ask for confirmation before executing."
    return f"Maya fallback response: I understood '{p[:140]}'."
