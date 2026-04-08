# Native Assistant Stack

## Product goal

Build Maya as an offline-first phone assistant that can cover and extend the common Siri-style workflows:

- wake by voice
- answer questions
- control reminders and calendars
- place calls and draft messages
- launch apps and route device actions
- read context and make proactive suggestions
- preserve privacy by keeping inference and memory local when possible

## Runtime stack

### Wake word

- `openWakeWord`

### Speech recognition

- `whisper.cpp`

### Speech synthesis

- `piper`

### On-device reasoning

- `MLC LLM Android` as the primary phone runtime
- `BitNet` as the heavier local fallback / experimental backend

### Native orchestration

- `android-native/` Kotlin app and foreground service
- runtime profile in `android-native/app/src/main/assets/runtime_profile.json`
- memory store for local context
- action router for phone capabilities
- HyperOS bridge slot for OEM-specific work

## Execution model

1. wake word detector arms in a foreground service
2. speech is transcribed locally
3. local model chooses a response or action
4. action router executes supported phone operations
5. response is spoken locally
6. memory layer stores the turn for future context

## Scaling intelligence

The assistant should use multiple tiers of intelligence rather than a single oversized model:

- fast lightweight model for instant intent recognition
- stronger phone model for normal conversation and planning
- optional heavier local backend for complex reasoning

That is how we maximize usefulness without destroying latency, storage, or battery.
