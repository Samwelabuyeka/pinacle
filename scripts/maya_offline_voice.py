#!/usr/bin/env python3
"""Offline Maya assistant (best effort): local BitNet inference + optional TTS/STT.

This is not Apple's Siri and cannot access private iOS APIs.
"""
import argparse
import os
import subprocess
import sys

BITNET_DIR = os.environ.get("BITNET_DIR", os.path.expanduser("~/bitnet.cpp"))
MODEL_PATH = os.environ.get(
    "BITNET_MODEL",
    os.path.join(BITNET_DIR, "models/BitNet-b1.58-2B-4T/ggml-model-i2_s.gguf"),
)


def speak(text: str):
    try:
        import pyttsx3
        engine = pyttsx3.init()
        engine.say(text)
        engine.runAndWait()
    except Exception:
        print("[TTS unavailable]", text)


def run_local_inference(prompt: str, n_predict: int = 96) -> str:
    cmd = [
        "python",
        os.path.join(BITNET_DIR, "run_inference.py"),
        "-m",
        MODEL_PATH,
        "-p",
        prompt,
        "-n",
        str(n_predict),
    ]
    proc = subprocess.run(cmd, cwd=BITNET_DIR, capture_output=True, text=True)
    if proc.returncode != 0:
        return proc.stderr.strip()[-1200:] or "Inference failed"
    return proc.stdout.strip()


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
    args = parser.parse_args()

    if not os.path.exists(os.path.join(BITNET_DIR, "build/bin/llama-cli")):
        print("Missing binary: ~/bitnet.cpp/build/bin/llama-cli", file=sys.stderr)
        sys.exit(1)

    if args.once:
        out = run_local_inference(args.once)
        print(out)
        if not args.text_only:
            speak(out)
        return

    print("Maya offline mode. Say 'exit' to quit.")
    while True:
        user_text = input("You: ").strip() if args.text_only else listen_once().strip()
        if user_text.lower() in {"exit", "quit"}:
            break
        answer = run_local_inference(user_text)
        print("Maya:", answer)
        if not args.text_only:
            speak(answer)


if __name__ == "__main__":
    main()
