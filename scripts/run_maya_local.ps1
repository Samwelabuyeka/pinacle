$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$python = "C:\Users\Marlyne\AppData\Local\Programs\Python\Python39\python.exe"

if (-not (Test-Path $python)) {
    throw "Python 3.9 not found at $python"
}

& $python "$root\scripts\maya_offline_voice.py" --text-only
