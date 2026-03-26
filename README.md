# pinacle the greates

## Maya assistant on top of Microsoft bitnet.cpp

You asked for a true Siri. A true Siri clone is not possible outside Apple private OS integrations.
What this repo gives you is the closest practical equivalent: an offline-capable local assistant with voice, local inference, and configurable permissions.

### 1) Install bitnet.cpp + build binary

```bash
./scripts/install_microsoft_local_ai.sh
```

### 2) Optional private-cloud API + frontend

```bash
./scripts/make_private_cloud.sh
$HOME/private-bitnet-cloud/start_private_cloud.sh
python -m http.server 4173
# open http://127.0.0.1:4173/frontend/
```

### 3) Offline talking mode (local)

```bash
python scripts/maya_offline_voice.py --text-only
# or single prompt:
python scripts/maya_offline_voice.py --once "Hello Maya" --text-only
```

This uses local BitNet inference only (no cloud required if model is local).

### Hard limit
Apple Siri system privileges (deep phone controls) are not available to third-party local scripts/web apps.
