# pinacle the greates

## Install Microsoft bitnet.cpp (local AI inference)

You were right — the Microsoft project you asked for is **bitnet.cpp** (repo: `microsoft/BitNet`).

### 1) Install bitnet.cpp

```bash
./scripts/install_microsoft_local_ai.sh
```

### 2) Make a private cloud (local private API)

```bash
./scripts/make_private_cloud.sh
$HOME/private-bitnet-cloud/start_private_cloud.sh
```

This private cloud API binds to `127.0.0.1` by default and requires an API key from `$HOME/private-bitnet-cloud/.env`.

### 3) Try talking to the AI

```bash
./scripts/talk_to_ai.sh "Hello BitNet"
```

If you see `missing_binary` or `missing_model`, finish BitNet runtime setup first:

```bash
huggingface-cli download microsoft/BitNet-b1.58-2B-4T-gguf --local-dir "$HOME/bitnet.cpp/models/BitNet-b1.58-2B-4T"
python "$HOME/bitnet.cpp/setup_env.py" -md "$HOME/bitnet.cpp/models/BitNet-b1.58-2B-4T" -q i2_s
```
