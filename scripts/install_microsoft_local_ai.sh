#!/usr/bin/env bash
set -euo pipefail

python -m pip install --user onnxruntime-genai
python - <<'PY'
import onnxruntime_genai as og
print(f"onnxruntime-genai installed: {og.__version__}")
PY
