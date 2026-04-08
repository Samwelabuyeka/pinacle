# Offline Native Roadmap

## Product direction

Maya is now treated as an offline-first phone assistant, not a web app.

## On-device pipeline

1. Wake word detection using `openWakeWord`
2. Speech-to-text using `whisper.cpp`
3. Intent and response planning using local BitNet or fallback local models
4. Speech synthesis using `piper`
5. Native action routing for alarms, reminders, calls, messaging, notifications, and app control

## Phone-first requirements

- always-on foreground assistant service
- permission-aware capability inspection
- offline storage for context and reminders
- boot recovery so the assistant can resume after restart
- OEM bridge layer for HyperOS and similar Android variants

## Reality boundary

The strongest assistant experience is achievable, but some powers require:

- assistant role privileges
- accessibility permissions
- device owner mode
- OEM-private APIs
- privileged/system app signing

That is the path to exceed Siri in flexibility while staying honest about deployment boundaries.
