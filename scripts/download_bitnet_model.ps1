$ErrorActionPreference = "Stop"

$pythonExe = "C:\Users\Marlyne\AppData\Local\Programs\Python\Python39\python.exe"
$modelDir = Join-Path $env:USERPROFILE "bitnet.cpp\models\BitNet-b1.58-2B-4T"
$repoId = "microsoft/BitNet-b1.58-2B-4T-gguf"

if (-not (Test-Path $pythonExe)) {
    throw "Python 3.9 was not found at $pythonExe"
}

New-Item -ItemType Directory -Force -Path $modelDir | Out-Null

& $pythonExe -m pip install --upgrade "huggingface_hub<1.0"
if ($LASTEXITCODE -ne 0) {
    throw "Failed to install a transformers-compatible huggingface_hub version"
}

@"
from huggingface_hub import snapshot_download
snapshot_download(
    repo_id="$repoId",
    local_dir=r"$modelDir",
    local_dir_use_symlinks=False,
    resume_download=True
)
"@ | & $pythonExe -
if ($LASTEXITCODE -ne 0) {
    throw "Failed to download BitNet model from Hugging Face"
}

Write-Host "BitNet model downloaded to $modelDir"
