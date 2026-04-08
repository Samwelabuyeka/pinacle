#!/usr/bin/env python3
"""Fallback responder when local model is missing or low quality."""


def generate_fallback(prompt: str, context_summary: str = "") -> str:
    p = prompt.strip()
    lower = p.lower()
    if not p:
        return "Hi, I'm Maya. How can I help you today?"
    if "remind" in lower:
        return "I can handle that reminder offline. Tell me the time and the reminder text, and I will stage it for confirmation."
    if "alarm" in lower:
        return "I can prepare the alarm request. Tell me the exact time and whether it should repeat."
    if "call" in lower:
        return "I can prepare the call action and ask for confirmation before executing it on the phone."
    if "message" in lower or "text " in lower or lower.startswith("sms"):
        return "I can draft the message flow offline. Tell me the recipient and the message content."
    if "calendar" in lower or "schedule" in lower:
        return "I can help organize your schedule. Tell me the event title, time, and any location or notes."
    if "open " in lower or "launch " in lower:
        return f"I understood that as an app or device action request: '{p[:140]}'. I can route that through Maya's native action layer."
    if "what can you do" in lower or "capabilities" in lower:
        return (
            "I can operate as an offline assistant for reminders, tasks, message drafting, call preparation, "
            "device actions, smart-home routing, and local voice flows. Deeper phone control depends on Android permissions."
        )
    prefix = "I understood your request and can keep this fully local."
    if context_summary and context_summary != "No prior context.":
        prefix += " I am also tracking recent context for continuity."
    return f"{prefix} You said: '{p[:140]}'."
