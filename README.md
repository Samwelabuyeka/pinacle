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

### Test call

```bash
source "$HOME/private-bitnet-cloud/.env"
curl -s http://127.0.0.1:8080/generate \
  -H "Authorization: Bearer $BITNET_CLOUD_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"prompt":"You are a helpful assistant","n_predict":32}'
```
