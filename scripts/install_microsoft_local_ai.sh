#!/usr/bin/env bash
set -euo pipefail

BITNET_DIR="${BITNET_DIR:-$HOME/bitnet.cpp}"

if [ ! -d "$BITNET_DIR/.git" ]; then
  git clone --recursive https://github.com/microsoft/BitNet "$BITNET_DIR"
else
  git -C "$BITNET_DIR" pull --ff-only
  git -C "$BITNET_DIR" submodule update --init --recursive
fi

python -m pip install --user -r "$BITNET_DIR/requirements.txt"
python "$BITNET_DIR/setup_env.py" -h >/dev/null

echo "bitnet.cpp is installed at: $BITNET_DIR"
echo "Next step (requires model download):"
echo "  huggingface-cli download microsoft/BitNet-b1.58-2B-4T-gguf --local-dir $BITNET_DIR/models/BitNet-b1.58-2B-4T"
echo "  python $BITNET_DIR/setup_env.py -md $BITNET_DIR/models/BitNet-b1.58-2B-4T -q i2_s"
