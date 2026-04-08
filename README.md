# pinacle

## Maya offline-native assistant

This repository is now aimed at a phone-first, offline-capable assistant rather than a web product.

Maya is structured around reusable components from:

- OVOS
- HiveMind
- whisper.cpp
- piper
- openWakeWord
- Home Assistant
- Microsoft BitNet
- MediaPipe
- TensorFlow Examples

## Native-first layout

- `android-native/` Kotlin Android shell for on-device assistant behavior
- `maya_core/` capability, context, privacy, personality, and fallback logic
- `private_cloud/` local/private model API components
- `integrations/` vendor shims and adapters
- `docs/OFFLINE_NATIVE_ROADMAP.md` phone-first roadmap
- `docs/NATIVE_ASSISTANT_STACK.md` end-state runtime architecture
- `config/maya_runtime_profile.json` Maya runtime priorities and target device profile

## Upstream bootstrap

```bash
./scripts/bootstrap_maya_stack.sh
./scripts/integrate_vendor_components.sh
./scripts/install_microsoft_local_ai.sh
```

On Windows, BitNet bootstrap can be driven from:

```powershell
.\scripts\setup_bitnet_windows.ps1
.\scripts\download_bitnet_model.ps1
```

## Local Maya runner

The local desktop and dev runner is wired to the real BitNet install with a quality gate:

```powershell
.\scripts\run_maya_local.ps1
python .\scripts\maya_offline_voice.py --status
python .\scripts\maya_offline_voice.py --once "What can you do?" --text-only
```

If BitNet returns degraded output, Maya automatically falls back to the local assistant layer in `maya_core/assistant_engine.py` and `maya_core/fallback_llm.py` so replies stay usable.

## Native direction

The Android app is the real product surface:

- foreground assistant service
- runtime profile driven offline inference coordination
- permission-aware capability inspection
- action routing for native device behaviors
- HyperOS bridge path for OEM-specific extensions
- local conversation memory
- runtime registry for `MLC`, `BitNet`, `whisper.cpp`, `piper`, `openWakeWord`, and vendor stacks
- vendor vision/audio references pulled into `vendor/mediapipe` and `vendor/tensorflow-examples`

## Capability direction

- wake word activation
- offline speech recognition
- local response generation with `MLC LLM Android` as primary runtime
- optional heavier fallback using `BitNet`
- reminders, notifications, and scheduling
- calling and messaging flows
- context memory and proactive suggestions
- guarded device actions and search
- vendor reuse from OVOS, HiveMind, and Home Assistant where it improves Maya

## Platform boundary

The deepest Siri-like device control depends on public Android APIs, assistant-role privileges, accessibility permissions, device-owner mode, OEM-private APIs, or privileged/system app deployment on the target phone.
