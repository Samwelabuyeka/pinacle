#!/usr/bin/env bash
set -euo pipefail

BITNET_DIR="${BITNET_DIR:-$HOME/bitnet.cpp}"
MODEL_DIR="$BITNET_DIR/models/BitNet-b1.58-2B-4T"
GGUF_PATH="$MODEL_DIR/ggml-model-i2_s.gguf"
PERM_FILE="${MAYA_PERMISSIONS_FILE:-$HOME/.maya_permissions.json}"

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

# Permission prompts at install (interactive TTY only).
if [ -t 0 ]; then
  python - <<'PY'
import json
from pathlib import Path

perm_file = Path.home() / '.maya_permissions.json'
def ask(name, default=False):
    hint = 'Y/n' if default else 'y/N'
    value = input(f'Grant permission `{name}`? [{hint}] ').strip().lower()
    if not value:
        return default
    return value in {'y','yes'}

perms = {
    'ai_chat': True,
    'send_sms': ask('send_sms'),
    'make_calls': ask('make_calls'),
    'manage_calendar': ask('manage_calendar'),
    'location_access': ask('location_access'),
    'run_when_phone_off': ask('run_when_phone_off'),
    'device_search': ask('device_search'),
    'always_mic': ask('always_mic', default=True),
    'always_speaker': ask('always_speaker', default=True),
    'os_level_control': ask('os_level_control'),
}
perm_file.write_text(json.dumps(perms, indent=2))
print(f'Permissions saved: {perm_file}')
PY
else
  python - <<'PY'
import json
from pathlib import Path
perm_file = Path.home() / '.maya_permissions.json'
if not perm_file.exists():
    perm_file.write_text(json.dumps({
        'ai_chat': True,
        'send_sms': False,
        'make_calls': False,
        'manage_calendar': False,
        'location_access': False,
        'run_when_phone_off': False,
        'device_search': False,
        'always_mic': True,
        'always_speaker': True,
        'os_level_control': False,
    }, indent=2))
print(f'Permissions file ready: {perm_file}')
PY
fi

echo "bitnet.cpp installed and binary built at: $BITNET_DIR/build/bin/llama-cli"
echo "Permission settings: $PERM_FILE (editable from frontend too)."
echo "Next step: replace placeholder model with a real one, then run inference."
