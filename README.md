# pinacle the greates

## Microsoft open-source local AI runtime

This repo now includes a runnable installer for **ONNX Runtime GenAI** (by Microsoft), which is used to run generative AI models locally.

### Run the installation

```bash
./scripts/install_microsoft_local_ai.sh
```

### What the script does

```bash
python -m pip install --user onnxruntime-genai
python - <<'PY'
import onnxruntime_genai as og
print(f"onnxruntime-genai installed: {og.__version__}")
PY
```
