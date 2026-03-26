# pinacle the greates

## Maya assistant on top of Microsoft bitnet.cpp + OVOS ecosystem

Maya is now structured to integrate with:
- OVOS
- HiveMind
- whisper.cpp
- piper
- openWakeWord
- Home Assistant

### 1) Install core

```bash
./scripts/install_microsoft_local_ai.sh
```

### 2) Start private API + frontend

```bash
./scripts/make_private_cloud.sh
$HOME/private-bitnet-cloud/start_private_cloud.sh
python -m http.server 4173
# open http://127.0.0.1:4173/frontend/
```

### 3) Start organism loop

```bash
python scripts/maya_daemon.py --interval 10
```

### Powerful behavior added
- Personality engine with learning events + proactive suggestions (`/events`, `/suggestions`)
- Privacy-guarded device search (`/device_search`) with allowlisted directories only
- Permission controls for always mic/speaker + device search + phone-off execution
- Persistent memory and task processing loop

### Privacy defense
- Explicit permission gates on sensitive capabilities
- API key auth
- filesystem search restricted to user-safe roots (Documents/Downloads/Desktop)

### Platform boundary
Deep Apple-private Siri privileges are not available to third-party code, but this stack is designed to deliver a powerful Siri-like daily assistant experience across platforms with strong privacy controls.
