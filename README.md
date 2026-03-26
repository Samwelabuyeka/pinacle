# pinacle the greates

## Maya assistant on top of Microsoft bitnet.cpp

You want Maya to feel like a living organism, always active, speaking, remembering, and handling tasks.
This repo now includes an **organism loop** (continuous daemon + memory + task execution) in addition to the frontend/API.

### 1) Install bitnet.cpp + build binary

```bash
./scripts/install_microsoft_local_ai.sh
```

### 2) Start private cloud API + frontend

```bash
./scripts/make_private_cloud.sh
$HOME/private-bitnet-cloud/start_private_cloud.sh
python -m http.server 4173
# open http://127.0.0.1:4173/frontend/
```

### 3) Start Maya organism mode (always-on loop)

```bash
python scripts/maya_daemon.py --interval 10
```

One-cycle dry run:

```bash
python scripts/maya_daemon.py --once
```

### 4) Offline talking mode

```bash
python scripts/maya_offline_voice.py --text-only
```

### What this adds
- long-running agent loop
- persistent memory (`~/.maya_memory.db`)
- background task execution from `~/.maya_tasks.json`
- local BitNet inference path

### Hard platform boundary
Apple private Siri system APIs are not available to third-party code.
This stack provides the closest equivalent behavior on your own infrastructure (local + private cloud + persistent agent loop).
