# pinacle the greates

## Maya assistant on top of Microsoft bitnet.cpp + OVOS ecosystem

Maya is structured around reusable components from:
- OVOS
- HiveMind
- whisper.cpp
- piper
- openWakeWord
- Home Assistant

### 0) Pull upstream repos (for strip/rename/reuse)

```bash
./scripts/bootstrap_maya_stack.sh
```

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

### Core features for “next Siri” direction
- Capability detection (`/capabilities`) so Maya knows what she can do
- Full capability matrix (`/capability_matrix`) for Siri-like feature parity tracking
- Personality engine + proactive suggestions (`/events`, `/suggestions`)
- Reminders API (`/reminders`) + organism reminder processing
- Privacy-guarded device search (`/device_search`) with allowlisted paths
- OS action interface (`/os_action`) guarded by `os_level_control` permission
- Permission controls for always mic/speaker + device search + phone-off execution

### Privacy defense
- explicit permission gates on sensitive capabilities
- API key auth
- filesystem search restricted to safe roots (Documents/Downloads/Desktop)

### Platform boundary
Deep Siri-equivalent OS control requires native platform plugins (Android/iOS telephony/notification APIs). This repo includes the permissioned OS-action bridge interface and capability detection so those native plugins can be added safely per device.
