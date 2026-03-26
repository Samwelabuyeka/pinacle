# pinacle the greates

## Maya assistant on top of Microsoft bitnet.cpp + OVOS ecosystem

Maya is structured around reusable components from:
- OVOS
- HiveMind
- whisper.cpp
- piper
- openWakeWord
- Home Assistant

### 0) Bootstrap dependency repos from GitHub

```bash
./scripts/bootstrap_maya_stack.sh
```

This downloads source repos so you can strip/rename/reuse only what your target device build needs.

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
- Capability detection (`/capabilities`) so Maya knows what she can do on the current device
- Personality engine + proactive suggestions (`/events`, `/suggestions`)
- Reminders API (`/reminders`) plus organism reminder processing
- Privacy-guarded device search (`/device_search`) with allowlisted directories only
- Permission controls for always mic/speaker + device search + phone-off execution

### Privacy defense
- Explicit permission gates on sensitive capabilities
- API key auth
- filesystem search restricted to safe roots (Documents/Downloads/Desktop)

### Platform boundary
Deep Apple-private Siri privileges and full telephony delegation are not available to third-party code everywhere. This stack is built to get as close as possible across platforms with strong privacy controls.
