# pinacle the greates

## Install Microsoft bitnet.cpp (local AI inference)

You were right — the Microsoft project you asked for is **bitnet.cpp** (repo: `microsoft/BitNet`).

### Run the installer

```bash
./scripts/install_microsoft_local_ai.sh
```

### What it does

1. Clones (or updates) `https://github.com/microsoft/BitNet` with submodules.
2. Installs Python dependencies from `requirements.txt`.
3. Verifies the install by running `setup_env.py -h`.

### Optional next step (download model + prepare runtime)

```bash
huggingface-cli download microsoft/BitNet-b1.58-2B-4T-gguf --local-dir "$HOME/bitnet.cpp/models/BitNet-b1.58-2B-4T"
python "$HOME/bitnet.cpp/setup_env.py" -md "$HOME/bitnet.cpp/models/BitNet-b1.58-2B-4T" -q i2_s
```
