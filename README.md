# pinacle the greates

## Install Microsoft bitnet.cpp (local AI inference)

You were right — the Microsoft project you asked for is **bitnet.cpp** (repo: `microsoft/BitNet`).

### 1) Install bitnet.cpp + build the binary

```bash
./scripts/install_microsoft_local_ai.sh
```

This builds `~/bitnet.cpp/build/bin/llama-cli` automatically.

### 2) Make a private cloud (local private API)

```bash
./scripts/make_private_cloud.sh
$HOME/private-bitnet-cloud/start_private_cloud.sh
```

This private cloud API binds to `127.0.0.1` by default and requires an API key from `$HOME/private-bitnet-cloud/.env`.

### 3) Siri-style frontend (voice + speech back)

```bash
python -m http.server 4173
# open http://127.0.0.1:4173/frontend/
```

The frontend in `frontend/` supports microphone input and spoken responses in-browser.

> Important: no web app can grant true Siri OS-level privileges (phone calls, SMS, contacts, system settings)
> without platform-native permissions and Apple private integrations. This project provides a private local assistant UI/API only.

### 4) Try talking to the AI from terminal

```bash
./scripts/talk_to_ai.sh "Hello BitNet"
```

If you see `missing_model`, download a real BitNet model and prepare it:

```bash
huggingface-cli download microsoft/BitNet-b1.58-2B-4T-gguf --local-dir "$HOME/bitnet.cpp/models/BitNet-b1.58-2B-4T"
python "$HOME/bitnet.cpp/setup_env.py" -md "$HOME/bitnet.cpp/models/BitNet-b1.58-2B-4T" -q i2_s
```
