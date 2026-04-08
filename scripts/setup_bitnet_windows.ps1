$ErrorActionPreference = "Stop"

$bitnetRoot = Join-Path $env:USERPROFILE "bitnet.cpp"
$pythonExe = "C:\Users\Marlyne\AppData\Local\Programs\Python\Python39\python.exe"
$cmakeExe = "C:\Program Files\CMake\bin\cmake.exe"
$llvmBin = "C:\Program Files\LLVM\bin"
$vsWhere = "${env:ProgramFiles(x86)}\Microsoft Visual Studio\Installer\vswhere.exe"

if (-not (Test-Path $bitnetRoot)) {
    throw "BitNet repo not found at $bitnetRoot"
}

if (-not (Test-Path $pythonExe)) {
    throw "Python 3.9 was not found at $pythonExe"
}

if (-not (Test-Path $cmakeExe)) {
    throw "CMake was not found at $cmakeExe"
}

$env:Path = "$llvmBin;C:\Program Files\CMake\bin;$([System.Environment]::GetEnvironmentVariable('Path', 'Machine'));$([System.Environment]::GetEnvironmentVariable('Path', 'User'))"

Write-Host "Using BitNet root: $bitnetRoot"
Write-Host "Using Python: $pythonExe"
Write-Host "Using CMake: $cmakeExe"

$pythonVersion = & $pythonExe --version
Write-Host $pythonVersion

if (Test-Path $vsWhere) {
    $vsInstall = & $vsWhere -latest -products * -requires Microsoft.VisualStudio.Component.VC.Tools.x86.x64 -property installationPath
    if ($vsInstall) {
        Write-Host "Visual Studio Build Tools detected at: $vsInstall"
    } else {
        Write-Warning "Visual Studio C++ build tools not detected yet. BitNet may not build successfully until they finish installing."
    }
} else {
    Write-Warning "vswhere.exe not found yet. Build Tools may still be installing."
}

Push-Location $bitnetRoot
try {
    function Invoke-WithRetry {
        param(
            [scriptblock]$Script,
            [string]$Description,
            [int]$MaxAttempts = 4
        )

        for ($attempt = 1; $attempt -le $MaxAttempts; $attempt++) {
            try {
                Write-Host "$Description (attempt $attempt/$MaxAttempts)"
                & $Script
                return
            } catch {
                if ($attempt -eq $MaxAttempts) {
                    throw
                }
                Write-Warning "$Description failed: $($_.Exception.Message)"
                Start-Sleep -Seconds (5 * $attempt)
            }
        }
    }

    Invoke-WithRetry -Description "Upgrading pip" -Script {
        & $pythonExe -m pip install --upgrade pip
        if ($LASTEXITCODE -ne 0) { throw "pip upgrade failed with exit code $LASTEXITCODE" }
    }

    Invoke-WithRetry -Description "Installing BitNet Python requirements" -Script {
        & $pythonExe -m pip install --no-cache-dir -r requirements.txt
        if ($LASTEXITCODE -ne 0) { throw "pip requirements install failed with exit code $LASTEXITCODE" }
    }

    $modelDir = Join-Path $bitnetRoot "models\BitNet-b1.58-2B-4T"
    if (-not (Test-Path $modelDir)) {
        New-Item -ItemType Directory -Force -Path $modelDir | Out-Null
    }

    Write-Host "Next recommended steps after prerequisites are fully present:"
    Write-Host "1. huggingface-cli download microsoft/BitNet-b1.58-2B-4T-gguf --local-dir $modelDir"
    Write-Host "2. $pythonExe setup_env.py -md $modelDir -q i2_s"
} finally {
    Pop-Location
}
