# pinacle the greates

## Install Microsoft bitnet.cpp (local AI inference)

This project runs **Maya**, a Siri-style assistant UX on top of `microsoft/BitNet`.

### 1) Install bitnet.cpp + build the binary

```bash
./scripts/install_microsoft_local_ai.sh
```

Installer behavior:
- builds `~/bitnet.cpp/build/bin/llama-cli`
- asks for permissions during install in interactive shells
- stores permissions in `~/.maya_permissions.json`

### 2) Start private cloud API

```bash
./scripts/make_private_cloud.sh
$HOME/private-bitnet-cloud/start_private_cloud.sh
```

### 3) Open frontend and manage permissions

```bash
python -m http.server 4173
# open http://127.0.0.1:4173/frontend/
```

Frontend capabilities:
- voice input + spoken responses
- permission management (`/permissions` API)
- task queueing (`/tasks` API)
- toggle `run_when_phone_off` for cloud-mode task execution policy

### 4) Talk from terminal

```bash
./scripts/talk_to_ai.sh "Hello Maya"
```

### Important platform note
A web app cannot grant Apple's private Siri OS privileges. This implementation provides install-time permission prompts + editable permission settings in the frontend for your own local/cloud stack.
