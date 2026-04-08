# Maya Android Wiring

## What is now wired

- runtime profile loading
- model manifest loading
- live prompt testing in `MainActivity`
- action planning and local response generation
- conversation memory enrichment
- premium female voice profile selection for Piper
- notification listener and accessibility service scaffolds

## What still needs native engine binding

- real `MLC LLM Android` runtime integration
- real `Piper` speech synthesis binding
- real `whisper.cpp` streaming speech recognition
- real `openWakeWord` wake-word detection
- permission and role onboarding on the target phone

## Current testing flow

The app can now:

- show the runtime profile
- show the model manifest
- accept a typed prompt
- run it through Maya's Android action and response path
- preview the response in the UI
