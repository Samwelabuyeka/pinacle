# Maya Implementation Notes

## What is being reused

Maya is not starting from zero. The repo now tracks how these upstream components are reused:

- `ovos-core` for assistant-style intent and service orchestration ideas
- `hivemind-core` for mesh and delegation patterns
- `whisper.cpp` for offline speech-to-text
- `piper` for offline speech output
- `openWakeWord` for always-on wake word
- `home-assistant-core` for automation and smart-home actions

The reuse registry lives in:

- `integrations/vendor_registry.py`
- `integrations/ovos_adapter.py`
- `integrations/hivemind_adapter.py`

## Runtime priorities

Maya's current runtime plan is:

1. `MLC LLM Android` as the main phone-native model path
2. `BitNet` as the heavier local backend
3. `whisper.cpp` for STT
4. `piper` for TTS
5. `openWakeWord` for wake word

These priorities are encoded in:

- `config/maya_runtime_profile.json`
- `android-native/app/src/main/assets/runtime_profile.json`

## Validation

Use:

```bash
python scripts/validate_maya_setup.py
python scripts/prepare_maya_vendor_manifest.py
python scripts/maya_offline_voice.py --status
```

to inspect runtime readiness and generate the current vendor manifest.

## Current local runtime state

- `BitNet` is installed, compiled, and can execute prompts locally through `llama-cli.exe`
- the current BitNet 2B checkpoint still produces degraded text for some prompts
- `maya_core/assistant_engine.py` now quality-checks BitNet output and falls back to local assistant logic when needed
- `scripts/run_maya_local.ps1` is the easiest Windows entrypoint for trying Maya locally today
