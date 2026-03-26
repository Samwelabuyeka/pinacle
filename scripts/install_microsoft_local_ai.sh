#!/usr/bin/env bash
set -euo pipefail

BITNET_DIR="${BITNET_DIR:-$HOME/bitnet.cpp}"
MODEL_DIR="$BITNET_DIR/models/BitNet-b1.58-2B-4T"
GGUF_PATH="$MODEL_DIR/ggml-model-i2_s.gguf"

if [ ! -d "$BITNET_DIR/.git" ]; then
  git clone --recursive https://github.com/microsoft/BitNet "$BITNET_DIR"
else
  git -C "$BITNET_DIR" pull --ff-only
  git -C "$BITNET_DIR" submodule update --init --recursive
fi

python -m pip install --user -r "$BITNET_DIR/requirements.txt"

# Compatibility patch for toolchains that fail on const-correctness.
python - <<'PY'
from pathlib import Path
p = Path.home() / "bitnet.cpp" / "src" / "ggml-bitnet-mad.cpp"
if p.exists():
    s = p.read_text()
    old = "int8_t * y_col = y + col * by;"
    new = "const int8_t * y_col = y + col * by;"
    if old in s:
        p.write_text(s.replace(old, new))
PY

mkdir -p "$MODEL_DIR"
if [ ! -s "$GGUF_PATH" ]; then
  printf 'placeholder' > "$GGUF_PATH"
fi

(
  cd "$BITNET_DIR"
  python setup_env.py -md "$MODEL_DIR" -q i2_s
)

echo "bitnet.cpp installed and binary built at: $BITNET_DIR/build/bin/llama-cli"
echo "Next step: replace placeholder model with a real one, then run inference."
