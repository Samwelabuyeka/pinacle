param(
    [string]$ProjectRoot = "C:\Users\Marlyne\Documents\New project\pinacle",
    [string]$VendorRoot = "C:\Users\Marlyne\maya_vendor"
)

$ErrorActionPreference = "Stop"

$androidRoot = Join-Path $ProjectRoot "android-native"
$appRoot = Join-Path $androidRoot "app"
$assetsRoot = Join-Path $appRoot "src\main\assets"
$jniRoot = Join-Path $appRoot "src\main\jniLibs\arm64-v8a"
$manifestPath = Join-Path $assetsRoot "runtime_bundle_manifest.json"

New-Item -ItemType Directory -Force -Path $assetsRoot | Out-Null
New-Item -ItemType Directory -Force -Path $jniRoot | Out-Null

$report = [ordered]@{
    generatedAt = (Get-Date).ToString("o")
    mlc = [ordered]@{ bundled = $false; notes = @() }
    whisper = [ordered]@{ bundled = $false; notes = @() }
    wakeWord = [ordered]@{ bundled = $false; notes = @() }
}

function Copy-IfExists {
    param(
        [string]$Source,
        [string]$Destination
    )
    if (Test-Path -LiteralPath $Source) {
        Copy-Item -LiteralPath $Source -Destination $Destination -Force
        return $true
    }
    return $false
}

function Find-FirstFile {
    param(
        [string]$Root,
        [string[]]$Patterns
    )
    if (-not (Test-Path -LiteralPath $Root)) {
        return $null
    }
    foreach ($pattern in $Patterns) {
        $match = Get-ChildItem -LiteralPath $Root -Recurse -File -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -like $pattern } |
            Select-Object -First 1
        if ($match) {
            return $match.FullName
        }
    }
    return $null
}

$mlcRoot = Join-Path $androidRoot "dist\lib\mlc4j"
$mlcConfig = Join-Path $mlcRoot "src\main\assets\mlc-app-config.json"
$mlcSo = Join-Path $mlcRoot "output\arm64-v8a\libtvm4j_runtime_packed.so"
$mlcConfigCopied = Copy-IfExists -Source $mlcConfig -Destination (Join-Path $assetsRoot "mlc-app-config.json")
$mlcSoCopied = Copy-IfExists -Source $mlcSo -Destination (Join-Path $jniRoot "libtvm4j_runtime_packed.so")
if ($mlcConfigCopied -or $mlcSoCopied) {
    $report.mlc.bundled = $true
}
if ($mlcConfigCopied) {
    $report.mlc.notes += "Copied mlc-app-config.json into Android assets."
} else {
    $report.mlc.notes += "MLC config asset not found under android-native/dist/lib/mlc4j."
}
if ($mlcSoCopied) {
    $report.mlc.notes += "Copied libtvm4j_runtime_packed.so into jniLibs."
} else {
    $report.mlc.notes += "MLC runtime shared library not found under android-native/dist/lib/mlc4j/output/arm64-v8a."
}

$whisperRoot = Join-Path $VendorRoot "whisper.cpp"
$whisperModel = Find-FirstFile -Root $whisperRoot -Patterns @("ggml-*.bin", "*whisper*.bin")
$whisperLib = Find-FirstFile -Root $whisperRoot -Patterns @("libwhisper.so", "whisper.dll")
if ($whisperModel) {
    Copy-Item -LiteralPath $whisperModel -Destination (Join-Path $assetsRoot "whisper-model.bin") -Force
    $report.whisper.bundled = $true
    $report.whisper.notes += "Copied whisper model from $whisperModel."
} else {
    $report.whisper.notes += "No whisper model file was found under $whisperRoot."
}
if ($whisperLib -and $whisperLib.ToLower().EndsWith(".so")) {
    Copy-Item -LiteralPath $whisperLib -Destination (Join-Path $jniRoot "libwhisper.so") -Force
    $report.whisper.bundled = $true
    $report.whisper.notes += "Copied libwhisper.so from $whisperLib."
} elseif ($whisperLib) {
    $report.whisper.notes += "Found non-Android whisper native library at $whisperLib. Android .so still needed."
} else {
    $report.whisper.notes += "No Android whisper shared library was found."
}

$wakeRoot = Join-Path $VendorRoot "openWakeWord"
$wakeModel = Find-FirstFile -Root $wakeRoot -Patterns @("*wake*.tflite", "*wake*.onnx", "*openwakeword*.tflite")
$wakeLib = Find-FirstFile -Root $wakeRoot -Patterns @("libopenwakeword.so", "openwakeword.dll")
if ($wakeModel) {
    Copy-Item -LiteralPath $wakeModel -Destination (Join-Path $assetsRoot "wake-word-model.tflite") -Force
    $report.wakeWord.bundled = $true
    $report.wakeWord.notes += "Copied wake-word model from $wakeModel."
} else {
    $report.wakeWord.notes += "No wake-word model file was found under $wakeRoot."
}
if ($wakeLib -and $wakeLib.ToLower().EndsWith(".so")) {
    Copy-Item -LiteralPath $wakeLib -Destination (Join-Path $jniRoot "libopenwakeword.so") -Force
    $report.wakeWord.bundled = $true
    $report.wakeWord.notes += "Copied libopenwakeword.so from $wakeLib."
} elseif ($wakeLib) {
    $report.wakeWord.notes += "Found non-Android wake-word native library at $wakeLib. Android .so still needed."
} else {
    $report.wakeWord.notes += "No Android wake-word shared library was found."
}

$report | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath $manifestPath -Encoding UTF8
Write-Host "Runtime bundle manifest written to $manifestPath"
Write-Host ($report | ConvertTo-Json -Depth 6)
