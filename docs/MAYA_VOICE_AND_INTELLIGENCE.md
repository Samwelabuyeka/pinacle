# Maya Voice And Intelligence

## Voice target

Maya should sound:

- feminine
- warm
- confident
- elegant
- clear during short assistant replies
- expressive without sounding artificial

The Android runtime profile now carries:

- `voiceProfileId`
- `voiceStyle`
- `voiceBackendTarget`

This is intended for a premium offline Piper voice selection rather than generic platform TTS.

## Intelligence target

Maya should exceed a basic assistant by combining:

- a fast phone-native model for normal requests
- a heavier reasoning model when needed
- memory-aware prompting
- action-first routing for calls, SMS, reminders, alarms, notifications, and app launch
- capability awareness so Maya knows what is available, restricted, or requires elevated privileges

## Product direction

The goal is not only to answer questions. Maya should:

- understand intent
- decide whether to act or converse
- remember recent context
- use a premium female offline voice
- escalate to heavier reasoning only when it materially improves the result
