#!/usr/bin/env python3
"""Offline Maya assistant (best effort): local BitNet inference + optional TTS/STT.

This is not Apple's Siri and cannot access private iOS APIs.
"""
import argparse
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from maya_core.assistant_engine import generate_reply
from maya_core.context_engine import ContextEngine
from maya_core.local_llm import BITNET_DIR, BITNET_MODEL, bitnet_ready


def speak(text: str):
    try:
        import pyttsx3
        engine = pyttsx3.init()
        engine.say(text)
        engine.runAndWait()
    except Exception:
        print("[TTS unavailable]", text)


def listen_once() -> str:
    """Offline speech-to-text if available; otherwise typed prompt."""
    try:
        import speech_recognition as sr
        r = sr.Recognizer()
        with sr.Microphone() as source:
            print("Listening...")
            audio = r.listen(source, timeout=6, phrase_time_limit=12)
        return r.recognize_vosk(audio)
    except Exception:
        return input("You: ").strip()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--text-only", action="store_true", help="Disable TTS and use typed input")
    parser.add_argument("--once", type=str, default="", help="Run one prompt and exit")
    parser.add_argument("--status", action="store_true", help="Print local runtime status and exit")
    parser.add_argument("--tokens", type=int, default=96, help="Maximum tokens to generate")
    args = parser.parse_args()
    context = ContextEngine()

    if args.status:
        print(f"bitnet_dir={BITNET_DIR}")
        print(f"bitnet_model={BITNET_MODEL}")
        print(f"bitnet_ready={bitnet_ready()}")
        return

    if not bitnet_ready():
        print(f"BitNet runtime not ready. dir={BITNET_DIR} model={BITNET_MODEL}", file=sys.stderr)
        sys.exit(1)

    if args.once:
        out = generate_reply(args.once, context=context, tokens=args.tokens)
        context.add_turn(args.once, out)
        print(out)
        if not args.text_only:
            speak(out)
        return

    print("Maya offline mode. Say 'exit' to quit.")
    while True:
        user_text = input("You: ").strip() if args.text_only else listen_once().strip()
        if user_text.lower() in {"exit", "quit"}:
            break
        answer = generate_reply(user_text, context=context, tokens=args.tokens)
        context.add_turn(user_text, answer)
        print("Maya:", answer)
        if not args.text_only:
            speak(answer)


if __name__ == "__main__":
    main()
